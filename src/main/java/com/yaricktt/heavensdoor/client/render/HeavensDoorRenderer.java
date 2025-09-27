package com.yaricktt.heavensdoor.client.render;

import com.github.standobyte.jojo.client.render.entity.model.stand.StandEntityModel;
import com.github.standobyte.jojo.client.render.entity.model.stand.StandModelRegistry;
import com.github.standobyte.jojo.client.render.entity.renderer.stand.StandEntityRenderer;
import com.yaricktt.heavensdoor.RotpHDAddon;
import com.yaricktt.heavensdoor.entity.HeavensDoorEntity;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class HeavensDoorRenderer extends StandEntityRenderer<HeavensDoorEntity, StandEntityModel<HeavensDoorEntity>> {
    
    public HeavensDoorRenderer(EntityRendererManager renderManager) {
        super(renderManager, 
                StandModelRegistry.registerModel(new ResourceLocation(RotpHDAddon.MOD_ID, "heavens_door"), HeavensDoorModel::new),
                new ResourceLocation(RotpHDAddon.MOD_ID, "textures/entity/stand/heavens_door.png"), 0);
    }
}
