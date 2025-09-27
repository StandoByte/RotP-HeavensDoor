package com.yaricktt.heavensdoor.network;

import com.yaricktt.heavensdoor.capability.entity.LivingUtilCapProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncLivingUtilCapPacket {
    private final int entityId;
    private final long liveTime;
    private final int pageCount;

    public SyncLivingUtilCapPacket(int entityId, long liveTime, int pageCount) {
        this.entityId = entityId;
        this.liveTime = liveTime;
        this.pageCount = pageCount;
    }

    public static void encode(SyncLivingUtilCapPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
        buf.writeLong(msg.liveTime);
        buf.writeInt(msg.pageCount);
    }

    public static SyncLivingUtilCapPacket decode(PacketBuffer buf) {
        return new SyncLivingUtilCapPacket(buf.readInt(), buf.readLong(), buf.readInt());
    }

    public static void handle(SyncLivingUtilCapPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                ClientWorld world = Minecraft.getInstance().level;
                if (world != null) {
                    Entity entity = world.getEntity(msg.entityId);
                    if (entity != null) {
                        entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                            cap.setLiveTime(msg.liveTime);
                            cap.setPageCount(msg.pageCount);
                        });
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}