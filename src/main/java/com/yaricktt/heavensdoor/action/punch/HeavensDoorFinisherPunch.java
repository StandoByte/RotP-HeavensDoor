package com.yaricktt.heavensdoor.action.punch;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandEntityHeavyAttack;
import com.github.standobyte.jojo.action.stand.punch.StandEntityPunch;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.entity.stand.StandPose;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.StandEntityDamageSource;
import com.yaricktt.heavensdoor.init.InitEffects;
import com.yaricktt.heavensdoor.network.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Random;

public class HeavensDoorFinisherPunch extends StandEntityHeavyAttack {
    public HeavensDoorFinisherPunch(Builder builder) {
        super(builder);
    }
    public static final StandPose FINISHER = new StandPose("finisher");

    @Override
    public ActionConditionResult checkTarget(ActionTarget target, LivingEntity user, IStandPower power) {
        Entity entityTarget = target.getEntity();
        if (entityTarget instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) entityTarget;
            if (!livingTarget.hasEffect(InitEffects.BOOK.get())) {
                return conditionMessage("no_book");
            }
            return ActionConditionResult.POSITIVE;
        }
        return ActionConditionResult.NEGATIVE_CONTINUE_HOLD;
    }

    @Override
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        if (stand.level.isClientSide) {
            if (target != null) {
                int entityId = target.getId();
                Random random = stand.level.getRandom();
                int command = random.nextInt(3);
                System.out.println("Random: " + command);
                switch (command) {
                    case 0:
                        if (target instanceof StandEntity) {
                            StandEntity targetStand = (StandEntity) target;
                            AddonPackets.INSTANCE.sendToServer(new TriggerFlyBackwardsPacket(targetStand.getUser().getId()));
                            AddonPackets.INSTANCE.sendToServer(new TriggerBookPacket(entityId, 0));
                        } else if (target instanceof LivingEntity) {
                            AddonPackets.INSTANCE.sendToServer(new TriggerFlyBackwardsPacket(entityId));
                            AddonPackets.INSTANCE.sendToServer(new TriggerBookPacket(entityId, 0));
                        }
                        break;
                    case 1:
                        AddonPackets.INSTANCE.sendToServer(new TriggerFreezePacket(entityId, 60));
                        AddonPackets.INSTANCE.sendToServer(new TriggerBookPacket(entityId, 0));
                        break;
                    case 2:
                        LivingEntity targetEntity = (LivingEntity) target;
                        if (targetEntity.hasEffect(InitEffects.BLINDNESS_ENTITY_EFFECT.get())) {
                            if (target instanceof StandEntity) {
                                StandEntity targetStand = (StandEntity) target;
                                AddonPackets.INSTANCE.sendToServer(new TriggerFlyBackwardsPacket(targetStand.getUser().getId()));
                                AddonPackets.INSTANCE.sendToServer(new TriggerBookPacket(targetStand.getUser().getId(), 0));
                            } else if (target instanceof LivingEntity) {
                                AddonPackets.INSTANCE.sendToServer(new TriggerFlyBackwardsPacket(entityId));
                                AddonPackets.INSTANCE.sendToServer(new TriggerBookPacket(entityId, 0));
                            }
                        } else {
                            AddonPackets.INSTANCE.sendToServer(new TriggerApplyBlindnessPacket(entityId, 15 * 20));
                            AddonPackets.INSTANCE.sendToServer(new TriggerBookPacket(entityId, 0));
                        }
                        break;
                }
            }
        }

        return super.punchEntity(stand, target, dmgSource)
                .addKnockback(0.05F + stand.getLastHeavyFinisherValue())
                .knockbackXRot(0.01F)
                .damage(0.5F);
    }

    @Override
    public TargetRequirement getTargetRequirement() {
        return TargetRequirement.ENTITY;
    }

}
