package com.yaricktt.heavensdoor.init;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.stand.*;
import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.init.power.stand.EntityStandRegistryObject;
import com.github.standobyte.jojo.init.power.stand.ModStandsInit;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.yaricktt.heavensdoor.RotpHDAddon;
import com.yaricktt.heavensdoor.action.*;
import com.yaricktt.heavensdoor.action.punch.HeavensDoorBarrage;
import com.yaricktt.heavensdoor.action.punch.HeavensDoorFinisherPunch;
import com.yaricktt.heavensdoor.action.punch.Punch;
import com.yaricktt.heavensdoor.entity.HeavensDoorEntity;

import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class InitStands {
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<Action<?>> ACTIONS = DeferredRegister.create(
            (Class<Action<?>>) ((Class<?>) Action.class), RotpHDAddon.MOD_ID);
    @SuppressWarnings("unchecked")
    public static final DeferredRegister<StandType<?>> STANDS = DeferredRegister.create(
            (Class<StandType<?>>) ((Class<?>) StandType.class), RotpHDAddon.MOD_ID);
    
 // ======================================== Heaven's Door Stand ========================================

    
    public static final RegistryObject<HeavensDoorBarrage> HEAVENS_DOOR_BARRAGE = ACTIONS.register("heavens_door_barrage",
            () -> new HeavensDoorBarrage(new HeavensDoorBarrage.Builder()
                    .standSound(StandEntityAction.Phase.PERFORM, false, InitSounds.ROHAN_BARRAGE)
                    .barrageHitSound(InitSounds.HEAVENS_DOOR_PUNCH_BARRAGE)));

    public static final RegistryObject<HeavensDoorFinisherPunch> HEAVENS_DOOR_FINISHER_PUNCH = ACTIONS.register("heavens_door_finisher_punch",
            () -> new HeavensDoorFinisherPunch(new StandEntityHeavyAttack.Builder() //TODO finisher ability
                    .punchSound(InitSounds.HEAVENS_DOOR_WRITE_COMMAND)
                    .standPose(HeavensDoorFinisherPunch.FINISHER)
                    .resolveLevelToUnlock(3) // <---- don't working
                    .partsRequired(StandPart.ARMS)));

    public static final RegistryObject<Punch> HEAVENS_DOOR_PUNCH = ACTIONS.register("heavens_door_turn_into_a_book",
            () -> new Punch(new StandEntityLightAttack.Builder()
                    .punchSound(InitSounds.HEAVENS_DOOR_PUNCH_LIGHT)
                    .staminaCost(100)));

    public static final RegistryObject<RemoveBook> HEAVENS_DOOR_REMOVE_BOOK = ACTIONS.register("heavens_door_remove_book",
            () -> new RemoveBook(new StandEntityHeavyAttack.Builder()
                    .shiftVariationOf(HEAVENS_DOOR_PUNCH)
                    .shiftVariationOf(HEAVENS_DOOR_BARRAGE)
                    .setFinisherVariation(HEAVENS_DOOR_FINISHER_PUNCH)
                    .standSound(SoundEvents.VILLAGER_WORK_LIBRARIAN.delegate)
                    .partsRequired(StandPart.ARMS)));
    
    public static final RegistryObject<StandEntityAction> HEAVENS_DOOR_BLOCK = ACTIONS.register("heavens_door_block",
            () -> new StandEntityBlock());

    public static final RegistryObject<TearOutAPage> HEAVENS_DOOR_TEAR_OUT_A_PAGE = ACTIONS.register("heavens_door_tear_out_a_page",
            () -> new TearOutAPage(new StandEntityAction.Builder()
                    .standPose(TearOutAPage.TEAR_OUT_A_PAGE)
                    .standSound(SoundEvents.VILLAGER_WORK_LIBRARIAN.delegate)
                    .cooldown(30)
                    .resolveLevelToUnlock(2)
                    .holdToFire(20, false)
                    .staminaCost(100)));

    public static final RegistryObject<ReturnPages> HEAVENS_DOOR_RETURN_PAGES = ACTIONS.register("heavens_door_return_pages",
            () -> new ReturnPages(new StandEntityAction.Builder()
                    .standSound(SoundEvents.VILLAGER_WORK_LIBRARIAN.delegate)
                    .resolveLevelToUnlock(2)
                    .shiftVariationOf(HEAVENS_DOOR_TEAR_OUT_A_PAGE)));

    public static final RegistryObject<OpenBook> HEAVENS_DOOR_OPEN_BOOK = ACTIONS.register("heavens_door_open_book",
            () -> new OpenBook(new StandAction.Builder()));


    public static final EntityStandRegistryObject<EntityStandType<StandStats>, StandEntityType<HeavensDoorEntity>> HEAVENS_DOOR_STAND =
            new EntityStandRegistryObject<>("heavens_door",
                    STANDS, 
                    () -> new EntityStandType.Builder<StandStats>()
                    .color(0xFFFEC5)
                    .storyPartName(ModStandsInit.PART_4_NAME)
                    .leftClickHotbar(
                            HEAVENS_DOOR_PUNCH.get(),
                            HEAVENS_DOOR_BARRAGE.get()
                            )
                    .rightClickHotbar(
                            HEAVENS_DOOR_BLOCK.get(),
                            HEAVENS_DOOR_OPEN_BOOK.get(),
                            HEAVENS_DOOR_TEAR_OUT_A_PAGE.get()

                            )

                    .defaultStats(StandStats.class, new StandStats.Builder()
                            .power(6)
                            .speed(12)
                            .range(12, 12)
                            .durability(12)
                            .precision(9)
                            .build())
                    .addSummonShout(InitSounds.ROHAN_SUMMON)
                    .addOst(InitSounds.HEAVENS_DOOR_OST)
                    .build(),
                    
                    InitEntities.ENTITIES,
                    () -> new StandEntityType<HeavensDoorEntity>(HeavensDoorEntity::new, 0.40F, 1.25F)
                    .summonSound(InitSounds.HEAVENS_DOOR_SUMMON_SOUND)
                    .unsummonSound(InitSounds.HEAVENS_DOOR_UNSUMMON_SOUND))
            .withDefaultStandAttributes();

}
