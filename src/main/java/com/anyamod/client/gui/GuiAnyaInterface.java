package com.anyamod.client.gui;

import com.anyamod.entity.EntityAnya;
import com.anyamod.network.AnyaNetwork;
import com.anyamod.network.PacketAnyaGuiState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiAnyaInterface extends GuiScreen {

    private static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    private static final int HEART_SIZE = 18;
    private static final int HEART_SPACING = 20;
    private static final int MARGIN_TOP = 20;
    private static final int MARGIN_RIGHT = 20;

    private final EntityAnya anya;

    public GuiAnyaInterface(EntityAnya anya) {
        this.anya = anya;
    }

    @Override
    public void initGui() {
        super.initGui();
        // Повідомляємо сервер, що гравець відкрив UI - Аня завмре і дивитиметься на нього
        AnyaNetwork.CHANNEL.sendToServer(new PacketAnyaGuiState(this.anya.getEntityId(), true));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        // Повідомляємо сервер про закриття - Аня повертається до звичайного AI
        AnyaNetwork.CHANNEL.sendToServer(new PacketAnyaGuiState(this.anya.getEntityId(), false));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Затемнення прибрано - інтерфейс прозорий, HUD ховається окремо через
        // AnyaGuiOverlayHandler, а не через темний фон.
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

        for (int i = 0; i < maxLives; i++) {
            int x = startX + i * HEART_SPACING;
            boolean filled = i < lives;

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
        return false;
    }
}
