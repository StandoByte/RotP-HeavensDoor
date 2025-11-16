package com.yaricktt.heavensdoor;

import com.yaricktt.heavensdoor.capability.entity.CapabilityHandler;
import com.yaricktt.heavensdoor.init.*;
import com.yaricktt.heavensdoor.network.AddonPackets;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RotpHDAddon.MOD_ID)
public class RotpHDAddon {
    public static final String MOD_ID = "rotp_hd";
    public static final Logger LOGGER = LogManager.getLogger();

    public RotpHDAddon() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        InitEntities.ENTITIES.register(modEventBus);
        InitSounds.SOUNDS.register(modEventBus);
        InitStands.ACTIONS.register(modEventBus);
        InitStands.STANDS.register(modEventBus);
        InitEffects.EFFECTS.register(modEventBus);
        InitItems.ITEMS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AddonPackets.register();
            CapabilityHandler.registerCapabilities();
            InitEffects.afterEffectsRegister();
        });
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}