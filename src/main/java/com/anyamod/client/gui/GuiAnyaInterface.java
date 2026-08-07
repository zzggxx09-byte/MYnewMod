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
import java.util.ArrayList;
import java.util.List;

public class GuiAnyaInterface extends GuiScreen {

    private static final ResourceLocation HEART_FULL =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/heart_full.png");
    private static final ResourceLocation HEART_EMPTY =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/heart_empty.png");

    private static final int HEART_SIZE = 22;
    private static final int HEART_SPACING = 28;
    private static final int MARGIN_BOTTOM = 10;

    // ==================== БІЧНІ ІКОНКИ-ВКЛАДКИ (рюкзак / футболка / серце) ====================

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

    // ==================== СЛОТИ ОДЯГУ (з'являються у вкладці "Футболка") ====================

    private static final ResourceLocation ICON_HAT =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/slot_hat.png");
    private static final ResourceLocation ICON_BRA =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/slot_bra.png");
    private static final ResourceLocation ICON_TANKTOP =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/slot_tanktop.png");
    private static final ResourceLocation ICON_JACKET =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/slot_jacket.png");
    private static final ResourceLocation ICON_PANTIES =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/slot_panties.png");
    private static final ResourceLocation ICON_SHORTS =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/slot_shorts.png");
    private static final ResourceLocation ICON_SHOES =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/slot_shoes.png");

    private static final ResourceLocation ICON_REVERSE =
            new ResourceLocation(AnyaMod.MODID, "textures/gui/icon_reverse.png");

    private static final int SLOT_SIZE = 32;
    private static final int SLOT_SPACING = 40; // крок між слотами (і по X, і по Y)

    // Опис одного слоту одягу: у якому рядку/стовпці грід-сітки він стоїть
    private static class ClothingSlotDef {
        final ResourceLocation icon;
        final int col; // -1 = ліва колонка, 0 = центр, 1 = права колонка
        final int row; // 0 = верхній ряд і далі вниз

        ClothingSlotDef(ResourceLocation icon, int col, int row) {
            this.icon = icon;
            this.col = col;
            this.row = row;
        }
    }

    private final List<ClothingSlotDef> clothingSlots = buildClothingSlots();

    private static List<ClothingSlotDef> buildClothingSlots() {
        List<ClothingSlotDef> list = new ArrayList<>();
        list.add(new ClothingSlotDef(ICON_HAT, 0, 0));       // капелюх - центр, верхній ряд

        list.add(new ClothingSlotDef(ICON_BRA, -1, 1));      // ліфчик - ліва колонка
        list.add(new ClothingSlotDef(ICON_TANKTOP, 0, 1));   // футболка - центр
        list.add(new ClothingSlotDef(ICON_JACKET, 1, 1));    // куртка - права колонка

        list.add(new ClothingSlotDef(ICON_PANTIES, -1, 2));  // трусики - ліва колонка
        list.add(new ClothingSlotDef(ICON_SHORTS, 0, 2));    // шорти - центр

        list.add(new ClothingSlotDef(ICON_SHOES, 0, 3));     // взуття - центр, нижній ряд
        return list;
    }

    private static final int CLOTHING_PANEL_OFFSET_X = 60; // відступ панелі одягу від колонки вкладок
    private static final int REVERSE_BUTTON_SIZE = 28;
    private static final int REVERSE_BUTTON_MARGIN_BOTTOM = 20;

    private enum Tab { INVENTORY, CLOTHES, STATS }

    private Tab currentTab = Tab.CLOTHES;
    private long tabSwitchTime; // момент останнього перемикання вкладки - для анімації появи панелі

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
        this.tabSwitchTime = this.openTime;
        AnyaNetwork.CHANNEL.sendToServer(new PacketAnyaGuiState(this.anya.getEntityId(), true));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        AnyaNetwork.CHANNEL.sendToServer(new PacketAnyaGuiState(this.anya.getEntityId(), false));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float openProgress = this.computeProgress(this.openTime);
        float tabProgress = this.computeProgress(this.tabSwitchTime);

        this.drawHearts(openProgress);
        this.drawSideIcons(mouseX, mouseY, openProgress);

        if (this.currentTab == Tab.CLOTHES) {
            this.drawClothingPanel(mouseX, mouseY, tabProgress);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private float computeProgress(long since) {
        long elapsedTime = System.currentTimeMillis() - since;
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

    private void drawSideIcons(int mouseX, int mouseY, float progress) {
        int targetX = SIDE_ICON_MARGIN_LEFT;
        int startX = -SIDE_ICON_SIZE - 10;
        int currentX = (int) (startX + (targetX - startX) * progress);

        ResourceLocation[] iconsNormal = { ICON_BACKPACK, ICON_SHIRT, ICON_HEART };
        ResourceLocation[] iconsHover = { ICON_BACKPACK_HOVER, ICON_SHIRT_HOVER, ICON_HEART_HOVER };

        for (int i = 0; i < iconsNormal.length; i++) {
            int y = SIDE_ICON_MARGIN_TOP + i * SIDE_ICON_SPACING;
            this.drawIconButton(currentX, y, SIDE_ICON_SIZE, iconsNormal[i], iconsHover[i], mouseX, mouseY);
        }
    }

    /**
     * Панель одягу - з'являється справа від колонки вкладок, коли обрано Tab.CLOTHES.
     * Плюс окрема кнопка "reverse" знизу біля колонки вкладок.
     */
    private void drawClothingPanel(int mouseX, int mouseY, float progress) {
        int baseX = SIDE_ICON_MARGIN_LEFT + SIDE_ICON_SIZE + CLOTHING_PANEL_OFFSET_X;
        int baseY = SIDE_ICON_MARGIN_TOP;

        // Легке виїжджання панелі одягу зверху вниз під час перемикання вкладки
        int slideOffset = (int) ((1.0F - progress) * 15);

        for (ClothingSlotDef slot : this.clothingSlots) {
            int x = baseX + slot.col * SLOT_SPACING;
            int y = baseY + slot.row * SLOT_SPACING - slideOffset;

            GlStateManager.color(1.0F, 1.0F, 1.0F, progress); // фейд-ін разом зі слайдом
            this.drawIconButtonNoColorReset(x, y, SLOT_SIZE, slot.icon, slot.icon, mouseX, mouseY);
        }

        // Кнопка "reverse" - знизу біля колонки вкладок
        int reverseX = SIDE_ICON_MARGIN_LEFT;
        int reverseY = this.height - REVERSE_BUTTON_MARGIN_BOTTOM - REVERSE_BUTTON_SIZE;
        GlStateManager.color(1.0F, 1.0F, 1.0F, progress);
        this.drawIconButtonNoColorReset(reverseX, reverseY, REVERSE_BUTTON_SIZE, ICON_REVERSE, ICON_REVERSE, mouseX, mouseY);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Малює квадратну кнопку-іконку: звичайна текстура, або hover-варіант при наведенні.
     */
    private void drawIconButton(int x, int y, int size, ResourceLocation normal, ResourceLocation hover, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        this.drawIconButtonNoColorReset(x, y, size, normal, hover, mouseX, mouseY);
        GlStateManager.disableBlend();
    }

    // Варіант без скидання кольору/бленду - для використання всередині drawClothingPanel,
    // де альфа-канал (фейд-ін) вже виставлений заздалегідь через GlStateManager.color(...).
    private void drawIconButtonNoColorReset(int x, int y, int size, ResourceLocation normal, ResourceLocation hover, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size;
        this.mc.getTextureManager().bindTexture(hovered ? hover : normal);
        this.drawScaledCustomSizeModalRect(x, y, 0, 0, 16, 16, size, size, 16, 16);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) return;

        // Клік по вкладках зверху
        int tabX = SIDE_ICON_MARGIN_LEFT;
        Tab[] tabs = { Tab.INVENTORY, Tab.CLOTHES, Tab.STATS };

        for (int i = 0; i < tabs.length; i++) {
            int y = SIDE_ICON_MARGIN_TOP + i * SIDE_ICON_SPACING;
            if (mouseX >= tabX && mouseX <= tabX + SIDE_ICON_SIZE && mouseY >= y && mouseY <= y + SIDE_ICON_SIZE) {
                if (this.currentTab != tabs[i]) {
                    this.currentTab = tabs[i];
                    this.tabSwitchTime = System.currentTimeMillis(); // перезапускаємо анімацію появи панелі
                }
                this.playClickSound();
                return;
            }
        }

        // Клік по кожному окремому слоту одягу (поки без реальної логіки одягання - TODO)
        if (this.currentTab == Tab.CLOTHES) {
            int baseX = SIDE_ICON_MARGIN_LEFT + SIDE_ICON_SIZE + CLOTHING_PANEL_OFFSET_X;
            int baseY = SIDE_ICON_MARGIN_TOP;

            for (ClothingSlotDef slot : this.clothingSlots) {
                int x = baseX + slot.col * SLOT_SPACING;
                int y = baseY + slot.row * SLOT_SPACING;
                if (mouseX >= x && mouseX <= x + SLOT_SIZE && mouseY >= y && mouseY <= y + SLOT_SIZE) {
                    this.playClickSound();
                    // TODO: відкрити вибір предмета для цього слоту одягу
                    return;
                }
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
