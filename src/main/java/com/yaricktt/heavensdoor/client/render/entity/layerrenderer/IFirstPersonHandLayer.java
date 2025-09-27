package com.yaricktt.heavensdoor.client.render.entity.layerrenderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.HandSide;

public interface IFirstPersonHandLayer {
    void renderHandFirstPerson(HandSide side, MatrixStack matrixStack,
                               IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player,
                               PlayerRenderer playerRenderer);


}
