package com.yaricktt.heavensdoor.client;

import com.github.standobyte.jojo.client.playeranim.PlayerAnimationHandler;
import com.yaricktt.heavensdoor.RotpHDAddon;
import com.yaricktt.heavensdoor.client.render.HeavensDoorRenderer;
import com.yaricktt.heavensdoor.client.render.entity.layerrenderer.BookLayer;
import com.yaricktt.heavensdoor.init.InitStands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;

@Mod.EventBusSubscriber(modid = RotpHDAddon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInit {

    @SubscribeEvent
    public static void onFMLClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(InitStands.HEAVENS_DOOR_STAND.getEntityType(), HeavensDoorRenderer::new);
        event.enqueueWork(() -> {
            Minecraft mc = event.getMinecraftSupplier().get();
            ClientEventHandler.init(mc);
            Map<String, PlayerRenderer> skinMap = mc.getEntityRenderDispatcher().getSkinMap();
            addLayers(skinMap.get("default"), false);
            addLayers(skinMap.get("slim"), true);
            mc.getEntityRenderDispatcher().renderers.values().forEach(ClientInit::addLayersToEntities);
            PlayerAnimationHandler.initAnimator();
        });
    }

    private static void addLayers(PlayerRenderer renderer, boolean slim) {
        addLivingLayers(renderer);
        addBipedLayers(renderer);
    }

    private static <T extends LivingEntity, M extends BipedModel<T>> void addLayersToEntities(EntityRenderer<?> renderer) {
        if (renderer instanceof LivingRenderer<?, ?>) {
            addLivingLayers((LivingRenderer<T, ?>) renderer);
            if (((LivingRenderer<?, ?>) renderer).getModel() instanceof BipedModel<?>) {
                addBipedLayers((LivingRenderer<T, M>) renderer);
            }
        }
    }

    private static <T extends LivingEntity, M extends EntityModel<T>> void addLivingLayers(LivingRenderer<T, M> renderer) {
        renderer.addLayer(new BookLayer<>(renderer));
    }

    private static <T extends LivingEntity, M extends BipedModel<T>> void addBipedLayers(LivingRenderer<T, M> renderer) {
    }
}