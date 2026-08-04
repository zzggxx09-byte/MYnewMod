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

    // ЗБІЛЬШЕНО: розмір іконки та відстань між ними для великого відображення
    private static final int HEART_SIZE = 22;       // розмір серця на екрані
    private static final int HEART_SPACING = 28;    // крок між серцями
    private static final int MARGIN_BOTTOM = 10;    // відступ від нижнього краю екрана

    private final EntityAnya anya;

    public GuiAnyaInterface(EntityAnya anya) {
        this.anya = anya;
    }

    @Override
    public void initGui() {
        super.initGui();
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

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        // Загальна ширина ряду сердець (враховуючи ширину самого серця)
        int totalWidth = (maxLives - 1) * HEART_SPACING + HEART_SIZE;

        // startX центрує весь ряд; при 5 серцях 3-є опиняється строго по центру екрана
        int startX = (this.width - totalWidth) / 2;
        int y = this.height - MARGIN_BOTTOM - HEART_SIZE;

        for (int i = 0; i < maxLives; i++) {
            int x = startX + i * HEART_SPACING;
            boolean filled = i < lives;

            this.mc.getTextureManager().bindTexture(filled ? HEART_FULL : HEART_EMPTY);
            
            // Масштабуємо 16x16 текстуру до нового розширеного розміру HEART_SIZE
            this.drawScaledCustomSizeModalRect(x, y, 0, 0, 16, 16, HEART_SIZE, HEART_SIZE, 16, 16);
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
