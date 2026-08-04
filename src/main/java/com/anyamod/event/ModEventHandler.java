package com.anyamod.event;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class ModEventHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (!(event.getEntity() instanceof EntityMob)) return;

        EntityMob mob = (EntityMob) event.getEntity();

        // Захист від повторного додавання таска при кожній перезавантаженні чанку
        if (mob.getEntityData().getBoolean("anyamod_targets_anya")) return;
        mob.getEntityData().setBoolean("anyamod_targets_anya", true);

        mob.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(mob, EntityAnya.class, true));
    }
}

@SubscribeEvent
public static void onAnyaJoinWorld(EntityJoinWorldEvent event) {
    if (event.getWorld().isRemote) return;
    if (!(event.getEntity() instanceof EntityAnya)) return;

    EntityAnya newAnya = (EntityAnya) event.getEntity();
    boolean alreadyExists = event.getWorld()
            .getEntities(EntityAnya.class, a -> a != newAnya && a.isEntityAlive())
            .stream().findAny().isPresent();

    if (alreadyExists) {
        event.setCanceled(true);
    }
}
