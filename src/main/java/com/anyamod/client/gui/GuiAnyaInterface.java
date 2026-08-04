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

    // ЗМІНЕНО: власні текстури замість ванільного textures/gui/icons.png
    private static final ResourceLocation HEART_FULL =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/heart_full.png");
    private static final ResourceLocation HEART_EMPTY =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/heart_empty.png");

    // Текстури вже 16x16 - малюємо в натуральному розмірі, без масштабування.
    // Якщо захочете трохи більші/менші сердечка на екрані - міняйте тільки HEART_SIZE.
    private static final int HEART_SIZE = 9;
    private static final int HEART_SPACING = 10;   // невеликий проміжок між серцями
    private static final int MARGIN_TOP = 20;
    private static final int MARGIN_RIGHT = 20;

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

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        int totalWidth = maxLives * HEART_SPACING;
        int startX = this.width - MARGIN_RIGHT - totalWidth;
        int y = MARGIN_TOP;

        for (int i = 0; i < maxLives; i++) {
            int x = startX + i * HEART_SPACING;
            boolean filled = i < lives;

            this.mc.getTextureManager().bindTexture(filled ? HEART_FULL : HEART_EMPTY);
            // Малюємо всю текстуру (0,0 -> 16x16) без вирізання шматка з атласу -
            // кожен файл вже сам по собі є готовим серцем.
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
