package com.yaricktt.heavensdoor.action.punch;

import com.github.standobyte.jojo.action.stand.StandEntityMeleeBarrage;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import net.minecraft.entity.Entity;

public class HeavensDoorBarrage extends StandEntityMeleeBarrage {
    public HeavensDoorBarrage(Builder builder) {
        super(builder);

    }
    @Override
    public BarrageEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        BarrageEntityPunch punch = new BarrageEntityPunch(stand, target, dmgSource).barrageHits(stand, stand.barrageHits);
        punch.impactSound(hitSound);
        punch.damage(0.05F);
        return punch;
    }
}
