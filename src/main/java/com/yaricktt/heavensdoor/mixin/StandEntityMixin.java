package com.yaricktt.heavensdoor.mixin;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.*;

@Mixin(StandEntity.class)
public abstract class StandEntityMixin extends Entity {

    @Shadow
    @Nullable
    public abstract IStandPower getUserPower();

    @Shadow
    @Nullable
    public abstract LivingEntity getUser();

    public StandEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onStandTick(CallbackInfo ci) {
        if (!this.level.isClientSide()) {
            IStandPower standPower = this.getUserPower();
            LivingEntity user = Objects.requireNonNull(standPower).getUser();
            if (user != null && user.hasEffect(InitEffects.FORGOT_STAND.get())) {
                standPower.getType().forceUnsummon(user, standPower);
                ci.cancel();
            }
        }
    }

    @Inject(method = "canAttack", at = @At("HEAD"), cancellable = true)
    private void onCanAttack(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (this.getUser() != null) {
            if (this.getUser().hasEffect(InitEffects.FORGOT_ATTACK.get())) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void onIsInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        boolean inBlindnessRadius = false;
        boolean ownerHasEffect = false;
        for (PlayerEntity Player : level.players()) {
            if (Player.hasEffect(InitEffects.BLINDNESS_ENTITY_EFFECT.get())) {
                double distanceSq = Player.distanceToSqr(this);
                if (distanceSq <= 10.0 * 10.0) {
                    inBlindnessRadius = true;
                    LivingEntity owner = this.getUser();
                    if (owner != null && owner.is(Player)) {
                        ownerHasEffect = true;
                    }
                }
            }
        }

        if (inBlindnessRadius) {
            LivingEntity owner = this.getUser();
            boolean isOwner = owner != null && owner.is(player);
            if (isOwner && ownerHasEffect) {
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }

            if (player.hasEffect(InitEffects.BLINDNESS_ENTITY_EFFECT.get())) {
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
        }
    }
}