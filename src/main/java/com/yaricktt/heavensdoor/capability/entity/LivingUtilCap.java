package com.yaricktt.heavensdoor.capability.entity;

import com.yaricktt.heavensdoor.init.InitEffects;
import com.yaricktt.heavensdoor.network.AddonPackets;
import com.yaricktt.heavensdoor.network.SyncLivingUtilCapPacket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.UUID;

public class LivingUtilCap {
    private final LivingEntity entity;
    private UUID ownerUUID;
    private boolean cannotAttackOwner;
    private UUID PlayerTargetUUID;
    private int pageCount = 0;
    private long liveTime = 0;

    private BehaviorState originalBehavior;
    private BehaviorState currentBehavior;

    public LivingUtilCap(LivingEntity entity) {
        this.entity = entity;
    }

    public static final double HEALTH_MODIFIER_PAGE = 0.50;
    public static final double GRAVITY_MODIFIER_PAGE = 0.001;
    public static final int MAX_PAGES = 10;

    public enum BehaviorState {
        AGGRESSIVE, PEACEFUL;
        public BehaviorState next() {
            switch (this) {
                case AGGRESSIVE: return PEACEFUL;
                case PEACEFUL: default: return AGGRESSIVE;
            }
        }
    }

    public boolean hasBehaviorBeenModified() {
        return this.originalBehavior != null && this.currentBehavior != this.originalBehavior;
    }

    @Nullable public BehaviorState getCurrentBehavior() {
        return this.currentBehavior;
    }

    @Nullable public BehaviorState getOriginalBehavior() {
        return this.originalBehavior;
    }

    public void cycleBehavior() {
        if (this.currentBehavior != null) {
            this.currentBehavior = this.currentBehavior.next();
        }
    }

    public void initializeBehavior(MobEntity mob) {
        if (this.originalBehavior != null) return;
        if (mob instanceof IMob || mob instanceof MonsterEntity) {
            this.originalBehavior = BehaviorState.AGGRESSIVE;
        } else {
            this.originalBehavior = BehaviorState.PEACEFUL;
        }
        this.currentBehavior = this.originalBehavior;
    }

    public void updatePageCount(int newPageCount, LivingEntity target) {
        newPageCount = Math.max(0, Math.min(newPageCount, MAX_PAGES));
        int oldPageCount = this.getPageCount();
        if (newPageCount == oldPageCount) return;
        int delta = newPageCount - oldPageCount;

        double healthChange = delta * HEALTH_MODIFIER_PAGE;
        double gravityChange = delta * GRAVITY_MODIFIER_PAGE;
        double newMaxHealth = Math.max(target.getAttribute(Attributes.MAX_HEALTH).getBaseValue() - healthChange, 1.0);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
        if (target.getHealth() > target.getMaxHealth()) {
            target.setHealth(target.getMaxHealth());
        }

        double newGravity = target.getAttribute(ForgeMod.ENTITY_GRAVITY.get()).getBaseValue() - gravityChange;
        target.getAttribute(ForgeMod.ENTITY_GRAVITY.get()).setBaseValue(newGravity);
        this.setPageCount(newPageCount);
        AddonPackets.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                new SyncLivingUtilCapPacket(target.getId(), this.liveTime, this.getPageCount())
        );
    }

    public void addForgotEffect(LivingEntity target) {
        int pages = this.getPageCount();
        if (pages >= 1) target.addEffect(new EffectInstance(InitEffects.FORGOT_SECOND_HAND.get(), 3600, 0, false, false));
        if (pages >= 2) target.addEffect(new EffectInstance(InitEffects.FORGOT_SWIMMING.get(), 3600, 0, false, false));
        if (pages >= 3) target.addEffect(new EffectInstance(InitEffects.FORGOT_RUNNING.get(), 3600, 0, false, false));
        if (pages >= 4) target.addEffect(new EffectInstance(InitEffects.FORGOT_JUMPING.get(), 3600, 0, false, false));
        if (pages >= 5) target.addEffect(new EffectInstance(InitEffects.FORGOT_SHIFTING.get(), 3600, 0, false, false));
        if (pages >= 6) target.addEffect(new EffectInstance(InitEffects.FORGOT_ATTACK.get(), 3600, 0, false, false));
        if (pages >= 7) target.addEffect(new EffectInstance(InitEffects.FORGOT_CRAFTING.get(), 3600, 0, false, false));
        if (pages >= 8) target.addEffect(new EffectInstance(InitEffects.FORGOT_CONTAINERS.get(), 3600, 0, false, false));
        if (pages >= 9) target.addEffect(new EffectInstance(InitEffects.FORGOT_BREAKING.get(), 3600, 0, false, false));
        if (pages >= 10) target.addEffect(new EffectInstance(InitEffects.FORGOT_STAND.get(), 3600, 0, false, false));

        AddonPackets.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                new SyncLivingUtilCapPacket(target.getId(), this.liveTime, pages)
        );
    }

    public void removeEffects(LivingEntity target) {
        int pages = this.getPageCount();

        if (pages < 10) target.removeEffect(InitEffects.FORGOT_STAND.get());
        if (pages < 9) target.removeEffect(InitEffects.FORGOT_BREAKING.get());
        if (pages < 8) target.removeEffect(InitEffects.FORGOT_CONTAINERS.get());
        if (pages < 7) target.removeEffect(InitEffects.FORGOT_CRAFTING.get());
        if (pages < 6) target.removeEffect(InitEffects.FORGOT_ATTACK.get());
        if (pages < 5) target.removeEffect(InitEffects.FORGOT_SHIFTING.get());
        if (pages < 4) target.removeEffect(InitEffects.FORGOT_JUMPING.get());
        if (pages < 3) target.removeEffect(InitEffects.FORGOT_RUNNING.get());
        if (pages < 2) target.removeEffect(InitEffects.FORGOT_SWIMMING.get());
        if (pages < 1) target.removeEffect(InitEffects.FORGOT_SECOND_HAND.get());

        AddonPackets.INSTANCE.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                new SyncLivingUtilCapPacket(target.getId(), this.liveTime, pages)
        );
    }

    public int getPageCount() {
        return this.pageCount;
    }

    public void setPageCount(int count) {
        this.pageCount = count;
    }

    public boolean hasOwner() {
        return this.ownerUUID != null;
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Nullable public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public boolean hasPlayerTargetUUID() {
        return this.PlayerTargetUUID != null;
    }

    @Nullable
    public UUID getPlayerTargetUUID() {
        return this.PlayerTargetUUID;
    }

    public void setPlayerTargetUUID(@Nullable UUID uuid) {
        this.PlayerTargetUUID = uuid;
    }

    public long getLiveTime() {
        return this.liveTime;
    }

    public void setLiveTime(long liveTime) {
        this.liveTime = liveTime;
    }

    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putLong("LiveTime", this.liveTime);
        nbt.putInt("PageCount", this.pageCount);
        nbt.putString("OriginBehavior", this.originalBehavior != null ? this.originalBehavior.name() : "");
        nbt.putString("CurrentBehavior", this.currentBehavior != null ? this.currentBehavior.name() : "");
        nbt.putUUID("HDOwner", ownerUUID != null ? ownerUUID : UUID.fromString("9e45bde6-8cf9-461e-ade7-4b54acb1a153"));
        nbt.putUUID("PlayerTarget", this.PlayerTargetUUID != null ? this.PlayerTargetUUID : UUID.fromString("9e45bde6-8cf9-461e-ade7-4b54acb1a153"));
        nbt.putUUID("ForbiddenAttackTarget", this.PlayerTargetUUID != null ? this.PlayerTargetUUID : UUID.fromString("9e45bde6-8cf9-461e-ade7-4b54acb1a153"));
        nbt.putBoolean("CannotAttackOwner", this.hasOwner() && this.cannotAttackOwner);
        return nbt;
    }

    public void fromNBT(CompoundNBT nbt) {
        this.liveTime = nbt.getLong("LiveTime");
        this.pageCount = nbt.getInt("PageCount");
        if (!nbt.getString("OriginalBehavior").isEmpty()) {
            this.originalBehavior = BehaviorState.valueOf(nbt.getString("OriginalBehavior"));
        } else {
            this.originalBehavior = null;
        }

        if (!nbt.getString("CurrentBehavior").isEmpty()) {
            this.currentBehavior = BehaviorState.valueOf(nbt.getString("CurrentBehavior"));
        } else {
            this.currentBehavior = this.originalBehavior;
        }

        if (nbt.hasUUID("HDOwner")) {
            this.ownerUUID = nbt.getUUID("HDOwner");
        } else {
            this.ownerUUID = null;
        }

        if (nbt.hasUUID("ForbiddenAttackTarget")) {
            this.PlayerTargetUUID = nbt.getUUID("ForbiddenAttackTarget");
        } else {
            this.PlayerTargetUUID = null;
        }
        this.cannotAttackOwner = this.hasOwner() && nbt.getBoolean("CannotAttackOwner");
    }
}