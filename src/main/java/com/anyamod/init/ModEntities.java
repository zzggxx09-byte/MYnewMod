package com.anyamod.init;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

/**
 * Відповідає ТІЛЬКИ за реєстрацію сутності Anya.
 * Реєстрація предмета-яйця винесена в ModItems.java.
 *
 * ANYA_ENTRY зроблено static final полем: воно створюється в момент завантаження
 * класу (ще до будь-яких RegistryEvent), тому ModItems може безпечно
 * використати цей об'єкт незалежно від порядку подій Entity/Item.
 */
@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class ModEntities {

    public static final EntityEntry ANYA_ENTRY = EntityEntryBuilder.create()
            .entity(EntityAnya.class)
            .id(new ResourceLocation(AnyaMod.MODID, "anya"), 0)
            .name("anya")
            .tracker(64, 3, true)
            .egg(0x4CAF50, 0xFFFFFF) // основний / плямистий кольори яйця
            .build();

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().register(ANYA_ENTRY);
    }
}
