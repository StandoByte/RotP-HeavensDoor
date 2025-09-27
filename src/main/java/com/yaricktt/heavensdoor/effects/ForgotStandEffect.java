package com.yaricktt.heavensdoor.effects;

import com.github.standobyte.jojo.potion.UncurableEffect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

public class ForgotStandEffect extends UncurableEffect {
    public ForgotStandEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
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