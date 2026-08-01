package com.anyamod.event;

import com.anyamod.AnyaMod;
import com.anyamod.data.AnyaRespawnData;
import com.anyamod.entity.EntityAnya;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class AnyaRespawnHandler {

    private static final int RESPAWN_DELAY_TICKS = 600; // 30 секунд

    @SubscribeEvent
    public static void onAnyaDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntity() instanceof EntityAnya)) return;

        AnyaRespawnData data = AnyaRespawnData.get(event.getEntity().world);
        if (data.hasHome()) {
            data.startRespawnCountdown(RESPAWN_DELAY_TICKS);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isRemote) return;

        AnyaRespawnData data = AnyaRespawnData.get(event.world);
        if (!data.isPendingRespawn()) return;

        if (data.tickRespawnCountdown()) {
            respawnAnya(event.world, data.getHome());
        }
    }

    private static void respawnAnya(World world, BlockPos homePos) {
        EntityAnya entity = new EntityAnya(world);
        entity.setLocationAndAngles(
                homePos.getX() + 0.5D,
                homePos.getY(),
                homePos.getZ() + 0.5D,
                0.0F, 0.0F
        );
        entity.setHealth(entity.getMaxHealth());
        world.spawnEntity(entity);
    }
}
