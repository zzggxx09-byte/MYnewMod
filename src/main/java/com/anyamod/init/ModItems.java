package com.anyamod.init;

import com.anyamod.AnyaMod;
import com.anyamod.item.ItemAnyaSpawnEgg;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Відповідає ТІЛЬКИ за реєстрацію предмета "яйце спавну Anya".
 * Сам клас предмета живе окремо в com.anyamod.item.ItemAnyaSpawnEgg.
 */
@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class ModItems {

    public static ItemAnyaSpawnEgg ANYA_SPAWN_EGG;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ANYA_SPAWN_EGG = new ItemAnyaSpawnEgg(
                ModEntities.ANYA_ENTRY.getEgg().primaryColor,
                ModEntities.ANYA_ENTRY.getEgg().secondaryColor
        );
        ANYA_SPAWN_EGG.setRegistryName(AnyaMod.MODID, "anya_spawn_egg");
        ANYA_SPAWN_EGG.setUnlocalizedName(AnyaMod.MODID + ".anya_spawn_egg");
        ANYA_SPAWN_EGG.setCreativeTab(AnyaMod.TAB_ANYA);

        event.getRegistry().register(ANYA_SPAWN_EGG);
    }
}
