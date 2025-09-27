package com.yaricktt.heavensdoor.effects;

import com.github.standobyte.jojo.potion.UncurableEffect;
import net.minecraftforge.fml.common.Mod;
import com.yaricktt.heavensdoor.RotpHDAddon;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.potion.EffectType;
import net.minecraftforge.common.ForgeMod;

@Mod.EventBusSubscriber(modid = RotpHDAddon.MOD_ID)

public class BookEffect extends UncurableEffect {
    public BookEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653b89-116e-49dc-9b6b-9971489b5be5", -0.20, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, "e4d278d8-a38b-434f-9c65-20c944abcff9", -0.20, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "e30ee41c-6ea2-468c-99ab-fd0a7d6be8c3", -0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.JUMP_STRENGTH, "0e11e63c-0b91-4f03-b8a0-2bfa8b71b507", -0.20, AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(ForgeMod.SWIM_SPEED.get(), "34dcb563-6759-4a2b-9dd8-ad2dd7e70404", -0.20, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}