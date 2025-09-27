package com.yaricktt.heavensdoor.mixin;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

import static com.yaricktt.heavensdoor.GameplayHandler.sendMessage;

@Mixin(value = EntityStandType.class, remap = false)
public abstract class StandEntityTypeMixin {

    @Inject(
            method = "summon(Lnet/minecraft/entity/LivingEntity;Lcom/github/standobyte/jojo/power/impl/stand/IStandPower;Ljava/util/function/Consumer;ZZ)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void preventSummonWithNausea(
            LivingEntity user,
            IStandPower standPower,
            Consumer<StandEntity> beforeTheSummon,
            boolean withoutNameVoiceLine,
            boolean addToWorld,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (user != null && user.hasEffect(InitEffects.FORGOT_STAND.get())) {
            sendMessage((PlayerEntity) user, "message.rotp_hd.forgot_stand");
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
