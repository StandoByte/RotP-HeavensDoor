package com.yaricktt.heavensdoor.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RequestPlayerListPacket {

    private final int targetEntityId;

    public RequestPlayerListPacket(int targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public RequestPlayerListPacket() {
        this.targetEntityId = -1;
    }

    public static void encode(RequestPlayerListPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.targetEntityId);
    }

    public static RequestPlayerListPacket decode(PacketBuffer buf) {
        return new RequestPlayerListPacket(buf.readInt());
    }

    public static void handle(RequestPlayerListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender == null) return;
            UUID excludedUuid = null;
            Entity targetEntity = sender.getLevel().getEntity(msg.targetEntityId);
            if (targetEntity instanceof PlayerEntity) {
                excludedUuid = targetEntity.getUUID();
            }
            final UUID finalExcludedUuid = excludedUuid;
            List<UUID> playerUuids = sender.getServer().getPlayerList().getPlayers().stream()
                    .filter(player -> !player.isSpectator())
                    .filter(player -> !player.getUUID().equals(finalExcludedUuid))
                    .map(player -> player.getGameProfile().getId())
                    .collect(Collectors.toList());
            AddonPackets.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sender), new UpdatePlayerListPacket(playerUuids));
        });
        ctx.get().setPacketHandled(true);
    }
}