package com.anyamod.client;

import com.anyamod.AnyaMod;
import com.anyamod.client.gui.GuiAnyaInterface;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = AnyaMod.MODID, value = Side.CLIENT)
public class AnyaGuiOverlayHandler {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiAnyaInterface)) {
            return;
        }

        switch (event.getType()) {
            case HOTBAR:
            case ARMOR:
            case HEALTH:
            case FOOD:
            case EXPERIENCE:
            case JUMPBAR:
            case AIR:
                event.setCanceled(true);
                break;
            default:
                break;
        }
    }
}
