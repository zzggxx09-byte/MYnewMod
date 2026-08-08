package com.anyamod.proxy;

import com.anyamod.entity.EntityAnya;
import com.anyamod.network.AnyaNetwork;
import com.anyamod.network.PacketGiveItemToAnya;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

    public static final KeyBinding KEY_GIVE_ITEM = new KeyBinding(
            "key.anyamod.give_item",
            KeyConflictContext.IN_GAME,
            Keyboard.KEY_G,
            "key.categories.anyamod"
    );

    @Override
    public void registerRenderers() {
        registerEntityRenderers();
        // Модель яйця реєструється окремо в ClientModelEvents.java на ModelRegistryEvent.

        ClientRegistry.registerKeyBinding(KEY_GIVE_ITEM);
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }

    @Override
    public void openAnyaGui(com.anyamod.entity.EntityAnya anya, net.minecraft.entity.player.EntityPlayer player) {
        net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(
                new com.anyamod.client.gui.GuiAnyaInterface(anya)
        );
    }

    private void registerEntityRenderers() {
        // Тепер Anya рендериться моделлю гравця (slim/Alex) зі своєю текстурою-скіном,
        // а не моделлю жителя. Клас RenderAnya - в окремому файлі RenderAnya.java.
        RenderingRegistry.registerEntityRenderingHandler(EntityAnya.class, RenderAnya::new);
    }

    /**
     * Слухає натискання клавіші G: якщо гравець дивиться хрестиком на Аню
     * і тримає предмет в руці - шле пакет на сервер, щоб Аня забрала предмет собі.
     */
    @SideOnly(Side.CLIENT)
    public static class KeyInputHandler {
        @SubscribeEvent
        public void onKeyInput(InputEvent.KeyInputEvent event) {
            if (!KEY_GIVE_ITEM.isPressed()) return;

            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.player;
            if (player == null) return;

            if (player.getHeldItemMainhand().isEmpty()) return;

            RayTraceResult trace = mc.objectMouseOver;
            if (trace == null || trace.entityHit == null) return;

            Entity target = trace.entityHit;
            if (!(target instanceof EntityAnya)) return;

            AnyaNetwork.CHANNEL.sendToServer(new PacketGiveItemToAnya(target.getEntityId()));
        }
    }
}
