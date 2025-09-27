package com.yaricktt.heavensdoor.mixin;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.yaricktt.heavensdoor.capability.entity.LivingUtilCapProvider;
import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import java.util.Comparator;

import static com.yaricktt.heavensdoor.capability.entity.LivingUtilCap.BehaviorState.*;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @Inject(method = "createMobAttributes", at = @At("RETURN"), cancellable = true)
    private static void addAttackAttribute(CallbackInfoReturnable<AttributeModifierMap.MutableAttribute> cir) {
        AttributeModifierMap.MutableAttribute attributes = cir.getReturnValue();
        if (!attributes.hasAttribute(Attributes.ATTACK_DAMAGE)) {
            attributes.add(Attributes.ATTACK_DAMAGE, 1);
        }
        cir.setReturnValue(attributes);
    }

    @Unique
    private boolean rotP_HeavensDoor$targetIsTooClose(MobEntity mob, @Nullable LivingEntity target) {
        if (target == null) return false;
        return mob.hasEffect(InitEffects.BLINDNESS_ENTITY_EFFECT.get()) && mob.distanceToSqr(target) <= 10.0 * 10.0;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;
        if (mob.level.isClientSide()) return;
        LivingEntity target = mob.getTarget();
        if (target instanceof StandEntity || (target instanceof PlayerEntity && ((PlayerEntity) target).abilities.instabuild)
                || rotP_HeavensDoor$targetIsTooClose(mob, mob.getTarget())) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            mob.setLastHurtByPlayer(null);
            ((MobEntity) mob).targetSelector.getRunningGoals().filter(prioritizedGoal -> prioritizedGoal.getGoal() instanceof HurtByTargetGoal).findFirst().ifPresent(PrioritizedGoal::stop);
        }

        mob.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getCurrentBehavior() == PEACEFUL && cap.hasBehaviorBeenModified()) {
                mob.setTarget(null);
                mob.setLastHurtByMob(null);
                mob.setLastHurtByPlayer(null);
                ((MobEntity) mob).targetSelector.getRunningGoals().filter(prioritizedGoal -> prioritizedGoal.getGoal() instanceof HurtByTargetGoal).findFirst().ifPresent(PrioritizedGoal::stop);
            }
            if (cap.getCurrentBehavior() == AGGRESSIVE && cap.hasBehaviorBeenModified()) {
                AxisAlignedBB area = mob.getBoundingBox().inflate(8);
                LivingEntity entity = mob.level.getEntitiesOfClass(LivingEntity.class, area, e -> e != mob)
                        .stream()
                        .min(Comparator.comparingDouble(e -> mob.distanceToSqr(e)))
                        .orElse(null);

                if (entity != null && entity != mob && !(mob instanceof AmbientEntity)) {
                    mob.setTarget(entity);
                //    mob.targetSelector.addGoal(2, new MeleeAttackGoalForPeaceful((CreatureEntity) mob, 1.0, false));
                }

            }
        });
    }

    @Inject(method = "canAttack", at = @At("HEAD"), cancellable = true)
    private void onCanAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        MobEntity mob = (MobEntity) (Object) this;
        if (mob.level.isClientSide()) return;
        if (target instanceof StandEntity || (target instanceof PlayerEntity && ((PlayerEntity) target).abilities.instabuild)
                || rotP_HeavensDoor$targetIsTooClose(mob, target)) {
            cir.setReturnValue(false);
        }

        mob.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getCurrentBehavior() == PEACEFUL && cap.hasBehaviorBeenModified()) {
                cir.setReturnValue(false);
            }
        });
    }

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void onSetTarget(@Nullable LivingEntity target, CallbackInfo ci) {
        if (target == null) return;
        MobEntity mob = (MobEntity) (Object) this;
        if (mob.level.isClientSide()) return;
        if (target instanceof StandEntity || (target instanceof PlayerEntity && ((PlayerEntity) target).abilities.instabuild)
                || rotP_HeavensDoor$targetIsTooClose(mob, target)) {
            ci.cancel();
        }

        mob.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            if (cap.getCurrentBehavior() == PEACEFUL && cap.hasBehaviorBeenModified()) {
                ci.cancel();
            }
        });
    }
}