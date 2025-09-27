package com.yaricktt.heavensdoor.effects;

import com.github.standobyte.jojo.potion.UncurableEffect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

public class ForgotSwimEffect extends UncurableEffect {
    public ForgotSwimEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Эффект активен каждый тик
    }

    @Override
    public boolean shouldRender(EffectInstance effect) {
        return false;
    }

    @Override
    public boolean shouldRenderHUD(EffectInstance effect) {
        return false;
    }
}