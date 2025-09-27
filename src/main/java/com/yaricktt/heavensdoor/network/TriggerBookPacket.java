package com.yaricktt.heavensdoor.network;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TriggerBookPacket {

    private final int targetEntityUUID;
    private final int durationTicks;

    public TriggerBookPacket(int targetEntityId, int durationTicks) {
        this.targetEntityUUID = targetEntityId;
        this.durationTicks = durationTicks;
    }

    public static void encode(TriggerBookPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.targetEntityUUID);
        buf.writeInt(msg.durationTicks);
    }

    public static TriggerBookPacket decode(PacketBuffer buf) {
        return new TriggerBookPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(TriggerBookPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender == null) return;
            ServerWorld serverWorld = sender.getLevel();
            Entity target = serverWorld.getEntity(msg.targetEntityUUID);
            if (target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity) target;

                if (msg.durationTicks > 0) {
                    EffectInstance effectInstance = new EffectInstance(
                            InitEffects.BOOK.get(),
                            msg.durationTicks,
                            0,
                            false,
                            false,
                            false
                    );
                    livingTarget.addEffect(effectInstance);
                } else {
                    livingTarget.removeEffect(InitEffects.BOOK.get());
                    if (livingTarget instanceof StandEntity) {
                        StandEntity livingTargetStand = (StandEntity) livingTarget;
                        if (livingTargetStand.getUser() != null) {
                            livingTargetStand.getUser().removeEffect(InitEffects.BOOK.get());
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}