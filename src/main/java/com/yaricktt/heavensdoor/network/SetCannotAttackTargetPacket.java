package com.yaricktt.heavensdoor.network;

import com.yaricktt.heavensdoor.capability.entity.LivingUtilCapProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

public class SetCannotAttackTargetPacket {

    private final int targetEntityId;
    private final UUID selectedPlayerUUID;

    public SetCannotAttackTargetPacket(int targetEntityId, UUID selectedPlayerUUID) {
        this.targetEntityId = targetEntityId;
        this.selectedPlayerUUID = selectedPlayerUUID;
    }

    public static void encode(SetCannotAttackTargetPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.targetEntityId);
        buf.writeUUID(msg.selectedPlayerUUID);
    }

    public static SetCannotAttackTargetPacket decode(PacketBuffer buf) {
        return new SetCannotAttackTargetPacket(buf.readInt(), buf.readUUID());
    }

    public static void handle(SetCannotAttackTargetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender == null) return;
            ServerWorld world = sender.getLevel();
            Entity target = world.getEntity(msg.targetEntityId);
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                livingTarget.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    cap.setPlayerTargetUUID(msg.selectedPlayerUUID);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}