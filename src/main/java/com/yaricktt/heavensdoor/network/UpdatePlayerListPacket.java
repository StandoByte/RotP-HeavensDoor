package com.yaricktt.heavensdoor.network;

import com.yaricktt.heavensdoor.client.ui.screen.PlayerSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class UpdatePlayerListPacket {

    private final List<UUID> playerUuids;

    public UpdatePlayerListPacket(List<UUID> playerUuids) {
        this.playerUuids = playerUuids;
    }

    public static void encode(UpdatePlayerListPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.playerUuids.size());
        for (UUID uuid : msg.playerUuids) {
            buf.writeUUID(uuid);
        }
    }

    public static UpdatePlayerListPacket decode(PacketBuffer buf) {
        int size = buf.readInt();
        List<UUID> uuids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            uuids.add(buf.readUUID());
        }
        return new UpdatePlayerListPacket(uuids);
    }

    public static void handle(UpdatePlayerListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof PlayerSelectionScreen) {
                PlayerSelectionScreen screen = (PlayerSelectionScreen) mc.screen;
                List<NetworkPlayerInfo> playerInfoList = new ArrayList<>();
                for (UUID uuid : msg.playerUuids) {
                    if (mc.player != null && mc.player.connection != null) {
                        playerInfoList.add(mc.player.connection.getPlayerInfo(uuid));
                    }
                }
                screen.initGrid(playerInfoList);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}