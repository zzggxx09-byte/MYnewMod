package com.anyamod.proxy;

import com.anyamod.entity.EntityAnya;
import net.minecraft.client.renderer.entity.RenderVillager;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        registerEntityRenderers();
        // Модель яйця реєструється окремо в ClientModelEvents.java на ModelRegistryEvent.
        // Тінт (IItemColor) прибрано - тепер яйце має свою текстуру,
        // тінтування лише спотворювало колір, накладаючи зелений/білий поверх картинки.
    }

    private void registerEntityRenderers() {
        // Поки що використовуємо стандартний рендер/модель жителя.
        // Пізніше тут можна підмінити на кастомний RenderLiving з новою моделлю.
        RenderingRegistry.registerEntityRenderingHandler(EntityAnya.class, RenderVillager::new);
    }
}
