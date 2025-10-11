package com.yaricktt.heavensdoor.mixin;

import com.github.standobyte.jojo.power.impl.PowerBaseImpl;
import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowerBaseImpl.class)
public class PowerBaseImplMixin {

    @Shadow
    @Final
    protected LivingEntity user;

    @Inject(method = "canLeap", at = @At("HEAD"), cancellable = true, remap = false)
    public void rotp_$$1canLeap(CallbackInfoReturnable<Boolean> cir) {
        EffectInstance book = user.getEffect(InitEffects.BOOK.get());
        EffectInstance forgotJump = user.getEffect(InitEffects.FORGOT_JUMPING.get());
        if (book != null && book.getAmplifier() >= 1) {
            cir.setReturnValue(false);
        }
        if (forgotJump != null && forgotJump.getAmplifier() >= 0) {
            cir.setReturnValue(false);
        }
    }
}
