package com.yaricktt.heavensdoor.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class RemoveBook extends StandEntityHeavyAttack {
    public RemoveBook(StandEntityHeavyAttack.Builder builder) {
        super(builder);
    }

    //@Nullable
    //@Override
    //protected Action<IStandPower> replaceAction(IStandPower power, ActionTarget target) {
    //    StandEntity standEntity = power.isActive() ? (StandEntity) power.getStandManifestation() : null;
    //    if (standEntity != null) {
    //        LivingEntity user = standEntity.getUser();
    //        IStandPower powerStand = IStandPower.getPlayerStandPower((PlayerEntity) user);
    //        if (standEntity.getFinisherMeter() >= 0.5 && powerStand.getResolveLevel() >= 3) {
    //            return InitStands.HEAVENS_DOOR_FINISHER_PUNCH.get();
    //        }
    //    }
    //    return this;
    //}

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return super.punchEntity(stand, target, dmgSource)
                .knockbackXRot(0.0F)
                .impactSound(SoundEvents.VILLAGER_WORK_LIBRARIAN.delegate)
                .damage(0F);

    }

     @Override
     public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
     Entity entityTarget = target.getEntity();
         if (entityTarget instanceof LivingEntity) {
                 LivingEntity livingTarget = (LivingEntity) entityTarget;
                 if (!livingTarget.hasEffect(InitEffects.BOOK.get())) {
                         return conditionMessage("no_book");
                     }
                 return ActionConditionResult.POSITIVE;
         }
         return ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
     }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            ActionTarget t = task.getTarget();
            LivingEntity victim = (LivingEntity) t.getEntity();
            if (t.getType() == ActionTarget.TargetType.ENTITY && victim.hasEffect(InitEffects.BOOK.get())) {
                victim.removeEffect(InitEffects.BOOK.get());
                if (victim instanceof StandEntity) {
                    StandEntity victimStand = (StandEntity) victim;
                    if (victimStand.getUser() != null) {
                        victimStand.getUser().removeEffect(InitEffects.BOOK.get());
                    }
                }
            }
        }
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
}