package com.yaricktt.heavensdoor.network;

import com.yaricktt.heavensdoor.capability.entity.LivingUtilCapProvider;
import com.yaricktt.heavensdoor.entity.ai.goal.MeleeAttackGoalForPeaceful;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Comparator;
import java.util.function.Supplier;

import static com.yaricktt.heavensdoor.capability.entity.LivingUtilCap.BehaviorState.*;

public class TriggerChangeBehaviorPacket {

    private final int entityUUID;

    public TriggerChangeBehaviorPacket(int entityUUID) {
        this.entityUUID = entityUUID;
    }

    public static void encode(TriggerChangeBehaviorPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityUUID);
    }

    public static TriggerChangeBehaviorPacket decode(PacketBuffer buf) {
        return new TriggerChangeBehaviorPacket(buf.readInt());
    }

    public static void handle(TriggerChangeBehaviorPacket msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.getSender();
            if (sender == null) return;
            World world = sender.getCommandSenderWorld();
            if (!(world.getEntity(msg.entityUUID) instanceof MobEntity)) {
                return;
            }

            MobEntity mob = (MobEntity) world.getEntity(msg.entityUUID);
            if (mob instanceof TameableEntity) {
                return;
            }

            mob.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.initializeBehavior(mob);
                cap.cycleBehavior();
                if (cap.getCurrentBehavior() == PEACEFUL) {
                    mob.setTarget(null);
                    mob.targetSelector.removeGoal(new MeleeAttackGoalForPeaceful((MobEntity) mob, 1.0, false));
                    mob.setLastHurtByPlayer(null);
                    mob.setLastHurtByMob(null);
                    ((MobEntity) mob).targetSelector.getRunningGoals().filter(prioritizedGoal -> prioritizedGoal.getGoal() instanceof HurtByTargetGoal).findFirst().ifPresent(PrioritizedGoal::stop);
                }
                if (cap.getCurrentBehavior() == AGGRESSIVE && cap.hasBehaviorBeenModified()) {
                    AxisAlignedBB area = mob.getBoundingBox().inflate(8);
                    LivingEntity targetEntity = mob.level.getEntitiesOfClass(LivingEntity.class, area, e -> e != mob)
                            .stream()
                            .min(Comparator.comparingDouble(e -> mob.distanceToSqr(e)))
                            .orElse(null);

                    if (targetEntity != null && targetEntity != mob && !(mob instanceof AmbientEntity)) {
                        mob.targetSelector.addGoal(2, new MeleeAttackGoalForPeaceful((MobEntity) mob, 1.0, false));
                    }

                }

                System.out.println("Set behavior for " + mob.getName().getString() + " to " + cap.getCurrentBehavior().name());
            });
        });
        ctx.setPacketHandled(true);
    }
}