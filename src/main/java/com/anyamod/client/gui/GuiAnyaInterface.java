package com.anyamod.client.gui;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import com.anyamod.network.AnyaNetwork;
import com.anyamod.network.PacketAnyaGuiState;
import com.anyamod.network.PacketDropItemFromAnya;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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

    // Підписи для тултіпів у тому ж порядку, що й іконки/таби нижче
    private static final String[] TAB_TOOLTIPS = { "Inventory", "Clothing", "Stats" };

    // ==================== ВКЛАДКА ІНВЕНТАРЯ ====================

    private static final int INV_COLUMNS = 9;
    private static final int INV_ROWS = 3;
    private static final int INV_SLOT_SIZE = 18;
    private static final int INV_MARGIN_TOP = 60;

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

        if (this.currentTab == Tab.INVENTORY) {
            this.drawInventoryTab(mouseX, mouseY);
        }

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
     * При наведенні також показується підказка (тултіп) з назвою кнопки.
     */
    private void drawSideIcons(int mouseX, int mouseY, float progress) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();

        int targetX = SIDE_ICON_MARGIN_LEFT;
        int startX = -SIDE_ICON_SIZE - 10;
        int currentX = (int) (startX + (targetX - startX) * progress);

        ResourceLocation[] iconsNormal = { ICON_BACKPACK, ICON_SHIRT, ICON_HEART };
        ResourceLocation[] iconsHover = { ICON_BACKPACK_HOVER, ICON_SHIRT_HOVER, ICON_HEART_HOVER };

        int hoveredIndex = -1;

        for (int i = 0; i < iconsNormal.length; i++) {
            int y = SIDE_ICON_MARGIN_TOP + i * SIDE_ICON_SPACING;
            boolean hovered = this.isMouseOverIcon(mouseX, mouseY, currentX, y);
            if (hovered) {
                hoveredIndex = i;
            }

            ResourceLocation texture = hovered ? iconsHover[i] : iconsNormal[i];

            this.mc.getTextureManager().bindTexture(texture);
            this.drawScaledCustomSizeModalRect(currentX, y, 0, 0, 16, 16, SIDE_ICON_SIZE, SIDE_ICON_SIZE, 16, 16);
        }

        GlStateManager.disableBlend();

        // Тултіп малюємо останнім, щоб він був поверх усіх іконок
        if (hoveredIndex != -1) {
            this.drawHoveringText(Collections.singletonList(TAB_TOOLTIPS[hoveredIndex]), mouseX, mouseY);
        }
    }

    private boolean isMouseOverIcon(int mouseX, int mouseY, int iconX, int iconY) {
        return mouseX >= iconX && mouseX <= iconX + SIDE_ICON_SIZE
                && mouseY >= iconY && mouseY <= iconY + SIDE_ICON_SIZE;
    }

    /**
     * Малює сітку 9x3 слотів з вмістом ItemStackHandler-а Ані.
     * Клік по слоту з предметом обробляється в mouseClicked -> handleInventoryClick.
     */
    private void drawInventoryTab(int mouseX, int mouseY) {
        ItemStackHandler inv = this.anya.getInventory();
        int slots = inv.getSlots();

        int gridWidth = INV_COLUMNS * INV_SLOT_SIZE;
        int gridHeight = INV_ROWS * INV_SLOT_SIZE;
        int startX = (this.width - gridWidth) / 2;
        int startY = INV_MARGIN_TOP;

        // Фон панелі під слотами
        this.drawRect(startX - 8, startY - 8, startX + gridWidth + 8, startY + gridHeight + 8, 0xC0101010);

        RenderItem itemRender = this.mc.getRenderItem();
        ItemStack hoveredStack = ItemStack.EMPTY;

        for (int i = 0; i < slots; i++) {
            int col = i % INV_COLUMNS;
            int row = i / INV_COLUMNS;
            int x = startX + col * INV_SLOT_SIZE;
            int y = startY + row * INV_SLOT_SIZE;

            boolean hovered = mouseX >= x && mouseX < x + INV_SLOT_SIZE
                    && mouseY >= y && mouseY < y + INV_SLOT_SIZE;

            int slotColor = hovered ? 0x80FFFFFF : 0x80000000;
            this.drawRect(x, y, x + INV_SLOT_SIZE - 1, y + INV_SLOT_SIZE - 1, slotColor);

            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.enableDepth();
                itemRender.renderItemAndEffectIntoGUI(stack, x + 1, y + 1);
                itemRender.renderItemOverlayIntoGUI(this.fontRenderer, stack, x + 1, y + 1, null);
                RenderHelper.disableStandardItemLighting();

                if (hovered) {
                    hoveredStack = stack;
                }
            }
        }

        if (!hoveredStack.isEmpty()) {
            this.drawItemTooltip(hoveredStack, mouseX, mouseY);
        }
    }

    private void drawItemTooltip(ItemStack stack, int mouseX, int mouseY) {
        List<String> tooltip = stack.getTooltip(
                this.mc.player,
                this.mc.gameSettings.advancedItemTooltips
                        ? net.minecraft.client.util.ITooltipFlag.TooltipFlags.ADVANCED
                        : net.minecraft.client.util.ITooltipFlag.TooltipFlags.NORMAL
        );
        this.drawHoveringText(tooltip, mouseX, mouseY);
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
                return;
            }
        }

        if (this.currentTab == Tab.INVENTORY) {
            this.handleInventoryClick(mouseX, mouseY);
        }
    }

    /**
     * Клік по предмету у вкладці "Інвентар" - Аня викидає його на землю.
     * Забрати напряму в руку не можна, це навмисне обмеження.
     */
    private void handleInventoryClick(int mouseX, int mouseY) {
        int gridWidth = INV_COLUMNS * INV_SLOT_SIZE;
        int gridHeight = INV_ROWS * INV_SLOT_SIZE;
        int startX = (this.width - gridWidth) / 2;
        int startY = INV_MARGIN_TOP;

        if (mouseX < startX || mouseX >= startX + gridWidth
                || mouseY < startY || mouseY >= startY + gridHeight) {
            return;
        }

        int col = (mouseX - startX) / INV_SLOT_SIZE;
        int row = (mouseY - startY) / INV_SLOT_SIZE;
        int slot = row * INV_COLUMNS + col;

        ItemStackHandler inv = this.anya.getInventory();
        if (slot < 0 || slot >= inv.getSlots()) return;
        if (inv.getStackInSlot(slot).isEmpty()) return;

        AnyaNetwork.CHANNEL.sendToServer(new PacketDropItemFromAnya(this.anya.getEntityId(), slot));
        this.playClickSound();
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
