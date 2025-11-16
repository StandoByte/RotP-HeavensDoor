package com.yaricktt.heavensdoor.capability.entity;

import com.yaricktt.heavensdoor.RotpHDAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RotpHDAddon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityHandler {
    public static final ResourceLocation LIVING_UTIL_CAP = new ResourceLocation("rotp_hd", "living_util");

    public CapabilityHandler() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            LivingUtilCapProvider provider = new LivingUtilCapProvider((LivingEntity)event.getObject());
            event.addCapability(LIVING_UTIL_CAP, provider);
        }

    }

    public static void registerCapabilities() {
        CapabilityManager.INSTANCE.register(LivingUtilCap.class, new LivingUtilCapStorage(), () -> new LivingUtilCap((LivingEntity)null));
    }
}
