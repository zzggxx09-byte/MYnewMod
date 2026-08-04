package com.anyamod.client.gui;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import com.anyamod.network.AnyaNetwork;
import com.anyamod.network.PacketAnyaGuiState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiAnyaInterface extends GuiScreen {

    private static final ResourceLocation HEART_FULL =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/heart_full.png");
    private static final ResourceLocation HEART_EMPTY =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/heart_empty.png");

    private static final int HEART_SIZE = 22;       // розмір серця на екрані
    private static final int HEART_SPACING = 28;    // крок між серцями
    private static final int MARGIN_BOTTOM = 10;    // відступ від нижнього краю екрана

    // Параметри анімації
    private static final int ANIMATION_DURATION_MS = 250; // тривалість у мілісекундах (0.25 сек)
    private long openTime;

    private final EntityAnya anya;

    public GuiAnyaInterface(EntityAnya anya) {
        this.anya = anya;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.openTime = System.currentTimeMillis(); // Засікаємо час відкриття GUI
        AnyaNetwork.CHANNEL.sendToServer(new PacketAnyaGuiState(this.anya.getEntityId(), true));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        AnyaNetwork.CHANNEL.sendToServer(new PacketAnyaGuiState(this.anya.getEntityId(), false));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawHearts();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHearts() {
        int maxLives = this.anya.getMaxLives();
        int lives = this.anya.getLives();

        if (maxLives <= 0) return;

        // Розрахунок прогресу анімації (від 0.0 до 1.0)
        long elapsedTime = System.currentTimeMillis() - this.openTime;
        float progress = Math.min(1.0F, (float) elapsedTime / ANIMATION_DURATION_MS);

        // Формула Smoothstep для м'якого зупинення наприкінці (Ease-Out)
        progress = progress * progress * (3.0F - 2.0F * progress);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        // Загальна ширина ряду сердець
        int totalWidth = (maxLives - 1) * HEART_SPACING + HEART_SIZE;

        // X-координата для центрування (3-є серце опиняється рівно по центру)
        int startX = (this.width - totalWidth) / 2;

        // Y-координата з анімацією висування з-за меж нижнього краю
        int targetY = this.height - MARGIN_BOTTOM - HEART_SIZE;
        int startY = this.height + 10; // Старт за нижньою межею екрана
        int currentY = (int) (startY + (targetY - startY) * progress);

        for (int i = 0; i < maxLives; i++) {
            int x = startX + i * HEART_SPACING;
            boolean filled = i < lives;

            this.mc.getTextureManager().bindTexture(filled ? HEART_FULL : HEART_EMPTY);
            
            // Відмальовка з актуальною поточною Y-координатою
            this.drawScaledCustomSizeModalRect(x, currentY, 0, 0, 16, 16, HEART_SIZE, HEART_SIZE, 16, 16);
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
        return false;
    }
}
