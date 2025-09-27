package com.yaricktt.heavensdoor.mixin;

import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.yaricktt.heavensdoor.GameplayHandler.sendMessage;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    public void rotp_$$1CantJump(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        EffectInstance book = player.getEffect(InitEffects.BOOK.get());
        EffectInstance forgotJump = player.getEffect(InitEffects.FORGOT_JUMPING.get());
        if (book != null && book.getAmplifier() >= 2) {
            ci.cancel();
        }
        if (forgotJump != null && forgotJump.getAmplifier() >= 0) {
            ci.cancel();
            sendMessage(player, "message.rotp_hd.forgot_jump");
        }
    }
}
