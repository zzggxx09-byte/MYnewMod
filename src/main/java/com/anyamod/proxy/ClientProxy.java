package com.anyamod.proxy;

import com.anyamod.entity.EntityAnya;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        registerEntityRenderers();
        // Модель яйця реєструється окремо в ClientModelEvents.java на ModelRegistryEvent.
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
}
