package com.yaricktt.heavensdoor.network;

import com.github.standobyte.jojo.init.ModStatusEffects;
import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class TriggerFlyBackwardsPacket {

    private final int targetEntityId;

    public TriggerFlyBackwardsPacket(int targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.targetEntityId);
    }

    public static TriggerFlyBackwardsPacket decode(PacketBuffer buffer) {
        return new TriggerFlyBackwardsPacket(buffer.readInt());
    }

    public static void handle(TriggerFlyBackwardsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;
            ServerWorld world = player.getLevel();
            Entity targetEntity = world.getEntity(msg.targetEntityId);
            if (targetEntity instanceof LivingEntity) {
                LivingEntity victim = (LivingEntity) targetEntity;
                if (!victim.isAlive()) return;
                if (!victim.hasEffect(InitEffects.BOOK.get())) {
                    return;
                }

                if (victim.hasEffect(ModStatusEffects.STUN.get())) {
                    victim.removeEffect(ModStatusEffects.STUN.get());
                }

                final double MAX_DIST_SQ_SERVER = 5.5 * 5.5;
                if (player.distanceToSqr(victim) > MAX_DIST_SQ_SERVER) {
                    return;
                }

                Vector3d directionVector = victim.position().subtract(player.position());
                directionVector = new Vector3d(directionVector.x, 0, directionVector.z);
                if (directionVector.lengthSqr() > 1.0E-4) {
                    directionVector = directionVector.normalize();
                    double strength = 3.25;
                    double yOffset = 0.35;
                    Vector3d resultingVelocity = directionVector.scale(strength).add(0, yOffset, 0);
                    victim.setDeltaMovement(resultingVelocity);
                    SyncEntityVelocityPacket packet = new SyncEntityVelocityPacket(victim.getId(), resultingVelocity);
                    AddonPackets.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> victim), packet);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}