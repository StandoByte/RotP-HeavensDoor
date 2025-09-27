package com.yaricktt.heavensdoor.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.yaricktt.heavensdoor.client.ui.screen.BookGui;
import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class OpenBook extends StandAction {
    public OpenBook(StandAction.Builder builder){
        super(builder);
    }

    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        Entity entityTarget = target.getEntity();
        if (entityTarget instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) entityTarget;
            if (!livingTarget.hasEffect(InitEffects.BOOK.get())) {
                return conditionMessage("no_book");
            } else if (user.distanceToSqr(entityTarget) > 4.5 * 4.5) {
                return conditionMessage("too_far");
            }
            return ActionConditionResult.POSITIVE;
        }
        return ActionConditionResult.NEGATIVE;
    }

    @Override
    protected void perform(World world, LivingEntity user, IStandPower power, ActionTarget target) {
        if (world.isClientSide) {
            BookGui.openWindowOnClick(user, target.getEntity().getId());
        }
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }
}