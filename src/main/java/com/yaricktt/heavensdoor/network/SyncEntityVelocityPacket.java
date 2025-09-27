package com.yaricktt.heavensdoor.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncEntityVelocityPacket {

    private final int entityUUID;
    private final double motionX;
    private final double motionY;
    private final double motionZ;

    public SyncEntityVelocityPacket(int entityId, Vector3d motion) {
        this.entityUUID = entityId;
        this.motionX = motion.x;
        this.motionY = motion.y;
        this.motionZ = motion.z;
    }

    private SyncEntityVelocityPacket(int entityId, double motionX, double motionY, double motionZ) {
        this.entityUUID = entityId;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(this.entityUUID);
        buffer.writeDouble(this.motionX);
        buffer.writeDouble(this.motionY);
        buffer.writeDouble(this.motionZ);
    }

    public static SyncEntityVelocityPacket decode(PacketBuffer buffer) {
        int entityId = buffer.readInt();
        double motionX = buffer.readDouble();
        double motionY = buffer.readDouble();
        double motionZ = buffer.readDouble();
        return new SyncEntityVelocityPacket(entityId, motionX, motionY, motionZ);
    }

    public static void handle(SyncEntityVelocityPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                ClientPacketHandler.handleVelocitySync(message);
            }
        });

        context.setPacketHandled(true);
    }

    public int getEntityUUID() {
        return entityUUID;
    }

    public double getMotionX() {
        return motionX;
    }

    public double getMotionY() {
        return motionY;
    }

    public double getMotionZ() {
        return motionZ;
    }

    private static class ClientPacketHandler {
        static void handleVelocitySync(SyncEntityVelocityPacket message) {
            World world = Minecraft.getInstance().level;
            if (world != null) {
                Entity entity = world.getEntity(message.getEntityUUID());
                if (entity != null) {
                    entity.setDeltaMovement(message.getMotionX(), message.getMotionY(), message.getMotionZ());
                }
            }
        }
    }
}