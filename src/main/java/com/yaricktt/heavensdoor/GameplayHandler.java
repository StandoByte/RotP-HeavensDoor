package com.yaricktt.heavensdoor;

import com.github.standobyte.jojo.entity.mob.StandUserDummyEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.yaricktt.heavensdoor.capability.entity.LivingUtilCap;
import com.yaricktt.heavensdoor.capability.entity.LivingUtilCapProvider;
import com.yaricktt.heavensdoor.entity.HeavensDoorEntity;
import com.yaricktt.heavensdoor.entity.ai.goal.MeleeAttackGoalForPeaceful;
import com.yaricktt.heavensdoor.init.InitEffects;
import com.yaricktt.heavensdoor.item.PageItem;
import com.yaricktt.heavensdoor.itemtracking.OfflinePlayerCache;
import com.yaricktt.heavensdoor.itemtracking.PageTrackerMap;
import com.yaricktt.heavensdoor.network.AddonPackets;
import com.yaricktt.heavensdoor.network.SyncLivingUtilCapPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static com.yaricktt.heavensdoor.capability.entity.LivingUtilCap.BehaviorState.AGGRESSIVE;
import static com.yaricktt.heavensdoor.capability.entity.LivingUtilCap.BehaviorState.PEACEFUL;

@Mod.EventBusSubscriber(modid = RotpHDAddon.MOD_ID)
public class GameplayHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity sourceEntity = event.getSource().getEntity();
        LivingEntity victim = event.getEntityLiving();
        if (victim.level.isClientSide || !(sourceEntity instanceof LivingEntity)) {
            return;
        }

        LivingEntity attacker = (LivingEntity) sourceEntity;
        if (attacker instanceof StandEntity) {
            StandEntity stand = (StandEntity) attacker;
            LivingEntity standOwner = stand.getUser();
            if (standOwner != null || standOwner instanceof StandUserDummyEntity) {
                standOwner.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    if (cap.getPlayerTargetUUID() != null && cap.getPlayerTargetUUID().equals(victim.getUUID())) {
                        event.setCanceled(true);
                    }
                });
            }
        } else {
            attacker.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                if (cap.getPlayerTargetUUID() != null && cap.getPlayerTargetUUID().equals(victim.getUUID())) {
                    event.setCanceled(true);
                }
            });
        }
        if (attacker instanceof StandEntity) {
            StandEntity stand = (StandEntity) attacker;
            LivingEntity standOwner = stand.getUser();
            if (standOwner != null || standOwner instanceof StandUserDummyEntity) {
                if (standOwner.hasEffect(InitEffects.FORGOT_ATTACK.get())) {
                    event.setCanceled(true);
                }
            }
        } else {
            if (attacker.hasEffect(InitEffects.FORGOT_ATTACK.get())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        LivingEntity victim = event.getEntityLiving();
        if (victim.level.isClientSide) {
            return;
        }

        Entity dmgEntity = source.getEntity();
        if (dmgEntity instanceof HeavensDoorEntity) {
            StandEntity stand = (StandEntity) dmgEntity;
            LivingEntity standOwner = stand.getUser();
            if (standOwner != null || standOwner instanceof StandUserDummyEntity) {
                victim.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent((LivingUtilCap cap) -> {
                    if (!cap.hasOwner() || !cap.getOwnerUUID().equals(standOwner.getUUID())) {
                        cap.setOwnerUUID(standOwner.getUUID());
                    }

                    AddonPackets.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> victim),
                            new SyncLivingUtilCapPacket(victim.getId(), cap.getLiveTime(), cap.getPageCount())
                    );
                });

                int effectLevel;
                int effectTime;
                IStandPower powerStand = IStandPower.getPlayerStandPower((PlayerEntity) standOwner);
                if (powerStand.getResolveLevel() > 1) {
                    if (IStandPower.getStandPowerOptional(victim).map(IStandPower::hasPower).orElse(false)
                            || INonStandPower.getNonStandPowerOptional(victim).map(INonStandPower::hasPower).orElse(false)) {
                        if (victim.getHealth() <= victim.getMaxHealth() * 0.65F) {
                            effectTime = 400;
                            effectLevel = 1;
                        } else {
                            effectTime = 300;
                            effectLevel = 0;
                        }
                    } else {
                        effectTime = 600;
                        effectLevel = 2;
                    }
                } else {
                    effectLevel = 0;
                    effectTime = 200;
                }

                victim.addEffect(new EffectInstance(InitEffects.BOOK.get(), effectTime, effectLevel, false, false));
                // int effectLevel = IStandPower.getStandPowerOptional(victim).map(IStandPower::hasPower).orElse(false) || INonStandPower.getNonStandPowerOptional(victim).map(INonStandPower::hasPower).orElse(false) ? 1 : 2;
                // victim.addEffect(new EffectInstance(InitEffects.BOOK.get(), 600, effectLevel, false, false));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity tickingEntity = event.getEntityLiving();
        if (tickingEntity.level.isClientSide || tickingEntity.tickCount % 20 != 0) return;
        tickingEntity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent((LivingUtilCap cap) -> {
            if (cap.hasOwner()) {
                PlayerEntity owner = tickingEntity.level.getPlayerByUUID(cap.getOwnerUUID());
                if (owner == null || !owner.isAlive()) {
                    cap.setOwnerUUID(null);
                    if (tickingEntity instanceof LivingEntity) {
                        if (tickingEntity.hasEffect(InitEffects.BOOK.get())) {
                            tickingEntity.removeEffect(InitEffects.BOOK.get());
                        }
                        if (tickingEntity.hasEffect(InitEffects.KYS_EFFECT.get())) {
                            tickingEntity.removeEffect(InitEffects.KYS_EFFECT.get());
                        }
                    }
                }
            }

            if (cap.hasPlayerTargetUUID()) {
                PlayerEntity ptarget = tickingEntity.level.getPlayerByUUID(cap.getPlayerTargetUUID());
                if (ptarget == null || !ptarget.isAlive() || !tickingEntity.isAlive()) {
                    cap.setPlayerTargetUUID(null);
                }
            }
        });
    }

    private static final Map<UUID, Boolean> entityInvisibility = new HashMap<>();

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        Minecraft mc = Minecraft.getInstance();
        LivingEntity viewer = mc.player;
        if (viewer == null) return;
        if (viewer.hasEffect(InitEffects.BLINDNESS_ENTITY_EFFECT.get())) {
            LivingEntity target = event.getEntity();
            if (target == viewer) return;

            Vector3d viewerPos = viewer.position();
            Vector3d targetPos = target.position();
            double distance = viewerPos.distanceTo(targetPos);

            if (distance < 10.0 && !(target instanceof StandEntity)) {
                if (!entityInvisibility.containsKey(target.getUUID())) {
                    entityInvisibility.put(target.getUUID(), target.isInvisible());
                }
                target.setInvisible(true);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity target = event.getEntity();
        UUID targetUUID = target.getUUID();
        if (entityInvisibility.containsKey(targetUUID)) {
            target.setInvisible(entityInvisibility.get(targetUUID));
            entityInvisibility.remove(targetUUID);
        }
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            if (player.hasEffect(InitEffects.BOOK.get())) {
                Vector3d motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, 0.1, motion.z);
            }
            if (player.hasEffect(InitEffects.FORGOT_JUMPING.get())) {
                Vector3d motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, 0, motion.z);
                sendMessage(player, "message.rotp_hd.forgot_jump");
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        if (player.hasEffect(InitEffects.FORGOT_SECOND_HAND.get())) {
            if (event.getHand() == Hand.OFF_HAND && !player.getOffhandItem().isEmpty()) {
                event.setCanceled(true);
                sendMessage(player, "message.rotp_hd.forgot_second_hand");
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        if (player.hasEffect(InitEffects.FORGOT_SECOND_HAND.get())) {
            if (event.getHand() == Hand.OFF_HAND && !player.getOffhandItem().isEmpty()) {
                event.setCanceled(true);
                sendMessage(player, "message.rotp_hd.forgot_second_hand");
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        PlayerEntity player = event.getPlayer();
        if (player.hasEffect(InitEffects.FORGOT_SECOND_HAND.get())) {
            if (event.getHand() == Hand.OFF_HAND && !player.getOffhandItem().isEmpty()) {
                event.setCanceled(true);
                sendMessage(player, "message.rotp_hd.forgot_second_hand");
            }
        }
    }

    public static void sendMessage(PlayerEntity player, String translationKey) {
        player.displayClientMessage(new TranslationTextComponent(translationKey), true);
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (!entity.level.isClientSide) {
            entity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                long newTime = cap.getLiveTime() + 1;
                cap.setLiveTime(newTime);
                if (entity.hasEffect(InitEffects.BOOK.get())) {
                    AddonPackets.INSTANCE.send(
                            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                            new SyncLivingUtilCapPacket(entity.getId(), cap.getLiveTime(), cap.getPageCount())
                    );
                }
            });
        }

        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            ModifiableAttributeInstance swimSpeed = player.getAttribute(ForgeMod.SWIM_SPEED.get());
            if (swimSpeed != null) {
                if (player.hasEffect(InitEffects.FORGOT_SWIMMING.get())) {
                    if (player.isInWater()) {
                        if (player.isSprinting()) {
                            player.setSprinting(false);
                            swimSpeed.setBaseValue(0.5);
                            sendMessage(player, "message.rotp_hd.forgot_swim");
                        } else {
                            swimSpeed.setBaseValue(0.9);
                        }
                    }
                } else {
                    swimSpeed.setBaseValue(1.0);
                }
            }
        }

        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            ModifiableAttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeed != null && player.hasEffect(InitEffects.FORGOT_RUNNING.get())) {
                if (player.isSprinting()) {
                    player.setSprinting(false);
                    movementSpeed.setBaseValue(0.08);
                    sendMessage(player, "message.rotp_hd.forgot_run");
                } else {
                    movementSpeed.setBaseValue(0.1000000049011612);
                }
            }
        }


    }

    private static boolean wasShiftPressed = false;
    private static boolean wasJumpPressed = false;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null && player.hasEffect(InitEffects.FORGOT_SHIFTING.get())) {
            if (Minecraft.getInstance().options.keyShift.isDown()) {
                Minecraft.getInstance().options.keyShift.setDown(false);
                if (!wasShiftPressed) {
                    wasShiftPressed = true;
                    player.setShiftKeyDown(false);
                    sendMessage(player, "message.rotp_hd.forgot_shift");
                }
            } else {
                wasShiftPressed = false;
            }
        }
        //if (player != null && player.hasEffect(InitEffects.FORGOT_JUMPING.get()) && !player.abilities.instabuild) {
        //    if (Minecraft.getInstance().options.keyJump.isDown()) {
        //        Minecraft.getInstance().options.keyJump.setDown(false);
        //        if (!wasJumpPressed) {
        //            wasJumpPressed = true;
        //            player.setJumping(false);
        //            sendMessage(player, "message.rotp_hd.forgot_jump");
        //        }
        //    } else {
        //        wasJumpPressed = false;
        //    }
        //}
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.level.isClientSide && event.player.hasEffect(InitEffects.FORGOT_SHIFTING.get()) && event.player.isCrouching()) {
            event.player.setSprinting(false);
            event.player.setShiftKeyDown(false);
        }
    }

    @SubscribeEvent
    public static void onBlockStartBreak(PlayerInteractEvent.LeftClickBlock event) {
        PlayerEntity player = event.getPlayer();
        if (player.hasEffect(InitEffects.FORGOT_BREAKING.get())) {
            sendMessage(player, "message.rotp_hd.forgot_break");
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        if (explosion.getExploder() instanceof CreeperEntity) {
            CreeperEntity creeper = (CreeperEntity) explosion.getExploder();
            Collection<Effect> effects = new ArrayList<>(creeper.getActiveEffectsMap().keySet());
            effects.forEach(effect -> {
                if (effect == InitEffects.FORGOT_ATTACK.get() || effect == InitEffects.FORGOT_SECOND_HAND.get() ||
                        effect == InitEffects.FORGOT_BREAKING.get() || effect == InitEffects.FORGOT_SWIMMING.get() ||
                        effect == InitEffects.FORGOT_SHIFTING.get() || effect == InitEffects.FORGOT_STAND.get() ||
                        effect == InitEffects.FORGOT_CONTAINERS.get() || effect == InitEffects.BOOK.get() ||
                        effect == InitEffects.FORGOT_JUMPING.get() ||
                        effect == InitEffects.FORGOT_CRAFTING.get() || effect == InitEffects.FORGOT_RUNNING.get() ||
                        effect == InitEffects.KYS_EFFECT.get() || effect == InitEffects.BLINDNESS_ENTITY_EFFECT.get()) {
                    creeper.removeEffect(effect);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.level.isClientSide() || player.getServer() == null) return;
        player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
            int pageCount = cap.getPageCount();
            if (pageCount > 0) {
                OfflinePlayerCache.get(player.getServer()).cachePlayerData(player.getUUID(), pageCount);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        if (player.level.isClientSide() || player.getServer() == null) return;
        OfflinePlayerCache cache = OfflinePlayerCache.get(player.getServer());
        CompoundNBT cachedData = cache.retrieveAndClearData(player.getUUID());
        if (cachedData != null) {
            int cachedPageCount = cachedData.getInt("PageCount");
            player.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                cap.updatePageCount(cachedPageCount, player);
            });
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity deadEntity = event.getEntityLiving();
        if (deadEntity.level.isClientSide()) return;
        MinecraftServer server = deadEntity.getServer();
        if (server == null) return;

        final UUID deadEntityUUID = deadEntity.getUUID();

        if (!(deadEntity instanceof PlayerEntity)) {
            PageTrackerMap.get(server).invalidateTarget(deadEntityUUID);
        }

        final ServerWorld overworld = server.overworld();
        server.getPlayerList().getPlayers().forEach(player -> {
            for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
                ItemStack stack = player.inventory.getItem(i);
                if (stack.getItem() instanceof PageItem) {
                    CompoundNBT pageData = stack.getTagElement(PageItem.PLAYER_DATA_KEY);
                    if (pageData != null && pageData.hasUUID(PageItem.CREATOR_UUID_KEY)) {
                        if (deadEntityUUID.equals(pageData.getUUID(PageItem.CREATOR_UUID_KEY))) {
                            ((PageItem) stack.getItem()).restorePageToOwner(stack, overworld);
                            if (stack.isEmpty()) {
                                player.inventory.setItem(i, ItemStack.EMPTY);
                            }
                        }
                    }
                }
            }
        });

        for (ServerWorld world : server.getAllLevels()) {
            world.getEntities().forEach(entity -> {
                ItemStack stack = ItemStack.EMPTY;
                if (entity instanceof ItemEntity) {
                    stack = ((ItemEntity) entity).getItem();
                } else if (entity instanceof ItemFrameEntity) {
                    stack = ((ItemFrameEntity) entity).getItem();
                }

                if (!stack.isEmpty() && stack.getItem() instanceof PageItem) {
                    CompoundNBT pageData = stack.getTagElement(PageItem.PLAYER_DATA_KEY);
                    if (pageData != null && pageData.hasUUID(PageItem.CREATOR_UUID_KEY)) {
                        if (deadEntityUUID.equals(pageData.getUUID(PageItem.CREATOR_UUID_KEY))) {
                            ((PageItem) stack.getItem()).restorePageToOwner(stack, overworld);
                            if (stack.isEmpty()) {
                                entity.remove();
                            }
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getTickCount() % 10 != 0) return;

        for (ServerWorld world : server.getAllLevels()) {
            world.getEntities().forEach(entity -> {
                if (entity instanceof MobEntity) {
                    MobEntity mob = (MobEntity) entity;
                    LivingEntity target = mob.getTarget();
                    mob.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                        if (cap.getCurrentBehavior() == PEACEFUL && cap.hasBehaviorBeenModified()) {
                            mob.setTarget(null);
                            mob.setLastHurtByMob(null);
                            mob.setLastHurtByPlayer(null);
                            ((MobEntity) mob).targetSelector.getRunningGoals().filter(prioritizedGoal -> prioritizedGoal.getGoal() instanceof HurtByTargetGoal).findFirst().ifPresent(PrioritizedGoal::stop);
                        }
                    if (target != null && mob.hasEffect(InitEffects.BLINDNESS_ENTITY_EFFECT.get()) && mob.distanceToSqr(target) <= 10.0 * 10.0) {
                        mob.setTarget(null);
                        mob.setLastHurtByMob(null);
                        mob.setLastHurtByPlayer(null);
                        ((MobEntity) mob).targetSelector.getRunningGoals().filter(prioritizedGoal -> prioritizedGoal.getGoal() instanceof HurtByTargetGoal).findFirst().ifPresent(PrioritizedGoal::stop);
                    }
                    });
                }
            });
        }

        PageTrackerMap invalidTargetMap = PageTrackerMap.get(server);
        for (PlayerEntity player : server.getPlayerList().getPlayers()) {
            for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
                ItemStack stack = player.inventory.getItem(i);
                if (shouldPageBeRemoved(stack, invalidTargetMap, server)) {
                    if (stack.isEmpty()) {
                        player.inventory.setItem(i, ItemStack.EMPTY);
                    }
                }
            }
        }

        for (ServerWorld world : server.getAllLevels()) {
            world.getEntities().forEach(entity -> {
                if (entity instanceof ItemEntity) {
                    ItemEntity itemEntity = (ItemEntity) entity;
                    ItemStack stack = itemEntity.getItem();
                    if (shouldPageBeRemoved(stack, invalidTargetMap, server)) {
                        if (stack.isEmpty()) {
                            itemEntity.remove();
                        }
                    }
                } else if (entity instanceof ItemFrameEntity) {
                    ItemFrameEntity frame = (ItemFrameEntity) entity;
                    ItemStack stack = frame.getItem();
                    if (shouldPageBeRemoved(stack, invalidTargetMap, server)) {
                        if (stack.isEmpty()) {
                            frame.setItem(ItemStack.EMPTY);
                        }
                    }
                }
            });

            for (TileEntity te : new ArrayList<>(world.blockEntityList)) {
                if (te instanceof IInventory) {
                    IInventory inventory = (IInventory) te;
                    for (int i = 0; i < inventory.getContainerSize(); ++i) {
                        ItemStack stack = inventory.getItem(i);
                        if (shouldPageBeRemoved(stack, invalidTargetMap, server)) {
                            if(stack.isEmpty()) {
                                inventory.setItem(i, ItemStack.EMPTY);
                            }
                        }
                    }
                } else {
                    if (!(te instanceof IItemHandler)) {
                    return;
                }
                    te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
                        for (int i = 0; i < handler.getSlots(); i++) {
                            ItemStack stack = handler.getStackInSlot(i);
                            if (stack.isEmpty() || !(stack.getItem() instanceof PageItem)) continue;
                            int originalCount = stack.getCount();
                            if (shouldPageBeRemoved(stack, invalidTargetMap, server)) {
                                handler.extractItem(i, originalCount, false);
                            }
                        }
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getPlayer().level.isClientSide()) return;
        MinecraftServer server = event.getPlayer().getServer();
        if (server == null) return;

        PageTrackerMap invalidMap = PageTrackerMap.get(server);
        Container container = event.getContainer();

        for (int i = 0; i < container.slots.size(); ++i) {
            ItemStack stack = container.slots.get(i).getItem();
            if (shouldPageBeRemoved(stack, invalidMap, server)) {
                if (stack.isEmpty()) {
                    container.slots.get(i).set(ItemStack.EMPTY);
                }
            }
        }
    }

    private static boolean isPageInvalid(ItemStack stack, PageTrackerMap invalidMap, MinecraftServer server) {
        UUID targetUUID = PageItem.getTargetUUID(stack);
        if (targetUUID == null) return true;

        if (invalidMap.isInvalidated(targetUUID)) {
            return true;
        }

        Entity targetEntity = null;
        for (ServerWorld world : server.getAllLevels()) {
            Entity e = world.getEntity(targetUUID);
            if (e != null) {
                targetEntity = e;
                break;
            }
        }

        if (targetEntity instanceof LivingEntity) {
            return ((LivingEntity) targetEntity).getCapability(LivingUtilCapProvider.CAPABILITY)
                    .map(cap -> cap.getPageCount() <= 0)
                    .orElse(true);
        } else if (targetEntity != null) {
            return true;
        } else {
            boolean isPlayerInCache = OfflinePlayerCache.get(server).hasDataFor(targetUUID);
            if (isPlayerInCache) return false;

            return server.getProfileCache().get(targetUUID) == null;
        }
    }

    private static boolean shouldPageBeRemoved(ItemStack stack, PageTrackerMap invalidMap, MinecraftServer server) {
        if (!(stack.getItem() instanceof PageItem)) {
            return false;
        }

        CompoundNBT pageData = stack.getTagElement(PageItem.PLAYER_DATA_KEY);
        if (pageData == null || !pageData.hasUUID(PageItem.TARGET_UUID_KEY)) {
            return false;
        }

        if (isPageInvalid(stack, invalidMap, server)) {
            stack.setCount(0);
            return true;
        }

        if (!pageData.hasUUID(PageItem.CREATOR_UUID_KEY)) {
            return false;
        }
        UUID creatorUUID = pageData.getUUID(PageItem.CREATOR_UUID_KEY);

        Entity creator = null;
        for (ServerWorld world : server.getAllLevels()) {
            Entity e = world.getEntity(creatorUUID);
            if (e != null) {
                creator = e;
                break;
            }
        }

        boolean creatorIsInvalid = false;
        if (creator == null) {
            if (server.getProfileCache().get(creatorUUID) == null) {
                creatorIsInvalid = true;
            }
        } else if (!creator.isAlive()) {
            creatorIsInvalid = true;
        } else if (creator instanceof LivingEntity) {
            LivingEntity livingCreator = (LivingEntity) creator;
            if (livingCreator.getHealth() <= livingCreator.getMaxHealth() * 0.25F) {
                if (!(livingCreator instanceof PlayerEntity) || !((PlayerEntity) livingCreator).abilities.instabuild) {
                    creatorIsInvalid = true;
                }
            }
        }

        if (creatorIsInvalid) {
            UUID targetUUID = PageItem.getTargetUUID(stack);
            if (targetUUID == null) {
                stack.setCount(0);
                return true;
            }

            Entity targetEntity = null;
            for (ServerWorld world : server.getAllLevels()) {
                Entity e = world.getEntity(targetUUID);
                if (e != null) {
                    targetEntity = e;
                    break;
                }
            }

            if (targetEntity instanceof LivingEntity) {
                ((PageItem) stack.getItem()).restorePageToOwner(stack, (ServerWorld) targetEntity.level);
                return true;
            } else {
                OfflinePlayerCache cache = OfflinePlayerCache.get(server);
                if (cache.hasDataFor(targetUUID)) {
                    boolean success = cache.decrementPageCount(targetUUID, stack.getCount());
                    if (success) {
                        stack.setCount(0);
                        return true;
                    }
                } else {
                    stack.setCount(0);
                    return true;
                }
            }
        }

        return false;
    }
}