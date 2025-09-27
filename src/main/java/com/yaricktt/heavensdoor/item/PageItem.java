package com.yaricktt.heavensdoor.item;

import com.yaricktt.heavensdoor.capability.entity.LivingUtilCap;
import com.yaricktt.heavensdoor.capability.entity.LivingUtilCapProvider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PageItem extends Item {
    public static final String PLAYER_DATA_KEY = "PLAYER_DATA";
    public static final String CREATOR_UUID_KEY = "CreatorUUID";
    public static final String TARGET_UUID_KEY = "TargetUUID";

    public PageItem(Properties properties) {
        super(properties);
    }

    public static boolean validPage(ItemStack stack) {
        CompoundNBT data = stack.getTagElement(PLAYER_DATA_KEY);
        return data != null && data.hasUUID(TARGET_UUID_KEY);
    }

    public static CompoundNBT createTargetNBT(LivingEntity target) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID(TARGET_UUID_KEY, target.getUUID());
        nbt.putString("PlayerName", target.getDisplayName().getString());
        return nbt;
    }

    public static ItemStack withTargetAndCreator(ItemStack stack, LivingEntity target, LivingEntity creator) {
        CompoundNBT pageNbt = createTargetNBT(target);
        pageNbt.putUUID(CREATOR_UUID_KEY, creator.getUUID());
        stack.addTagElement(PLAYER_DATA_KEY, pageNbt);
        return stack;
    }

    @Nullable
    public static UUID getTargetUUID(ItemStack stack) {
        CompoundNBT data = stack.getTagElement(PLAYER_DATA_KEY);
        return data != null && data.hasUUID(TARGET_UUID_KEY) ? data.getUUID(TARGET_UUID_KEY) : null;
    }

    @Nullable
    public LivingEntity getTargetEntity(ServerWorld world, ItemStack stack) {
        UUID targetUUID = getTargetUUID(stack);
        if (targetUUID == null) return null;

        Entity entity = world.getEntity(targetUUID);
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        if (world.isClientSide() || !validPage(stack)) return;

        ServerWorld serverWorld = (ServerWorld) world;
        CompoundNBT pageData = stack.getOrCreateTagElement(PLAYER_DATA_KEY);
        if (pageData.hasUUID(CREATOR_UUID_KEY)) {
            UUID creatorUUID = pageData.getUUID(CREATOR_UUID_KEY);
            Entity creator = serverWorld.getEntity(creatorUUID);
            if (creator instanceof LivingEntity && creator.isAlive() &&
                    ((LivingEntity) creator).getHealth() <= ((LivingEntity) creator).getMaxHealth() * 0.25F &&
                    (!(creator instanceof PlayerEntity) || !((PlayerEntity) creator).abilities.instabuild)) {
                restorePageToOwner(stack, serverWorld);
            }
        }
    }

    public void restorePageToOwner(ItemStack stack, ServerWorld world) {
        LivingEntity target = getTargetEntity(world, stack);
        if (target == null) return;
        target.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            int pageCount = cap.getPageCount();
            if (pageCount <= 0) return;
            int restoreCount = Math.min(stack.getCount(), pageCount);

            int newPageCount = pageCount - restoreCount;
            cap.updatePageCount(newPageCount, target);
            stack.shrink(restoreCount);
        });
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        CompoundNBT data = stack.getTagElement(PLAYER_DATA_KEY);
        if (data == null || !data.contains("PlayerName")) {
            tooltip.add(new TranslationTextComponent("rotp_hd.page.no_current_player")
                    .withStyle(TextFormatting.DARK_PURPLE));
            return;
        }
        if (data.contains("PlayerName")) {
            String playerName = data.getString("PlayerName");
            tooltip.add(new TranslationTextComponent("rotp_hd.page.current_player")
                    .withStyle(TextFormatting.DARK_PURPLE)
                    .append(new StringTextComponent(" " + playerName)
                    .withStyle(TextFormatting.GOLD)));
        }

        if (world instanceof ServerWorld && validPage(stack)) {
            UUID targetUUID = getTargetUUID(stack);
            if (targetUUID != null) {
                Entity target = ((ServerWorld) world).getEntity(targetUUID);
                if (target != null) {
                    Optional<Integer> pageCount = target.getCapability(LivingUtilCapProvider.CAPABILITY)
                            .map(LivingUtilCap::getPageCount);
                    if (pageCount.isPresent() && pageCount.get() > 0) {
                        tooltip.add(new TranslationTextComponent("rotp_hd.page.count", pageCount.get())
                                .withStyle(TextFormatting.LIGHT_PURPLE));
                    }
                }
            }
        }
    }
}