package com.yaricktt.heavensdoor.mixin;

import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

import static com.yaricktt.heavensdoor.GameplayHandler.sendMessage;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "openMenu", at = @At("HEAD"), cancellable = true)
    private void onOpenMenu(INamedContainerProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.hasEffect(InitEffects.FORGOT_CONTAINERS.get()) && provider != null) {
            cir.setReturnValue(OptionalInt.empty());
            cir.cancel();
            sendMessage(player, "message.rotp_hd.forgot_use_containers");
        }
    }
}
