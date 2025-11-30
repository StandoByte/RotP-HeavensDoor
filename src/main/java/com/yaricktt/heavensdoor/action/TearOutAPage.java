package com.yaricktt.heavensdoor.action;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.yaricktt.heavensdoor.capability.entity.LivingUtilCap;
import com.yaricktt.heavensdoor.capability.entity.LivingUtilCapProvider;
import com.yaricktt.heavensdoor.init.InitEffects;
import com.yaricktt.heavensdoor.init.InitItems;
import com.yaricktt.heavensdoor.item.PageItem;
import com.yaricktt.heavensdoor.itemtracking.PageTrackerMap;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class TearOutAPage extends StandEntityAction {
    public TearOutAPage(StandEntityAction.Builder builder) {
        super(builder);
    }
    public static final StandPose TEAR_OUT_A_PAGE = new StandPose("tearPage");

    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        Entity entityTarget = target.getEntity();
        if (entityTarget instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) entityTarget;
            int pageCount = livingTarget.getCapability(LivingUtilCapProvider.CAPABILITY)
                    .map(LivingUtilCap::getPageCount)
                    .orElse(0);
            if (!livingTarget.hasEffect(InitEffects.BOOK.get())) {
                return conditionMessage("no_book");
            } else if (pageCount >= 10) {
                return conditionMessage("10_page");
            } else if (user.isAlive() && user.getHealth() <= user.getMaxHealth() * 0.26F && (user instanceof PlayerEntity && !(((PlayerEntity)user).abilities.instabuild))) {
                return conditionMessage("low_hp");
            } else if (user.distanceToSqr(entityTarget) > 4.5 * 4.5) {
                return conditionMessage("too_far");
            }
            return ActionConditionResult.POSITIVE;
        }
        return ActionConditionResult.NEGATIVE_QUEUEABLE;
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            ActionTarget t = task.getTarget();
            LivingEntity user = userPower.getUser();
            Entity entityTarget = t.getEntity();

            if (entityTarget instanceof StandEntity) {
                LivingEntity target = ((StandEntity) entityTarget).getUser();
                if (world.getServer() != null && target != null) {
                    PageTrackerMap.get(world.getServer()).reviveTarget(target.getUUID());
                }
                target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                int pageCount = cap.getPageCount();

                if (pageCount >= 10 || target.getMaxHealth() <= 1.0F || !target.hasEffect(InitEffects.BOOK.get()))
                    return;

                int newPageCount = cap.getPageCount() + 1;
                cap.updatePageCount(newPageCount, target);
                cap.addForgotEffect(target);

                ItemStack page = PageItem.withTargetAndCreator(new ItemStack(InitItems.PAGE.get()), target, user);
                MCUtil.giveItemTo(user, page, false);
            });
            } else if (entityTarget instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) entityTarget;
                if (world.getServer() != null && target != null) {
                    PageTrackerMap.get(world.getServer()).reviveTarget(target.getUUID());
                }

                target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    int pageCount = cap.getPageCount();

                    if (pageCount >= 10 || target.getMaxHealth() <= 1.0F || !target.hasEffect(InitEffects.BOOK.get()))
                        return;

                    int newPageCount = cap.getPageCount() + 1;
                    cap.updatePageCount(newPageCount, target);
                    cap.addForgotEffect(target);

                    ItemStack page = PageItem.withTargetAndCreator(new ItemStack(InitItems.PAGE.get()), target, user);
                    MCUtil.giveItemTo(user, page, false);
                });
            }
        }
    }

    @Override
    public void onTaskSet(World world, StandEntity standEntity, IStandPower standPower, Phase phase, StandEntityTask task, int ticks) {
        if (task.getPhase() == Phase.BUTTON_HOLD && !standEntity.isManuallyControlled()) {
            ActionTarget target = task.getTarget();
            Entity entity = target.getEntity();
            LivingEntity user = standPower.getUser();
            if (entity instanceof LivingEntity && entity.isAlive()) {
                Vector3d dir_difference=entity.position().subtract(user.position());
                Vector3d normal_dir=dir_difference.normalize();
                standEntity.lookAt(EntityAnchorArgument.Type.EYES, normal_dir);
                standEntity.moveTo(entity.position().subtract(normal_dir));
            }
        }
    }

    @Override
    protected boolean standKeepsTarget(ActionTarget target) {
        return target.getType() == ActionTarget.TargetType.ENTITY && target.getEntity() instanceof LivingEntity;
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }

    @Override
    public boolean cancelHeldOnGettingAttacked(IStandPower power, DamageSource dmgSource, float dmgAmount) {
        return true;
    }

    @Override
    public boolean noAdheringToUserOffset(IStandPower standPower, StandEntity standEntity) {
        return true;
    }
}