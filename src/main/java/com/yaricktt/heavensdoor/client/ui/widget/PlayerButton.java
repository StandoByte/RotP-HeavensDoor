package com.yaricktt.heavensdoor.client.ui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.yaricktt.heavensdoor.RotpHDAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

// https://github.com/dikiytechies/enhanced_pearls
public class PlayerButton extends Button {

    public static final ResourceLocation BUTTON_LOCATION = new ResourceLocation(RotpHDAddon.MOD_ID, "textures/gui/selection_grid_button.png");

    private final ResourceLocation playerSkin;

    public PlayerButton(int x, int y, int width, int height, ResourceLocation playerSkin, IPressable onPress, Button.ITooltip tooltip) {
        super(x, y, width, height, ITextComponent.nullToEmpty(null), onPress, tooltip);
        this.playerSkin = playerSkin;
    }

    private void renderPlayerFace(MatrixStack matrixStack, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bind(this.playerSkin);
        AbstractGui.blit(matrixStack, x, y, 16, 16, 8.0F, 8.0F, 8, 8, 64, 64);
        if (mc.options.getModelParts().contains(PlayerModelPart.HAT)) {
            AbstractGui.blit(matrixStack, x, y, 16, 16, 48.0F, 8.0F, 8, 8, 64, 64);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (this.visible) {
            this.renderPlayerFace(matrixStack, this.x + 4, this.y + 4);
            if (isMouseOver(mouseX, mouseY)) {
                Minecraft.getInstance().getTextureManager().bind(BUTTON_LOCATION);
                this.blit(matrixStack, this.x, this.y, 0, 24, this.width, this.height, 48, 48);
            }
        }

        if (this.isHovered()) {
            this.renderToolTip(matrixStack, mouseX, mouseY);
        }
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(BUTTON_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(matrixStack, this.x, this.y, 0, 0, this.width, this.height, 48, 48);
    }
}