package com.yaricktt.heavensdoor.action;


import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandEntityTask;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.yaricktt.heavensdoor.capability.entity.LivingUtilCapProvider;
import com.yaricktt.heavensdoor.init.InitItems;
import com.yaricktt.heavensdoor.item.PageItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

import static com.yaricktt.heavensdoor.item.PageItem.PLAYER_DATA_KEY;

public class ReturnPages extends StandEntityAction {

    public ReturnPages(StandEntityAction.Builder builder) {
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, IStandPower power, ActionTarget target) {
        ItemStack heldItem = user.getMainHandItem();
        if (user instanceof PlayerEntity) {
            if (heldItem.getItem() != InitItems.PAGE.get() || heldItem.isEmpty()) {
                return conditionMessage("no_page");
            } else if (target == null) {
                return conditionMessage("no_return");
            }
            return ActionConditionResult.POSITIVE;
        }
        return ActionConditionResult.NEGATIVE_QUEUEABLE;
    }

    @Override
    public void standPerform(World world, StandEntity standEntity, IStandPower userPower, StandEntityTask task) {
        if (!world.isClientSide()) {
            LivingEntity user = userPower.getUser();
            ItemStack heldItem = user.getMainHandItem();
            if (heldItem.getItem() != InitItems.PAGE.get())
                return;
            CompoundNBT pageData = heldItem.getTagElement(PLAYER_DATA_KEY);
            if (pageData == null)
                return;
            UUID targetUUID = pageData.getUUID(PageItem.TARGET_UUID_KEY);
            List<LivingEntity> entities = world.getEntitiesOfClass(
                    LivingEntity.class,
                    user.getBoundingBox().inflate(24)
            );
            for (LivingEntity target : entities) {
                if (!target.getUUID().equals(targetUUID)) continue;
                target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    int newPageCount = cap.getPageCount() - 1;
                    cap.updatePageCount(newPageCount, target);
                    heldItem.shrink(1);
                });
                break;
            }
        }
    }
}