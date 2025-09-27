package com.yaricktt.heavensdoor.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.util.mc.OstSoundList;
import com.yaricktt.heavensdoor.RotpHDAddon;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(
            ForgeRegistries.SOUND_EVENTS, RotpHDAddon.MOD_ID); // TODO sounds.json
    
    public static final RegistryObject<SoundEvent> ROHAN_SUMMON = SOUNDS.register("rohan_summon",
            () -> new SoundEvent(new ResourceLocation(RotpHDAddon.MOD_ID, "rohan_summon")));

    public static final Supplier<SoundEvent> HEAVENS_DOOR_SUMMON_SOUND = ModSounds.STAND_SUMMON_DEFAULT;
    
    public static final Supplier<SoundEvent> HEAVENS_DOOR_UNSUMMON_SOUND = ModSounds.STAND_UNSUMMON_DEFAULT;
    
    public static final Supplier<SoundEvent> HEAVENS_DOOR_PUNCH_LIGHT = SOUNDS.register("rohan_light_attack",
            () -> new SoundEvent(new ResourceLocation(RotpHDAddon.MOD_ID, "rohan_light_attack")));

    public static final Supplier<SoundEvent> HEAVENS_DOOR_PUNCH_BARRAGE = ModSounds.STAND_PUNCH_LIGHT;
    
    public static final Supplier<SoundEvent> ROHAN_BARRAGE = SOUNDS.register("rohan_barrage",
            () -> new SoundEvent(new ResourceLocation(RotpHDAddon.MOD_ID, "rohan_barrage")));

    public static final OstSoundList HEAVENS_DOOR_OST = new OstSoundList(
            new ResourceLocation(RotpHDAddon.MOD_ID, "heavens_door_ost"), SOUNDS);

    public static final Supplier<SoundEvent> HEAVENS_DOOR_WRITE_COMMAND = SOUNDS.register("heavens_door_write_command",
            () -> new SoundEvent(new ResourceLocation(RotpHDAddon.MOD_ID, "heavens_door_write_command")));
}
