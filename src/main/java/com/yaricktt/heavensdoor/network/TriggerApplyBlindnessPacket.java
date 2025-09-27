package com.yaricktt.heavensdoor.network;

import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class TriggerApplyBlindnessPacket {

    private final int targetEntityUUID;
    private final int durationTicks;

    public TriggerApplyBlindnessPacket(int targetEntityId, int durationTicks) {
        this.targetEntityUUID = targetEntityId;
        this.durationTicks = durationTicks;
    }

    public static void encode(TriggerApplyBlindnessPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.targetEntityUUID);
        buf.writeInt(msg.durationTicks);
    }

    public static TriggerApplyBlindnessPacket decode(PacketBuffer buf) {
        return new TriggerApplyBlindnessPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(TriggerApplyBlindnessPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender == null) return;
            ServerWorld serverWorld = sender.getLevel();
            Entity target = serverWorld.getEntity(msg.targetEntityUUID);
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (msg.durationTicks > 0) {
                    EffectInstance effectInstance = new EffectInstance(
                            InitEffects.BLINDNESS_ENTITY_EFFECT.get(),
                            msg.durationTicks,
                            0,
                            false,
                            false,
                            false
                    );
                    livingTarget.addEffect(effectInstance);
                    if (livingTarget instanceof MobEntity) {
                        MobEntity mobTarget = (MobEntity) livingTarget;
                        if (mobTarget.getTarget() != null && mobTarget.distanceToSqr(mobTarget.getTarget()) <= 10.0 * 10.0) {
                            mobTarget.setTarget(null);
                            mobTarget.setLastHurtByMob(null);
                            mobTarget.setLastHurtByPlayer(null);
                            mobTarget.setLastHurtMob(null);
                        }
                    }
                } else {
                    livingTarget.removeEffect(InitEffects.BLINDNESS_ENTITY_EFFECT.get());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}