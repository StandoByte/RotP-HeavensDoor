package com.yaricktt.heavensdoor.init;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.yaricktt.heavensdoor.effects.*;
import com.yaricktt.heavensdoor.RotpHDAddon;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.potion.Effect;

import static com.github.standobyte.jojo.init.ModStatusEffects.FREEZE;
import static com.github.standobyte.jojo.init.ModStatusEffects.setEffectAsTracked;

public class InitEffects {
    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create
            (ForgeRegistries.POTIONS, RotpHDAddon.MOD_ID);

    public static final RegistryObject<BookEffect> BOOK = EFFECTS.register("book_effect",
            () -> new BookEffect(EffectType.NEUTRAL, 0x000111));

    public static final RegistryObject<BlindnessEntityEffect> BLINDNESS_ENTITY_EFFECT = EFFECTS.register("blindness_entity_effect",
            () -> new BlindnessEntityEffect(EffectType.NEUTRAL, 0x000111));

    public static final RegistryObject<KYSEffect> KYS_EFFECT = EFFECTS.register("kys_effect",
            () -> new KYSEffect(EffectType.NEUTRAL, 0xFF0000));

    public static final RegistryObject<ForgotAttackEffect> FORGOT_ATTACK = EFFECTS.register("forgot_attack",
            () -> new ForgotAttackEffect(EffectType.NEUTRAL, 0xF01111));

    public static final RegistryObject<ForgotSecondHandEffect> FORGOT_SECOND_HAND = EFFECTS.register("forgot_second_hand",
            () -> new ForgotSecondHandEffect(EffectType.NEUTRAL, 0xFF0000));

    public static final RegistryObject<ForgotSwimEffect> FORGOT_SWIMMING = EFFECTS.register("forgot_swim",
            () -> new ForgotSwimEffect(EffectType.NEUTRAL, 0xFF0500));

    public static final RegistryObject<ForgotRunEffect> FORGOT_RUNNING = EFFECTS.register("forgot_run",
            () -> new ForgotRunEffect(EffectType.NEUTRAL, 0xFF0500));

    public static final RegistryObject<ForgotJumpEffect> FORGOT_JUMPING = EFFECTS.register("forgot_jump",
            () -> new ForgotJumpEffect(EffectType.NEUTRAL, 0xFF0500));

    public static final RegistryObject<ForgotBreakEffect> FORGOT_BREAKING = EFFECTS.register("forgot_break",
            () -> new ForgotBreakEffect(EffectType.NEUTRAL, 0xFF0500));

    public static final RegistryObject<ForgotCraftEffect> FORGOT_CRAFTING = EFFECTS.register("forgot_craft",
            () -> new ForgotCraftEffect(EffectType.NEUTRAL, 0xFF0500));

    public static final RegistryObject<ForgotStandEffect> FORGOT_STAND = EFFECTS.register("forgot_stand",
            () -> new ForgotStandEffect(EffectType.NEUTRAL, 0xFF0500));

    public static final RegistryObject<ForgotUseContEffect> FORGOT_CONTAINERS = EFFECTS.register("forgot_containers",
            () -> new ForgotUseContEffect(EffectType.NEUTRAL, 0xFF0500));

    public static final RegistryObject<ForgotShiftEffect> FORGOT_SHIFTING = EFFECTS.register("forgot_shift",
            () -> new ForgotShiftEffect(EffectType.NEUTRAL, 0xFF0500));


    public static void afterEffectsRegister() {
        StandEntity.addSharedEffectsFromUser(BOOK.get(), FORGOT_ATTACK.get(), BLINDNESS_ENTITY_EFFECT.get(), KYS_EFFECT.get(), FREEZE.get());
        StandEntity.addSharedEffectsFromStand(BOOK.get(), FORGOT_ATTACK.get(), BLINDNESS_ENTITY_EFFECT.get(), KYS_EFFECT.get(), FREEZE.get());
        setEffectAsTracked(BOOK.get());
        setEffectAsTracked(BLINDNESS_ENTITY_EFFECT.get());
        setEffectAsTracked(KYS_EFFECT.get());
    }

}