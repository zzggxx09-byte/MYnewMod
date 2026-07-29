package com.anyamod.init;

import com.anyamod.AnyaMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Відповідає ТІЛЬКИ за предмет "яйце спавну Anya".
 * Раніше .egg(...) в EntityEntryBuilder лише зберігав кольори,
 * а сам предмет ніде не реєструвався - тому яйця не було у творчому інвентарі.
 */
@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class ModItems {

    public static Item ANYA_SPAWN_EGG;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ANYA_SPAWN_EGG = new ItemMonsterPlacer(
                ModEntities.ANYA_ENTRY,
                ModEntities.ANYA_ENTRY.getEgg().getPrimaryColor(),
                ModEntities.ANYA_ENTRY.getEgg().getSecondaryColor()
        );
        ANYA_SPAWN_EGG.setRegistryName(AnyaMod.MODID, "anya_spawn_egg");
        ANYA_SPAWN_EGG.setUnlocalizedName(AnyaMod.MODID + ".anya_spawn_egg");
        ANYA_SPAWN_EGG.setCreativeTab(AnyaMod.TAB_ANYA);

        event.getRegistry().register(ANYA_SPAWN_EGG);
    }
}
