package com.yaricktt.heavensdoor.client.ui.screen;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.ui.screen.stand.ge.EntityTypeIcon;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.entitysubtype.EntitySubtype;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.yaricktt.heavensdoor.RotpHDAddon;
import com.yaricktt.heavensdoor.capability.entity.LivingUtilCapProvider;
import com.yaricktt.heavensdoor.init.InitEffects;
import com.yaricktt.heavensdoor.init.InitSounds;
import com.yaricktt.heavensdoor.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class BookGui extends Screen {
    public static final ResourceLocation BOOK_GUI = new ResourceLocation(RotpHDAddon.MOD_ID, "textures/gui/book_gui.png");
    private final int targetEntityUUID;
    private LivingEntity targetEntity;
    private boolean targetLost = false;

    private static final double MAX_DISTANCE_SQ = 4.5 * 4.5;

    private static final ITextComponent FLY_BACKWARDS_TEXT = new TranslationTextComponent("gui.rotp_hd.command.fly_backwards");
    private int textFBButtonX;
    private int textFBButtonY;
    private int textFBButtonWidth;
    private int textFBButtonHeight;
    private boolean isTextFBHovered = false;

    private static final ITextComponent APPLY_BLINDNESS_TEXT = new TranslationTextComponent("gui.rotp_hd.command.vanish_nearby");
    private static final ITextComponent REMOVE_BLINDNESS_TEXT = new TranslationTextComponent("gui.rotp_hd.command.no_vanish_nearby")
        .setStyle(
            Style.EMPTY
            .setStrikethrough(true)
        );
    private int blindnessActionButtonX;
    private int blindnessActionButtonY;
    private int blindnessActionButtonWidth;
    private int blindnessActionButtonHeight;
    private boolean isBlindnessActionButtonHovered = false;

    private static final ITextComponent FREEZE_TEXT = new TranslationTextComponent("gui.rotp_hd.command.freeze_text");
    private int freezeActionButtonX;
    private int freezeActionButtonY;
    private int freezeActionButtonWidth;
    private int freezeActionButtonHeight;
    private boolean isFreezeActionButtonHovered = false;

    private static final ITextComponent CANT_ATTACK_PLAYER_TEXT = new TranslationTextComponent("gui.rotp_hd.command.cant_attack_player");
    private int cantAttackPlayerButtonX;
    private int cantAttackPlayerButtonY;
    private int cantAttackPlayerButtonWidth;
    private int cantAttackPlayerButtonHeight;
    private boolean isCantAttackMeButtonHovered = false;

    private static final ITextComponent KILLYOURSELF_TEXT = new TranslationTextComponent("gui.rotp_hd.command.kys");
    private static final ITextComponent REMOVE_KILLYOURSELF_TEXT = new TranslationTextComponent("gui.rotp_hd.command.no_kys")
            .setStyle(
                    Style.EMPTY
                            .setStrikethrough(true)
            );
    private int KYSButtonX;
    private int KYSButtonY;
    private int KYSButtonWidth;
    private int KYSButtonHeight;
    private boolean isKYSButtonHovered = false;

    private static final ITextComponent BEGAVIOR_TEXT = new TranslationTextComponent("gui.rotp_hd.command.behavior");
    private int behaviorButtonX;
    private int behaviorButtonY;
    private int behaviorButtonWidth;
    private int behaviorButtonHeight;
    private boolean isBehaviorButtonHovered = false;

    private static final int TEXT_COLOR_NORMAL = 0x66999999;
    private static final int TEXT_COLOR_HOVER = 0x999999;

    @Override
    protected void init() {
        super.init();
        Entity entity = Minecraft.getInstance().level.getEntity(this.targetEntityUUID);
        if (entity instanceof LivingEntity) {
            this.targetEntity = (LivingEntity) entity;
            this.targetLost = false;
        } else {
            this.targetEntity = null;
            this.targetLost = true;
            this.onClose();
            return;
        }

        int guiLeft = (this.width - 262) / 2;
        int guiTop = (this.height - 164) / 2;

        this.textFBButtonX = guiLeft + 145;
        this.textFBButtonY = guiTop + 17;
        this.textFBButtonWidth = this.font.width(FLY_BACKWARDS_TEXT);
        this.textFBButtonHeight = this.font.lineHeight;

        this.blindnessActionButtonX = guiLeft + 145;
        this.blindnessActionButtonY = this.textFBButtonY + this.textFBButtonHeight + 21;
        int applyWidth = this.font.width(APPLY_BLINDNESS_TEXT);
        int removeWidth = this.font.width(REMOVE_BLINDNESS_TEXT);
        this.blindnessActionButtonWidth = Math.max(applyWidth, removeWidth);
        this.blindnessActionButtonHeight = this.font.lineHeight;

        this.freezeActionButtonX = guiLeft + 160;
        this.freezeActionButtonY = this.blindnessActionButtonY + this.blindnessActionButtonHeight + 21;
        this.freezeActionButtonWidth = this.font.width(FREEZE_TEXT);
        this.freezeActionButtonHeight = this.font.lineHeight;

        this.cantAttackPlayerButtonX = guiLeft + 148;
        this.cantAttackPlayerButtonY = this.freezeActionButtonY + this.freezeActionButtonHeight + 19;
        this.cantAttackPlayerButtonWidth = this.font.width(CANT_ATTACK_PLAYER_TEXT);
        this.cantAttackPlayerButtonHeight = this.font.lineHeight;

        this.KYSButtonX = guiLeft + 155;
        this.KYSButtonY = this.cantAttackPlayerButtonY + this.cantAttackPlayerButtonHeight + 21;
        int applyKysWidth = this.font.width(KILLYOURSELF_TEXT);
        int removeKysWidth = this.font.width(REMOVE_KILLYOURSELF_TEXT);
        this.KYSButtonWidth = Math.max(applyKysWidth, removeKysWidth);
        this.KYSButtonHeight = this.font.lineHeight;

        this.behaviorButtonX = guiLeft + 13;
        this.behaviorButtonY = this.KYSButtonY + this.KYSButtonHeight - 5;
        this.behaviorButtonWidth = this.font.width(BEGAVIOR_TEXT);
        this.behaviorButtonHeight = this.font.lineHeight;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.targetLost || this.minecraft == null || this.minecraft.player == null) {
            this.onClose();
            return;
        }

        if (this.targetEntity != null) {
            PlayerEntity player = this.minecraft.player;
            boolean shouldClose = false;

            if (!this.targetEntity.isAlive()) {
                shouldClose = true;
            } else if (!this.targetEntity.hasEffect(InitEffects.BOOK.get())) {
                shouldClose = true;
            } else if (player.distanceToSqr(this.targetEntity) > MAX_DISTANCE_SQ) {
                shouldClose = true;
            }
            if (shouldClose) {
                this.onClose();
            }
        } else {
            this.onClose();
        }
    }

    public BookGui(int entityUUID) {
        super(NarratorChatListener.NO_TITLE);
        this.targetEntityUUID = entityUUID;
    }

    public static void openWindowOnClick(LivingEntity user, int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null && user == mc.player) {
            Screen screen = new BookGui(entityId);
            mc.setScreen(screen);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        AddonPackets.INSTANCE.sendToServer(new TriggerBookPacket(this.targetEntityUUID, 0));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (this.targetLost) {
            return false;
        }

        if (this.isMouseOverFbButton(mouseX, mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            World world = ClientUtil.getClientWorld();
            Entity entity = world.getEntity(this.targetEntityUUID);
            if (entity instanceof StandEntity) {
                StandEntity stand = (StandEntity) entity;
                AddonPackets.INSTANCE.sendToServer(new TriggerFlyBackwardsPacket(stand.getUser().getId()));
            } else if (entity instanceof LivingEntity) {
                AddonPackets.INSTANCE.sendToServer(new TriggerFlyBackwardsPacket(this.targetEntityUUID));
            }
            this.minecraft.getSoundManager().play(SimpleSound.forUI(InitSounds.HEAVENS_DOOR_WRITE_COMMAND.get(), 1.0F));
            this.onClose();
            return true;
        }

        if (this.isMouseOverBlindnessActionButton(mouseX, mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSound.forUI(InitSounds.HEAVENS_DOOR_WRITE_COMMAND.get(), 1.0F));
            boolean hasEffect = this.targetEntity.hasEffect(InitEffects.BLINDNESS_ENTITY_EFFECT.get());
            if (hasEffect) {
                AddonPackets.INSTANCE.sendToServer(new TriggerApplyBlindnessPacket(this.targetEntityUUID, 0));
            } else {
                AddonPackets.INSTANCE.sendToServer(new TriggerApplyBlindnessPacket(this.targetEntityUUID, 15 * 20));
            }

            this.onClose();
            return true;
        }

        if (this.isMouseOverFreezeButton(mouseX, mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSound.forUI(InitSounds.HEAVENS_DOOR_WRITE_COMMAND.get(), 1.0F));
            AddonPackets.INSTANCE.sendToServer(new TriggerFreezePacket(this.targetEntityUUID, 60));
            this.onClose();
            return true;
        }

        if (this.isMouseOverCantAttackPlayerButton(mouseX, mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            World world = ClientUtil.getClientWorld();
            Entity entity = world.getEntity(this.targetEntityUUID);
            if (entity instanceof StandEntity) {
                StandEntity stand = (StandEntity) entity;
                this.minecraft.setScreen(new PlayerSelectionScreen(stand.getUser().getId()));
            } else if (entity instanceof LivingEntity) {
                this.minecraft.setScreen(new PlayerSelectionScreen(this.targetEntityUUID));
            }

           // this.onClose();
            return true;
        }

        if (this.isMouseOverKYSButton(mouseX, mouseY)){
            this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSound.forUI(InitSounds.HEAVENS_DOOR_WRITE_COMMAND.get(), 1.0F));
            boolean hasEffect = this.targetEntity.hasEffect(InitEffects.KYS_EFFECT.get());
            if (hasEffect) {
                AddonPackets.INSTANCE.sendToServer(new TriggerApplyKYSPacket(this.targetEntityUUID, 0));
            } else {
                int duration = 9999 * 20;
                AddonPackets.INSTANCE.sendToServer(new TriggerApplyKYSPacket(this.targetEntityUUID, duration));
            }

            this.onClose();
            return true;
        }

        if (this.isMouseOverBehaviorButton(mouseX, mouseY)) {
            this.minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.minecraft.getSoundManager().play(SimpleSound.forUI(InitSounds.HEAVENS_DOOR_WRITE_COMMAND.get(), 1.0F));
            AddonPackets.INSTANCE.sendToServer(new TriggerChangeBehaviorPacket(this.targetEntityUUID));
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderPlayerFace(MatrixStack matrixStack, int x, int y, int size, PlayerEntity player) {
        NetworkPlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(player.getGameProfile().getId());
        if (playerInfo != null) {


            int frameThickness = 1;
            fill(matrixStack, x - frameThickness - 3, y - frameThickness - 3 , x + size + frameThickness + 3, y + 36, 0xFF000001);
            fill(matrixStack, x - frameThickness - 2, y - frameThickness - 2, x + size + frameThickness + 2, y + 35, 0xFFFFFFFF);
            fill(matrixStack, x - frameThickness, y - frameThickness, x + size + frameThickness, y + 33, 0xFF000001);
            fill(matrixStack, x - frameThickness + 1, y - frameThickness + 1, x + size + frameThickness - 1, y + 32, 0xFFE2E8F8);

            ResourceLocation skin = playerInfo.getSkinLocation();
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getTextureManager().bind(skin);

            blit(matrixStack, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);


            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            blit(matrixStack, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
            RenderSystem.disableBlend();

            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            RenderSystem.enableTexture();
        }
    }

    private void renderMobFace(MatrixStack matrixStack, int x, int y, int size, LivingEntity entity) {
        ResourceLocation icon = EntityTypeIcon.getIcon(EntitySubtype.base(entity.getType()));
        if (icon != null && icon != EntityTypeIcon.UNKNOWN) {
            Minecraft minecraft = Minecraft.getInstance();
            try {
                if (minecraft.getResourceManager().hasResource(icon)) {
                    int iconSize = 32;
                    int offsetX = (size - iconSize) / 2;
                    int offsetY = (size - iconSize) / 2;

                    int frameThickness = 1;

                    fill(matrixStack, x - frameThickness - 3, y - frameThickness - 3 , x + size + frameThickness + 3, y + 36, 0xFF000001);
                    fill(matrixStack, x - frameThickness - 2, y - frameThickness - 2, x + size + frameThickness + 2, y + 35, 0xFFFFFFFF);
                    fill(matrixStack, x - frameThickness, y - frameThickness, x + size + frameThickness, y + 33, 0xFF000001);
                    fill(matrixStack, x - frameThickness + 1, y - frameThickness + 1, x + size + frameThickness - 1, y + 32, 0xFFE2E8F8);

                    minecraft.getTextureManager().bind(icon);
                    blit(matrixStack, x + offsetX, y + offsetY, iconSize, iconSize, 0, 0, iconSize, iconSize, iconSize, iconSize);

                    RenderSystem.disableTexture();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableTexture();
                }
            } catch (Exception e) {
                RotpHDAddon.LOGGER.error("Failed to load mob icon: {}", icon, e);
            }
        }
    }

    private ITextComponent getOwnerInfo(LivingEntity entity) {
        if (entity instanceof TameableEntity) {
            TameableEntity tameable = (TameableEntity) entity;
            LivingEntity owner = tameable.getOwner();
            if (owner != null) {
                return new TranslationTextComponent("gui.rotp_hd.text.owner", owner.getName());
            }
            return new TranslationTextComponent("gui.rotp_hd.text.owner.none");
        }
        return null;
    }

    private boolean isMouseOverFbButton(double mouseX, double mouseY) {
        if (this.targetLost || this.targetEntity == null) {
            return false;
        }
        return mouseX >= this.textFBButtonX && mouseX < this.textFBButtonX + this.textFBButtonWidth &&
                mouseY >= this.textFBButtonY && mouseY < this.textFBButtonY + this.textFBButtonHeight;
    }

    private boolean isMouseOverBlindnessActionButton(double mouseX, double mouseY) {
        if (this.targetLost || this.targetEntity == null) {
            return false;
        }
        return mouseX >= this.blindnessActionButtonX && mouseX < this.blindnessActionButtonX + this.blindnessActionButtonWidth &&
                mouseY >= this.blindnessActionButtonY && mouseY < this.blindnessActionButtonY + this.blindnessActionButtonHeight;
    }

    private boolean isMouseOverFreezeButton(double mouseX, double mouseY) {
        if (this.targetLost || this.targetEntity == null) {
            return false;
        }
        return mouseX >= this.freezeActionButtonX && mouseX < this.freezeActionButtonX + this.freezeActionButtonWidth &&
                mouseY >= this.freezeActionButtonY && mouseY < this.freezeActionButtonY + this.freezeActionButtonHeight;
    }

    private boolean isMouseOverCantAttackPlayerButton(double mouseX, double mouseY) {
        if (this.targetLost || this.targetEntity == null) {
            return false;
        }
        return mouseX >= this.cantAttackPlayerButtonX && mouseX < this.cantAttackPlayerButtonX + this.cantAttackPlayerButtonWidth &&
                mouseY >= this.cantAttackPlayerButtonY && mouseY < this.cantAttackPlayerButtonY + this.cantAttackPlayerButtonHeight;
    }

    private boolean isMouseOverKYSButton(double mouseX, double mouseY) {
        if (this.targetLost || this.targetEntity == null) {
            return false;
        }
        return mouseX >= this.KYSButtonX && mouseX < this.KYSButtonX + this.KYSButtonWidth &&
                mouseY >= this.KYSButtonY && mouseY < this.KYSButtonY + this.KYSButtonHeight;
    }

    private boolean isMouseOverBehaviorButton(double mouseX, double mouseY) {
        if (this.targetLost || this.targetEntity == null) {
            return false;
        }
        return mouseX >= this.behaviorButtonX && mouseX < this.behaviorButtonX + this.behaviorButtonWidth &&
                mouseY >= this.behaviorButtonY && mouseY < this.behaviorButtonY + this.behaviorButtonHeight;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableBlend();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(BOOK_GUI);
        int i = (this.width - 262) / 2;
        int j = (this.height - 164) / 2;
        blit(matrixStack, i, j, 0.0F, 0.0F, 255, 164, 255, 164);
        if (this.targetEntity != null && this.targetEntity.isAlive()) {
            String name = this.targetEntity.getName().getString();
            final long[] liveTime = {0};
            if (this.targetEntity instanceof StandEntity) {
                LivingEntity user = ((StandEntity) this.targetEntity).getUser();
                user.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    liveTime[0] = cap.getLiveTime() / (20 * 60);
                });
            } else if (this.targetEntity instanceof LivingEntity) {
                this.targetEntity.getCapability(LivingUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                    liveTime[0] = cap.getLiveTime() / (20 * 60);
                });
            }

            int health = (int)this.targetEntity.getHealth();
            int maxHealth = (int)this.targetEntity.getMaxHealth();
            float width = this.targetEntity.getBbWidth();
            float height = this.targetEntity.getBbHeight();
            ITextComponent ownerInfo = getOwnerInfo(this.targetEntity);

            INonStandPower power = INonStandPower.getNonStandPowerOptional(this.targetEntity).orElse(null);
            IStandPower standPower = IStandPower.getStandPowerOptional(this.targetEntity).orElse(null);

            String healthStr = String.format("%d / %d", health, maxHealth);

            int textX = i + 10;
            int textY = j + 15;
            int lineHeight = 17;
            float font = 0.85F;

            matrixStack.pushPose();
            matrixStack.scale(font, font, font);
            float scaledTextX = textX / font;
            float scaledTextY = textY / font;

            scaledTextX += 2;
            scaledTextY -= 4;
            if (name.length() <= 16) {
                this.font.draw(matrixStack, name, scaledTextX, scaledTextY, 0x505050);
            } else if (name.length() > 17) {
                ResourceLocation icon = EntityTypeIcon.getIcon(EntitySubtype.base(this.targetEntity.getType()));
                if (icon == null || icon == EntityTypeIcon.UNKNOWN) {
                    if (name.length() > 20) {
                        String firstLine = name.substring(0, 19);
                        String secondLine = name.substring(19);
                        this.font.draw(matrixStack, firstLine + "-", scaledTextX, scaledTextY, 0x505050);
                        scaledTextY += 8;
                        this.font.draw(matrixStack, secondLine, scaledTextX, scaledTextY, 0x505050);
                        scaledTextY -= 8;
                    }
                } else if (icon != null && icon != EntityTypeIcon.UNKNOWN) {
                    if (name.length() > 17) {
                        String firstLine = name.substring(0, 16);
                        String secondLine = name.substring(16);
                        this.font.draw(matrixStack, firstLine + "-", scaledTextX, scaledTextY, 0x505050);
                        scaledTextY += 8;
                        this.font.draw(matrixStack, secondLine, scaledTextX, scaledTextY, 0x505050);
                        scaledTextY -= 8;
                    }
                }
            } else {
                this.font.draw(matrixStack, name, scaledTextX, scaledTextY, 0x505050);
            }
            
            scaledTextX -= 2;
            scaledTextY += 4;
            if (this.targetEntity instanceof PlayerEntity) {
                scaledTextY += lineHeight / font;
                renderPlayerFace(matrixStack, (int) scaledTextX + 102, (int) scaledTextY - 25, 32, (PlayerEntity) this.targetEntity);
            } else if (this.targetEntity instanceof LivingEntity) {
                scaledTextY += lineHeight / font;
                renderMobFace(matrixStack, (int) scaledTextX + 100, (int) scaledTextY - 25, 32, (LivingEntity) this.targetEntity);
            } else {
                scaledTextY += lineHeight / font;
            }

            final ITextComponent HEALTHS = new TranslationTextComponent("gui.rotp_hd.text.health", healthStr);
            final ITextComponent LIFETIME = new TranslationTextComponent("gui.rotp_hd.text.life_time", liveTime[0]);
            final ITextComponent HEIGHT = new TranslationTextComponent("gui.rotp_hd.text.height", height);
            final ITextComponent WIDTH = new TranslationTextComponent("gui.rotp_hd.text.width", width);
            final ITextComponent ITSPLAYER = new TranslationTextComponent("gui.rotp_hd.text.itsplayer");
            final ITextComponent ITSSTAND = new TranslationTextComponent("gui.rotp_hd.text.itsstand");
            final ITextComponent OWNER = ownerInfo;


            this.font.draw(matrixStack, HEALTHS, scaledTextX, scaledTextY, 0x505050);

            scaledTextY += lineHeight / font;

            this.font.draw(matrixStack, LIFETIME, scaledTextX, scaledTextY, 0x505050);
            scaledTextY += lineHeight / font;

            this.font.draw(matrixStack, HEIGHT, scaledTextX, scaledTextY, 0x505050);
            scaledTextY += lineHeight / font;

            this.font.draw(matrixStack, WIDTH, scaledTextX, scaledTextY, 0x505050);
            scaledTextY += 18 / font;

            if (this.targetEntity instanceof StandEntity) {
                StandEntity stand = (StandEntity) this.targetEntity;
                LivingEntity standOwner = stand.getUser();
                if (standOwner != null){
                    String standOwnerStr = standOwner.getName().getString();
                    if (standOwnerStr.length() > 16) {
                        String firstLine = standOwnerStr.substring(0, 15);
                        String secondLine = standOwnerStr.substring(15);
                        final ITextComponent MASTERF = new TranslationTextComponent("gui.rotp_hd.text.master", firstLine, "-");
                        this.font.draw(matrixStack, MASTERF, scaledTextX, scaledTextY, 0x505050);
                        scaledTextY += 8 / font;
                        this.font.draw(matrixStack, secondLine, scaledTextX, scaledTextY, 0x505050);
                    } else {
                        final ITextComponent MASTER = new TranslationTextComponent("gui.rotp_hd.text.master", standOwnerStr);
                        this.font.draw(matrixStack, MASTER, scaledTextX, scaledTextY, 0x505050);
                    }
                }
            } else {
                if (standPower != null) {
                    String standName = standPower.getName() != null ? standPower.getName().getString() : "Unknown Stand";
                    final ITextComponent NONESTAND = new TranslationTextComponent("gui.rotp_hd.text.stand_none");
                    if (!standName.isEmpty()) {
                        if (standName.length() > 15) {
                            String firstLine = standName.substring(0, 14);
                            String secondLine = standName.substring(14);
                            final ITextComponent STANDF = new TranslationTextComponent("gui.rotp_hd.text.stand", firstLine, "-");
                            this.font.draw(matrixStack, STANDF, scaledTextX, scaledTextY, 0x505050);
                            scaledTextY += 8 / font;
                            this.font.draw(matrixStack, secondLine, scaledTextX, scaledTextY, 0x505050);
                            int iconX = (int) scaledTextX + 30;
                            int iconY = (int) scaledTextY - 16;
                            this.minecraft.getTextureManager().bind(standPower.clGetPowerTypeIcon());
                            blit(matrixStack, iconX, iconY, 0, 0, 20, 20,20,20);
                        } else {
                            final ITextComponent STAND = new TranslationTextComponent("gui.rotp_hd.text.stand", standName);
                            this.font.draw(matrixStack, STAND, scaledTextX, scaledTextY, 0x505050);
                            int iconX = (int) scaledTextX + 31;
                            int iconY = (int) scaledTextY - 9;
                            this.minecraft.getTextureManager().bind(standPower.clGetPowerTypeIcon());
                            blit(matrixStack, iconX, iconY, 0, 0, 20, 20,20,20);
                        }
                    } else {
                        this.font.draw(matrixStack, NONESTAND, scaledTextX, scaledTextY, 0x505050);
                    }
                } else {
                    final ITextComponent NONESTAND = new TranslationTextComponent("gui.rotp_hd.text.stand_none");
                    this.font.draw(matrixStack, NONESTAND, scaledTextX, scaledTextY, 0x505050);
                }
            }

            scaledTextY += 13 / font;

            if (power != null) {
                String jjpower = power.getName() != null ? power.getName().getString() : "Unknown Power";

                final ITextComponent NONEPOWER = new TranslationTextComponent("gui.rotp_hd.text.power_none");
                if (!jjpower.isEmpty()) {
                    if (jjpower.length() > 14) {
                        String firstLine = jjpower.substring(0, 13);
                        String secondLine = jjpower.substring(13);
                        final ITextComponent POWER = new TranslationTextComponent("gui.rotp_hd.text.power", firstLine + "-");
                        this.font.draw(matrixStack, POWER , scaledTextX, scaledTextY, 0x505050);
                        scaledTextY += 8 / font;
                        scaledTextX += 45 / font;
                        this.font.draw(matrixStack, secondLine, scaledTextX, scaledTextY, 0x505050);
                        scaledTextY -= 8 / font;
                        scaledTextX -= 45 / font;
                    } else {
                        final ITextComponent POWER = new TranslationTextComponent("gui.rotp_hd.text.power", jjpower);
                        this.font.draw(matrixStack, POWER , scaledTextX, scaledTextY, 0x505050);
                    }

                    // this.font.draw(matrixStack, POWER , scaledTextX, scaledTextY, 0x505050);
                    int iconX = (int) scaledTextX + 32;
                    this.minecraft.getTextureManager().bind(power.clGetPowerTypeIcon());
                    if (standPower != null) {
                        String standName = standPower.getName() != null ? standPower.getName().getString() : "Unknown Stand";
                        if (standName.length() < 15) {
                            int iconY = (int) scaledTextY - 3;
                            blit(matrixStack, iconX, iconY, 0, 0, 20, 20, 20, 20);
                        } else {
                            int iconY = (int) scaledTextY - 10;
                            blit(matrixStack, iconX, iconY, 0, 0, 20, 20, 20, 20);
                        }
                    } else {
                        int iconY = (int) scaledTextY - 3;
                        blit(matrixStack, iconX, iconY, 0, 0, 20, 20, 20, 20);
                    }
                } else {
                    this.font.draw(matrixStack, NONEPOWER, scaledTextX, scaledTextY, 0x505050);
                }
            } else {
                final ITextComponent NONEPOWER = new TranslationTextComponent("gui.rotp_hd.text.power_none");
                this.font.draw(matrixStack, NONEPOWER, scaledTextX, scaledTextY, 0x505050);
            }

            matrixStack.popPose();

            PlayerEntity user = minecraft.player;
            IStandPower powerStand = IStandPower.getPlayerStandPower(user);


            this.isTextFBHovered = isMouseOverFbButton(mouseX, mouseY);
            int fbTextColor = this.isTextFBHovered ? TEXT_COLOR_HOVER : TEXT_COLOR_NORMAL;
            if (powerStand.getResolveLevel() >= 2) {
                this.font.draw(matrixStack, FLY_BACKWARDS_TEXT, this.textFBButtonX, this.textFBButtonY, fbTextColor);
            }

            this.isBlindnessActionButtonHovered = isMouseOverBlindnessActionButton(mouseX, mouseY);
            int buttonTextColor = this.isBlindnessActionButtonHovered ? TEXT_COLOR_HOVER : TEXT_COLOR_NORMAL;
            if (powerStand.getResolveLevel() >= 2) {
                if (this.targetEntity.hasEffect(InitEffects.BLINDNESS_ENTITY_EFFECT.get())) {
                    this.font.draw(matrixStack, REMOVE_BLINDNESS_TEXT, this.blindnessActionButtonX, this.blindnessActionButtonY, buttonTextColor);
                } else {
                    this.font.draw(matrixStack, APPLY_BLINDNESS_TEXT, this.blindnessActionButtonX, this.blindnessActionButtonY, buttonTextColor);
                }
            }

            this.isFreezeActionButtonHovered = isMouseOverFreezeButton(mouseX, mouseY);
            int freezeColor = this.isFreezeActionButtonHovered ? TEXT_COLOR_HOVER : TEXT_COLOR_NORMAL;
            if (powerStand.getResolveLevel() >= 1) {
                this.font.draw(matrixStack, FREEZE_TEXT, this.freezeActionButtonX, this.freezeActionButtonY, freezeColor);
            }

            this.isCantAttackMeButtonHovered = isMouseOverCantAttackPlayerButton(mouseX, mouseY);
            int cantAttackColor = this.isCantAttackMeButtonHovered ? TEXT_COLOR_HOVER : TEXT_COLOR_NORMAL;
            if (powerStand.getResolveLevel() >= 3) {
                this.font.draw(matrixStack, CANT_ATTACK_PLAYER_TEXT, this.cantAttackPlayerButtonX, this.cantAttackPlayerButtonY, cantAttackColor);
            }

            this.isKYSButtonHovered = isMouseOverKYSButton(mouseX, mouseY);
            int KysTextColor = this.isKYSButtonHovered ? TEXT_COLOR_HOVER : TEXT_COLOR_NORMAL;
            if (user.hasEffect(ModStatusEffects.RESOLVE.get()) && powerStand.getResolveLevel() >= 4) {
                if (!this.targetEntity.hasEffect(InitEffects.KYS_EFFECT.get())) {
                    this.font.draw(matrixStack, KILLYOURSELF_TEXT, this.KYSButtonX, this.KYSButtonY, KysTextColor);
                } else {
                    this.font.draw(matrixStack, REMOVE_KILLYOURSELF_TEXT, this.KYSButtonX, this.KYSButtonY, KysTextColor);
                }
            } else {
                if (this.targetEntity.hasEffect(InitEffects.KYS_EFFECT.get())) {
                    this.font.draw(matrixStack, REMOVE_KILLYOURSELF_TEXT, this.KYSButtonX, this.KYSButtonY, KysTextColor);
                }
            }

            if (powerStand.getResolveLevel() >= 4 && !(this.targetEntity instanceof TameableEntity)
                    && !(this.targetEntity instanceof PlayerEntity) && !(this.targetEntity instanceof StandEntity)) {
                this.isBehaviorButtonHovered = isMouseOverBehaviorButton(mouseX, mouseY);
                int TextButtonColor = this.isBehaviorButtonHovered ? TEXT_COLOR_HOVER : TEXT_COLOR_NORMAL;
                this.font.draw(matrixStack, BEGAVIOR_TEXT, this.behaviorButtonX, this.behaviorButtonY, TextButtonColor);
            } else if (this.targetEntity instanceof PlayerEntity) {
                this.font.draw(matrixStack, ITSPLAYER, this.behaviorButtonX, this.behaviorButtonY, 0x505050);
            }  else if (this.targetEntity instanceof StandEntity) {
                this.font.draw(matrixStack, ITSSTAND, this.behaviorButtonX, this.behaviorButtonY, 0x505050);
            } else if (this.targetEntity instanceof TameableEntity ) {
                if (OWNER != null) {
                    this.font.draw(matrixStack, OWNER, this.behaviorButtonX, this.behaviorButtonY, 0x505050);
                }
            }
        } else {
            int textX = this.width / 2;
            int textY = this.height / 2;
            drawCenteredString(matrixStack, this.font, "Target lost or invalid.", textX, textY, 0xFF0000);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}