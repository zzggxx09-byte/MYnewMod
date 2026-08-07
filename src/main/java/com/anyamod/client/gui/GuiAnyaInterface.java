package com.anyamod.client.gui;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import com.anyamod.network.AnyaNetwork;
import com.anyamod.network.PacketAnyaGuiState;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiAnyaInterface extends GuiScreen {

    private static final ResourceLocation HEART_FULL =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/heart_full.png");
    private static final ResourceLocation HEART_EMPTY =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/heart_empty.png");

    private static final int HEART_SIZE = 22;
    private static final int HEART_SPACING = 28;
    private static final int MARGIN_BOTTOM = 10;

    // ==================== БІЧНІ ІКОНКИ (рюкзак / футболка / серце) ====================

    private static final ResourceLocation ICON_BACKPACK =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/icon_backpack.png");
    private static final ResourceLocation ICON_SHIRT =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/icon_shirt.png");
    private static final ResourceLocation ICON_HEART =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/icon_heart.png");

    private static final ResourceLocation ICON_BACKPACK_HOVER =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/icon_backpack_hover.png");
    private static final ResourceLocation ICON_SHIRT_HOVER =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/icon_shirt_hover.png");
    private static final ResourceLocation ICON_HEART_HOVER =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/icon_heart_hover.png");

    private static final int SIDE_ICON_SIZE = 28;
    private static final int SIDE_ICON_SPACING = 36;
    private static final int SIDE_ICON_MARGIN_LEFT = 20;
    private static final int SIDE_ICON_MARGIN_TOP = 20;

    private enum Tab { INVENTORY, CLOTHES, STATS }

    private Tab currentTab = Tab.CLOTHES;

    // ==================== АНІМАЦІЯ ====================

    private static final int ANIMATION_DURATION_MS = 250;
    private long openTime;

    private final EntityAnya anya;

    public GuiAnyaInterface(EntityAnya anya) {
        this.anya = anya;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.openTime = System.currentTimeMillis();
        AnyaNetwork.CHANNEL.sendToServer(new PacketAnyaGuiState(this.anya.getEntityId(), true));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        AnyaNetwork.CHANNEL.sendToServer(new PacketAnyaGuiState(this.anya.getEntityId(), false));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float progress = this.getAnimationProgress();

        this.drawHearts(progress);
        this.drawSideIcons(mouseX, mouseY, progress);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private float getAnimationProgress() {
        long elapsedTime = System.currentTimeMillis() - this.openTime;
        float progress = Math.min(1.0F, (float) elapsedTime / ANIMATION_DURATION_MS);
        return progress * progress * (3.0F - 2.0F * progress); // smoothstep
    }

    private void drawHearts(float progress) {
        int maxLives = this.anya.getMaxLives();
        int lives = this.anya.getLives();

        if (maxLives <= 0) return;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        int totalWidth = (maxLives - 1) * HEART_SPACING + HEART_SIZE;
        int startX = (this.width - totalWidth) / 2;

        int targetY = this.height - MARGIN_BOTTOM - HEART_SIZE;
        int startY = this.height + 10;
        int currentY = (int) (startY + (targetY - startY) * progress);

        for (int i = 0; i < maxLives; i++) {
            int x = startX + i * HEART_SPACING;
            boolean filled = i < lives;

            this.mc.getTextureManager().bindTexture(filled ? HEART_FULL : HEART_EMPTY);
            this.drawScaledCustomSizeModalRect(x, currentY, 0, 0, 16, 16, HEART_SIZE, HEART_SIZE, 16, 16);
        }

        GlStateManager.disableBlend();
    }

    /**
     * Три іконки зліва (рюкзак/футболка/серце) - виїжджають з-за лівого краю екрана,
     * при наведенні картинка підміняється на hover-варіант, клік грає звук.
     */
    private void drawSideIcons(int mouseX, int mouseY, float progress) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        int targetX = SIDE_ICON_MARGIN_LEFT;
        int startX = -SIDE_ICON_SIZE - 10;
        int currentX = (int) (startX + (targetX - startX) * progress);

        ResourceLocation[] iconsNormal = { ICON_BACKPACK, ICON_SHIRT, ICON_HEART };
        ResourceLocation[] iconsHover = { ICON_BACKPACK_HOVER, ICON_SHIRT_HOVER, ICON_HEART_HOVER };

        for (int i = 0; i < iconsNormal.length; i++) {
            int y = SIDE_ICON_MARGIN_TOP + i * SIDE_ICON_SPACING;
            boolean hovered = this.isMouseOverIcon(mouseX, mouseY, currentX, y);

            ResourceLocation texture = hovered ? iconsHover[i] : iconsNormal[i];

            this.mc.getTextureManager().bindTexture(texture);
            this.drawScaledCustomSizeModalRect(currentX, y, 0, 0, 16, 16, SIDE_ICON_SIZE, SIDE_ICON_SIZE, 16, 16);
        }

        GlStateManager.disableBlend();
    }

    private boolean isMouseOverIcon(int mouseX, int mouseY, int iconX, int iconY) {
        return mouseX >= iconX && mouseX <= iconX + SIDE_ICON_SIZE
                && mouseY >= iconY && mouseY <= iconY + SIDE_ICON_SIZE;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) return; // тільки ліва кнопка миші

        int targetX = SIDE_ICON_MARGIN_LEFT;
        Tab[] tabs = { Tab.INVENTORY, Tab.CLOTHES, Tab.STATS };

        for (int i = 0; i < tabs.length; i++) {
            int y = SIDE_ICON_MARGIN_TOP + i * SIDE_ICON_SPACING;
            if (this.isMouseOverIcon(mouseX, mouseY, targetX, y)) {
                this.currentTab = tabs[i];
                this.playClickSound();
                // TODO: тут пізніше підключимо реальне перемикання вмісту вкладки
                break;
            }
        }
    }

    private void playClickSound() {
        this.mc.getSoundHandler().playSound(
                PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F)
        );
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
