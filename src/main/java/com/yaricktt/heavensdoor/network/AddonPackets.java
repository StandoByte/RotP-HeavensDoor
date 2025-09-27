package com.yaricktt.heavensdoor.network;

import com.yaricktt.heavensdoor.RotpHDAddon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AddonPackets {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RotpHDAddon.MOD_ID, "main_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int messageId = 0;

    public static void register() {
        registerMessage(SyncLivingUtilCapPacket.class, SyncLivingUtilCapPacket::encode, SyncLivingUtilCapPacket::decode, SyncLivingUtilCapPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(UpdatePlayerListPacket.class, UpdatePlayerListPacket::encode, UpdatePlayerListPacket::decode, UpdatePlayerListPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registerMessage(SyncEntityVelocityPacket.class, SyncEntityVelocityPacket::encode, SyncEntityVelocityPacket::decode, SyncEntityVelocityPacket::handle,
                Optional.empty());

        registerMessage(TriggerFlyBackwardsPacket.class, TriggerFlyBackwardsPacket::encode, TriggerFlyBackwardsPacket::decode, TriggerFlyBackwardsPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(TriggerApplyBlindnessPacket.class, TriggerApplyBlindnessPacket::encode, TriggerApplyBlindnessPacket::decode, TriggerApplyBlindnessPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(TriggerApplyKYSPacket.class, TriggerApplyKYSPacket::encode, TriggerApplyKYSPacket::decode, TriggerApplyKYSPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(TriggerFreezePacket.class, TriggerFreezePacket::encode, TriggerFreezePacket::decode, TriggerFreezePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(TriggerBookPacket.class, TriggerBookPacket::encode, TriggerBookPacket::decode, TriggerBookPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(TriggerChangeBehaviorPacket.class, TriggerChangeBehaviorPacket::encode, TriggerChangeBehaviorPacket::decode, TriggerChangeBehaviorPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(RequestPlayerListPacket.class, RequestPlayerListPacket::encode, RequestPlayerListPacket::decode, RequestPlayerListPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage(SetCannotAttackTargetPacket.class, SetCannotAttackTargetPacket::encode, SetCannotAttackTargetPacket::decode, SetCannotAttackTargetPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private static <MSG> void registerMessage(Class<MSG> messageType,
                                              BiConsumer<MSG, net.minecraft.network.PacketBuffer> encoder,
                                              Function<net.minecraft.network.PacketBuffer, MSG> decoder,
                                              BiConsumer<MSG, Supplier<net.minecraftforge.fml.network.NetworkEvent.Context>> messageConsumer,
                                              Optional<NetworkDirection> direction) {
        if (messageId > 127) {
            throw new IllegalStateException("Too many packets (> 127) registered for a single channel!");
        }

        INSTANCE.registerMessage(messageId++, messageType, encoder, decoder, messageConsumer, direction);
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayerEntity player) {
        if (!(player instanceof FakePlayer)) {
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }

    public static <MSG> void sendToTrackingEntity(MSG message, Entity entity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }
}