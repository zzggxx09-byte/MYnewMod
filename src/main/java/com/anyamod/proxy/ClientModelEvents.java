package com.anyamod.proxy;

import com.anyamod.AnyaMod;
import com.anyamod.init.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Реєстрація моделі яйця має відбутись САМЕ на ModelRegistryEvent.
 */
@Mod.EventBusSubscriber(modid = AnyaMod.MODID, value = Side.CLIENT)
public class ClientModelEvents {

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(
                ModItems.ANYA_SPAWN_EGG, 0,
                new ModelResourceLocation(AnyaMod.MODID + ":anya_spawn_egg", "inventory")
        );
    }
}
