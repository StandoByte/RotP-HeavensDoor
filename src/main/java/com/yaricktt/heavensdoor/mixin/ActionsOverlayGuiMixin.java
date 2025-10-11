package com.yaricktt.heavensdoor.mixin;

import com.github.standobyte.jojo.client.ui.actionshud.ActionsModeConfig;
import com.github.standobyte.jojo.client.ui.actionshud.ActionsOverlayGui;
import com.github.standobyte.jojo.power.IPower;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ActionsOverlayGui.class)
public class ActionsOverlayGuiMixin {

    @Inject(method = "renderLeapIcon", at = @At("HEAD"), cancellable = true, remap = false)
    protected void rotp_$$1renderLeapIcon(MatrixStack matrixStack, ActionsModeConfig<?> mode, int screenWidth, int screenHeight, CallbackInfo ci) {
        IPower<?, ?> power = mode.getPower();
        LivingEntity user = power.getUser();
        if (power.isLeapUnlocked()) {
            EffectInstance book = user.getEffect(InitEffects.BOOK.get());
            EffectInstance forgotJump = user.getEffect(InitEffects.FORGOT_JUMPING.get());
            if (book != null && book.getAmplifier() >= 1) {
                ci.cancel();
            }
            if (forgotJump != null && forgotJump.getAmplifier() >= 0) {
                ci.cancel();
            }
        }

    }
}
