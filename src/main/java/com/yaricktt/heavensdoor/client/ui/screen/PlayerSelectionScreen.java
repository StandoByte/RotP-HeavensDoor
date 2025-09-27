package com.yaricktt.heavensdoor.client.ui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.yaricktt.heavensdoor.client.ui.widget.PlayerButton;
import com.yaricktt.heavensdoor.init.InitEffects;
import com.yaricktt.heavensdoor.init.InitSounds;
import com.yaricktt.heavensdoor.network.AddonPackets;
import com.yaricktt.heavensdoor.network.RequestPlayerListPacket;
import com.yaricktt.heavensdoor.network.SetCannotAttackTargetPacket;
import com.yaricktt.heavensdoor.network.TriggerBookPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

// https://github.com/dikiytechies/enhanced_pearls
public class PlayerSelectionScreen extends Screen {

    private final int targetEntityId;
    private boolean gridInitialized = false;
    private static final double MAX_DISTANCE_SQ = 4.5 * 4.5;

    public PlayerSelectionScreen(int targetEntityId) {
        super(new TranslationTextComponent("gui.rotp_hd.player_selection"));
        this.targetEntityId = targetEntityId;
    }

    @Override
    protected void init() {
        super.init();
        AddonPackets.INSTANCE.sendToServer(new RequestPlayerListPacket(this.targetEntityId));
    }


    @Override
    public void tick() {
        super.tick();
        Entity entity = Minecraft.getInstance().level.getEntity(this.targetEntityId);
        LivingEntity targetEntity = (LivingEntity) entity;
        if (targetEntity != null) {
            PlayerEntity player = this.minecraft.player;
            boolean shouldClose = false;
            if (!targetEntity.isAlive()) {
                shouldClose = true;
            } else if (!targetEntity.hasEffect(InitEffects.BOOK.get())) {
                shouldClose = true;
            } else if (player.distanceToSqr(targetEntity) > MAX_DISTANCE_SQ) {
                shouldClose = true;
            }
            if (shouldClose) {
                this.onClose();
            }
        } else {
            this.onClose();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        AddonPackets.INSTANCE.sendToServer(new TriggerBookPacket(this.targetEntityId, 0));
    }

    public void initGrid(List<NetworkPlayerInfo> players) {
        if (this.minecraft == null) return;
        this.buttons.clear();
        this.children.clear();
        this.gridInitialized = true;

        int gridScale = 24;
        int gridSpace = 4;
        int gridCols = 9;

        int totalWidth = (gridScale + gridSpace) * gridCols - gridSpace;
        int startX = (this.width - totalWidth) / 2;
        int startY = (this.height - (int)Math.ceil((double)players.size() / gridCols) * (gridScale + gridSpace)) / 2;

        for (int i = 0; i < players.size(); i++) {
            NetworkPlayerInfo playerInfo = players.get(i);
            if (playerInfo == null) continue;

            int row = i / gridCols;
            int col = i % gridCols;

            int buttonX = startX + col * (gridScale + gridSpace);
            int buttonY = startY + row * (gridScale + gridSpace);

            this.addButton(new PlayerButton(
                    buttonX, buttonY, gridScale, gridScale,
                    playerInfo.getSkinLocation(),
                    (button) -> {
                        AddonPackets.INSTANCE.sendToServer(new SetCannotAttackTargetPacket(this.targetEntityId, playerInfo.getProfile().getId()));
                        this.minecraft.getSoundManager().play(SimpleSound.forUI(InitSounds.HEAVENS_DOOR_WRITE_COMMAND.get(), 1.0F));
                        AddonPackets.INSTANCE.sendToServer(new TriggerBookPacket(this.targetEntityId, 0));
                        this.onClose();
                    },
                    (button, matrixStack, mouseX, mouseY) -> {
                        if (button.isHovered()) {
                            this.renderTooltip(matrixStack, ITextComponent.nullToEmpty(playerInfo.getProfile().getName()), mouseX, mouseY);
                        }
                    }
            ));
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (this.gridInitialized && this.buttons.isEmpty()) {
            drawCenteredString(matrixStack, this.font, "No Players", this.width / 2, this.height / 2, 0xFFFFFF);
        } else if (!this.gridInitialized) {
            drawCenteredString(matrixStack, this.font, " ", this.width / 2, this.height / 2, 0xFFFFFF);
        }

        for (Widget widget : this.buttons) {
            if (widget.isHovered()) {
                widget.renderToolTip(matrixStack, mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}