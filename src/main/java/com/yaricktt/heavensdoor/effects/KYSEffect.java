package com.yaricktt.heavensdoor.effects;

import com.github.standobyte.jojo.potion.UncurableEffect;
import com.google.common.collect.Multimap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.potion.EffectType;
import net.minecraft.util.*;
import net.minecraft.world.server.ServerWorld;

public class KYSEffect extends UncurableEffect {
    public KYSEffect(EffectType type, int liquidColor) {
        super(type, liquidColor);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level.isClientSide && entity.isAlive()) {
            boolean hasLighter = hasLighterInHand(entity);
            if (entity instanceof CreeperEntity) {
                CreeperEntity creeper = (CreeperEntity) entity;
                creeper.ignite();
                return;
            }
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                entity.swing(Hand.MAIN_HAND, true);
                player.resetAttackStrengthTicker();
                player.playSound(SoundEvents.PLAYER_ATTACK_STRONG,
                        0.8F,
                        0.8F + player.getRandom().nextFloat() * 0.4F);
            } else {
                entity.swing(Hand.MAIN_HAND, true);
                if (entity.level instanceof ServerWorld) {
                    ServerWorld world = (ServerWorld) entity.level;
                    world.broadcastEntityEvent(entity, (byte) 4);
                }
                entity.playSound(SoundEvents.PLAYER_ATTACK_STRONG,
                        0.8F,
                        0.8F + entity.getRandom().nextFloat() * 0.4F);
            }
            DamageSource selfHarmSource = new EntityDamageSource("self.harm", entity)
                    .bypassArmor()
                    .setMagic();

            float damage = calculateDamage(entity, amplifier);
            entity.hurt(selfHarmSource, damage);

            if (hasLighter) {
                playLighterSounds(entity);
            }
        }
    }

    private boolean hasLighterInHand(LivingEntity entity) {
        return entity.getMainHandItem().getItem() == Items.FLINT_AND_STEEL ||
                entity.getOffhandItem().getItem() == Items.FLINT_AND_STEEL;
    }

    private void playLighterSounds(LivingEntity entity) {
        ServerWorld world = (ServerWorld) entity.level;
        world.playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.FLINTANDSTEEL_USE,
                SoundCategory.PLAYERS,
                0.8F,
                0.9F + world.random.nextFloat() * 0.2F
        );
    }

    private float calculateDamage(LivingEntity entity, int amplifier) {
        float damage = 1.0F;

        ItemStack mainHandItem = entity.getMainHandItem();
        if (!mainHandItem.isEmpty()) {
            Multimap<Attribute, AttributeModifier> attributes = mainHandItem.getAttributeModifiers(EquipmentSlotType.MAINHAND);
            for (AttributeModifier modifier : attributes.get(Attributes.ATTACK_DAMAGE)) {
                damage += modifier.getAmount();
            }

            if (mainHandItem.getItem() instanceof ToolItem) {
                ToolItem tool = (ToolItem) mainHandItem.getItem();
                damage += tool.getAttackDamage();
            }

            damage += EnchantmentHelper.getDamageBonus(mainHandItem, entity.getMobType());
        }

        ModifiableAttributeInstance attackDamageAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttr != null) {
            damage += attackDamageAttr.getValue();
        }

        return damage * (amplifier + 1);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = 40;
        return duration % interval == 0;
    }
}