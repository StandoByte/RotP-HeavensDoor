package com.yaricktt.heavensdoor.capability.entity;

import com.yaricktt.heavensdoor.RotpHDAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RotpHDAddon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityHandler {

    public static final ResourceLocation LIVING_UTIL_CAP = new ResourceLocation(RotpHDAddon.MOD_ID, "living_util");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            final LivingUtilCapProvider provider = new LivingUtilCapProvider();
            event.addCapability(LIVING_UTIL_CAP, provider);
            event.addListener(provider::invalidate);
        }
    }
}