package com.yaricktt.heavensdoor.mixin;

import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.power.IPower;
import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.yaricktt.heavensdoor.GameplayHandler.sendMessage;

@Mixin(StandAction.class)
public abstract class StandActionMixin {

    @Inject(method = "onPerform(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lcom/github/standobyte/jojo/power/IPower;Lcom/github/standobyte/jojo/action/ActionTarget;Lnet/minecraft/network/PacketBuffer;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void onPerform(World par1, LivingEntity par2, IPower power, ActionTarget par4, PacketBuffer par5, CallbackInfo ci) {
        LivingEntity user = power.getUser();
        if (user != null && user.hasEffect(InitEffects.FORGOT_STAND.get())) {
            power.stopHeldAction(false);
            sendMessage((PlayerEntity) user, "message.rotp_hd.forgot_stand");
            ci.cancel();
        }
    }
}
