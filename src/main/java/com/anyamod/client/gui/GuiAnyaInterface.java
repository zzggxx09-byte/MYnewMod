package com.anyamod.client.gui;

import com.anyamod.entity.EntityAnya;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * Кастомний інтерфейс Ані. Відкривається правим кліком (EntityAnya.processInteract).
 * Наразі малює тільки хардкорні серця (житя) у верхньому правому куті -
 * решта інтерфейсу буде додана пізніше.
 */
public class GuiAnyaInterface extends GuiScreen {

    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    // Підкрутіть ці числа під ваш макет, якщо розмір/позиція трохи не збігаються
    private static final int HEART_SIZE = 18;        // розмір одного серця на екрані (px)
    private static final int HEART_SPACING = 20;     // відстань між серцями (px)
    private static final int MARGIN_TOP = 20;         // відступ від верху екрана
    private static final int MARGIN_RIGHT = 20;       // відступ від правого краю екрана

    private final EntityAnya anya;

    public GuiAnyaInterface(EntityAnya anya) {
        this.anya = anya;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Затемнений фон - те, що приховує хотбар/інвентар гравця замість нього
        this.drawDefaultBackground();

        this.drawHearts();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHearts() {
        int maxLives = this.anya.getMaxLives();
        int lives = this.anya.getLives();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        this.mc.getTextureManager().bindTexture(ICONS);

        int totalWidth = maxLives * HEART_SPACING;
        int startX = this.width - MARGIN_RIGHT - totalWidth;
        int y = MARGIN_TOP;

        // i=0 - лівий, i=maxLives-1 - правий. Життя заповнюються зліва,
        // тому втрачені (чорні) серця завжди з правого краю ряду.
        for (int i = 0; i < maxLives; i++) {
            int x = startX + i * HEART_SPACING;
            boolean filled = i < lives;

            // Хардкорні серця в icons.png лежать на 45px нижче звичайних (5 рядків по 9px)
            int u = filled ? 52 : 16;
            int v = 45;

            this.drawScaledCustomSizeModalRect(x, y, u, v, 9, 9, HEART_SIZE, HEART_SIZE, 256, 256);
        }

        GlStateManager.disableBlend();
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
        // Не ставимо гру на пауза - це інтерфейс взаємодії, не меню
        return false;
    }
}
