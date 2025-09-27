package com.yaricktt.heavensdoor.capability.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class LivingUtilCapProvider implements ICapabilitySerializable<CompoundNBT> {

    @CapabilityInject(LivingUtilCap.class)
    public static Capability<LivingUtilCap> CAPABILITY = null;

    private final LivingUtilCap instance = new LivingUtilCap();
    private final LazyOptional<LivingUtilCap> optional = LazyOptional.of(() -> instance);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return optional.orElse(new LivingUtilCap()).toNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        optional.ifPresent(cap -> cap.fromNBT(nbt));
    }

    public void invalidate() {
        optional.invalidate();
    }
}