package com.yaricktt.heavensdoor.action.punch;

import com.github.standobyte.jojo.action.stand.StandEntityLightAttack;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import com.yaricktt.heavensdoor.init.InitEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;

public class Punch extends StandEntityLightAttack {
        public Punch(StandEntityLightAttack.Builder builder){
                super(builder);
        }

        @Override
        public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return super.punchEntity(stand, target, dmgSource)
                .addKnockback(0.05F)
                .knockbackXRot(0.01F)
                .damage(0.3F);
        }
}