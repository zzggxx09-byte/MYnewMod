package com.anyamod.init;

import com.anyamod.AnyaMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class ModSounds {

    public static final SoundEvent ANYA_AMBIENT = createSound("entity.anya.ambient");
    public static final SoundEvent ANYA_HURT = createSound("entity.anya.hurt");

    private static SoundEvent createSound(String name) {
        ResourceLocation loc = new ResourceLocation(AnyaMod.MODID, name);
        return new SoundEvent(loc).setRegistryName(loc);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
                ANYA_AMBIENT,
                ANYA_HURT
        );
    }
}
