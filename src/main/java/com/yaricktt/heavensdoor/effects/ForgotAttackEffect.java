package com.yaricktt.heavensdoor.effects;

import com.github.standobyte.jojo.potion.UncurableEffect;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;

public class ForgotAttackEffect extends UncurableEffect {

    public ForgotAttackEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653b89-116e-49dc-9b6b-9971489b5be6", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
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