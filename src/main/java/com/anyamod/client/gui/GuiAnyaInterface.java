package com.anyamod.client.gui;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Collections;

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
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Розрахунок координат бокових іконок
        int heartX = SIDE_ICON_MARGIN_LEFT;
        int heartY = SIDE_ICON_MARGIN_TOP;

        int shirtX = SIDE_ICON_MARGIN_LEFT;
        int shirtY = SIDE_ICON_MARGIN_TOP + SIDE_ICON_SPACING;

        int backpackX = SIDE_ICON_MARGIN_LEFT;
        int backpackY = SIDE_ICON_MARGIN_TOP + (SIDE_ICON_SPACING * 2);

        // 1. Отрисовка іконок (Звичайна / Hover)
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Серце (Indicators / STATS)
        boolean isHeartHovered = isHovered(heartX, heartY, SIDE_ICON_SIZE, SIDE_ICON_SIZE, mouseX, mouseY);
        this.mc.getTextureManager().bindTexture(isHeartHovered ? ICON_HEART_HOVER : ICON_HEART);
        drawModalRectWithCustomSizedTexture(heartX, heartY, 0, 0, SIDE_ICON_SIZE, SIDE_ICON_SIZE, SIDE_ICON_SIZE, SIDE_ICON_SIZE);

        // Футболка (Clothing / CLOTHES)
        boolean isShirtHovered = isHovered(shirtX, shirtY, SIDE_ICON_SIZE, SIDE_ICON_SIZE, mouseX, mouseY);
        this.mc.getTextureManager().bindTexture(isShirtHovered ? ICON_SHIRT_HOVER : ICON_SHIRT);
        drawModalRectWithCustomSizedTexture(shirtX, shirtY, 0, 0, SIDE_ICON_SIZE, SIDE_ICON_SIZE, SIDE_ICON_SIZE, SIDE_ICON_SIZE);

        // Рюкзак (Inventory / INVENTORY)
        boolean isBackpackHovered = isHovered(backpackX, backpackY, SIDE_ICON_SIZE, SIDE_ICON_SIZE, mouseX, mouseY);
        this.mc.getTextureManager().bindTexture(isBackpackHovered ? ICON_BACKPACK_HOVER : ICON_BACKPACK);
        drawModalRectWithCustomSizedTexture(backpackX, backpackY, 0, 0, SIDE_ICON_SIZE, SIDE_ICON_SIZE, SIDE_ICON_SIZE, SIDE_ICON_SIZE);

        // ==================== СПЛИВАЮЧІ ПІДКАЗКИ (TOOLTIPS) ====================
        // Отрисовка підказок викликається в самому кінці, щоб вони були поверх усіх елементів GUI

        if (isHeartHovered) {
            this.drawHoveringText(Collections.singletonList("Indicators"), mouseX, mouseY);
        } else if (isShirtHovered) {
            this.drawHoveringText(Collections.singletonList("Clothing"), mouseX, mouseY);
        } else if (isBackpackHovered) {
            this.drawHoveringText(Collections.singletonList("Inventory"), mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) { // Лівий клік миші
            int heartX = SIDE_ICON_MARGIN_LEFT;
            int heartY = SIDE_ICON_MARGIN_TOP;

            int shirtX = SIDE_ICON_MARGIN_LEFT;
            int shirtY = SIDE_ICON_MARGIN_TOP + SIDE_ICON_SPACING;

            int backpackX = SIDE_ICON_MARGIN_LEFT;
            int backpackY = SIDE_ICON_MARGIN_TOP + (SIDE_ICON_SPACING * 2);

            if (isHovered(heartX, heartY, SIDE_ICON_SIZE, SIDE_ICON_SIZE, mouseX, mouseY)) {
                this.currentTab = Tab.STATS;
            } else if (isHovered(shirtX, shirtY, SIDE_ICON_SIZE, SIDE_ICON_SIZE, mouseX, mouseY)) {
                this.currentTab = Tab.CLOTHES;
            } else if (isHovered(backpackX, backpackY, SIDE_ICON_SIZE, SIDE_ICON_SIZE, mouseX, mouseY)) {
                this.currentTab = Tab.INVENTORY;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * Допоміжний метод перевірки чи знаходиться курсор у межах області
     */
    private boolean isHovered(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    }
