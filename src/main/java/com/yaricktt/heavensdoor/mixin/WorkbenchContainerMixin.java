package com.yaricktt.heavensdoor.mixin;

import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.yaricktt.heavensdoor.GameplayHandler.sendMessage;

@Mixin(WorkbenchContainer.class)
public abstract class WorkbenchContainerMixin {

    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)
    private static void onUpdateResult(int p_217066_0_, World p_217066_1_, PlayerEntity player, CraftingInventory p_217066_3_, CraftResultInventory p_217066_4_, CallbackInfo ci) {
        if (player.hasEffect(InitEffects.FORGOT_CRAFTING.get())) {
            ci.cancel();
            sendMessage(player, "message.rotp_hd.forgot_craft");
        }
    }
}
