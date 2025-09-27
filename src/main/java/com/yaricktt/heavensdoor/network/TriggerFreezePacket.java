package com.yaricktt.heavensdoor.network;

import com.github.standobyte.jojo.init.ModStatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TriggerFreezePacket {

    private final int targetEntityId;
    private final int durationTicks;

    public TriggerFreezePacket(int targetEntityId, int durationTicks) {
        this.targetEntityId = targetEntityId;
        this.durationTicks = durationTicks;
    }

    public static void encode(TriggerFreezePacket msg, PacketBuffer buf) {
        buf.writeInt(msg.targetEntityId);
        buf.writeInt(msg.durationTicks);
    }

    public static TriggerFreezePacket decode(PacketBuffer buf) {
        return new TriggerFreezePacket(buf.readInt(), buf.readInt());
    }

    public static void handle(TriggerFreezePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender == null) return;
            ServerWorld serverWorld = sender.getLevel();
            Entity target = serverWorld.getEntity(msg.targetEntityId);
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;
                EffectInstance effectInstance = new EffectInstance(
                        ModStatusEffects.STUN.get(),
                        msg.durationTicks,
                        0,
                        false,
                        false,
                        false
                );

                livingTarget.addEffect(effectInstance);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}