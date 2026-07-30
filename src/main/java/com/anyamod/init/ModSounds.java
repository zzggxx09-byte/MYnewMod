package com.anyamod.init;

import com.anyamod.AnyaMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder(AnyaMod.MODID)
@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class ModSounds {

    // Створюємо самі SoundEvent об'єкти
    public static final SoundEvent ANYA_AMBIENT = createSoundEvent("entity.anya.ambient");
    public static final SoundEvent ANYA_HURT = createSoundEvent("entity.anya.hurt");

    private static SoundEvent createSoundEvent(String soundName) {
        ResourceLocation location = new ResourceLocation(AnyaMod.MODID, soundName);
        return new SoundEvent(location).setRegistryName(location);
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
                ANYA_AMBIENT,
                ANYA_HURT
        );
    }
}
