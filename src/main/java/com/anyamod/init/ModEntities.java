package com.anyamod.init;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class ModEntities {

    private static int entityId = 0;

    // Реєструємо саму сутність + яйце спавну (кольори тимчасові - зелений/білий, як заготовка)
    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        EntityEntry anyaEntry = EntityEntryBuilder.create()
                .entity(EntityAnya.class)
                .id(new ResourceLocation(AnyaMod.MODID, "anya"), entityId++)
                .name("anya")
                .tracker(64, 3, true)
                .egg(0x4CAF50, 0xFFFFFF) // основний / плямистий кольори яйця
                .build();

        event.getRegistry().register(anyaEntry);
    }

    // Викликається з preInit головного класу - реєстрація рендера роблена в ClientProxy,
    // тут місце залишено для майбутньої реєстрації текстур/лутів тощо.
    public static void registerRenders() {
        // заготовка на майбутнє
    }
}
