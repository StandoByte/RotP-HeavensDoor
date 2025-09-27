package com.yaricktt.heavensdoor.network;

import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TriggerApplyKYSPacket {

    private final int targetEntityUUID;
    private final int durationTicks;

    public TriggerApplyKYSPacket(int targetEntityId, int durationTicks) {
        this.targetEntityUUID = targetEntityId;
        this.durationTicks = durationTicks;
    }

    public static void encode(TriggerApplyKYSPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.targetEntityUUID);
        buf.writeInt(msg.durationTicks);
    }

    public static TriggerApplyKYSPacket decode(PacketBuffer buf) {
        return new TriggerApplyKYSPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(TriggerApplyKYSPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender == null) return;
            ServerWorld serverWorld = sender.getLevel();
            Entity target = serverWorld.getEntity(msg.targetEntityUUID);
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                if (msg.durationTicks > 0) {
                    EffectInstance effectInstance = new EffectInstance(
                            InitEffects.KYS_EFFECT.get(),
                            msg.durationTicks,
                            0,
                            false,
                            false,
                            false
                    );
                    livingTarget.addEffect(effectInstance);
                } else {
                    livingTarget.removeEffect(InitEffects.KYS_EFFECT.get());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}