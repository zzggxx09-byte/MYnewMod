package com.anyamod.proxy;

import com.anyamod.entity.EntityAnya;
import com.anyamod.init.ModItems;
import net.minecraft.client.renderer.entity.RenderVillager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        registerEntityRenderers();
        registerItemModels();
    }

    private void registerEntityRenderers() {
        // Поки що використовуємо стандартний рендер/модель жителя.
        // Пізніше тут можна підмінити на кастомний RenderLiving з новою моделлю.
        RenderingRegistry.registerEntityRenderingHandler(EntityAnya.class, RenderVillager::new);
    }

    private void registerItemModels() {
        // Стандартна текстура-заготовка Forge для яєць спавну (тінтується кольорами з ModEntities)
        ModelLoader.setCustomModelResourceLocation(
                ModItems.ANYA_SPAWN_EGG, 0,
                new ModelResourceLocation("forge:spawn_egg", "inventory")
        );
    }
}
