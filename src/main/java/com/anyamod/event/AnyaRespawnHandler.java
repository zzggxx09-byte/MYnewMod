package com.anyamod.event;

import com.anyamod.AnyaMod;
import com.anyamod.data.AnyaRespawnData;
import com.anyamod.entity.EntityAnya;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * СИСТЕМА РЕСПАВНУ ANYA З ЖИТТЯМИ
 * 
 * 1. При спавні: Anya отримує 5 життів
 * 2. При смерті: -1 life, якщо life > 0 → респавн, якщо = 0 → смерть назавжди
 * 3. При новому дні: +1 life (max 5)
 */
@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class AnyaRespawnHandler {

    private static final int RESPAWN_DELAY_TICKS = 600; // 30 секунд

    // ==================== СМЕРТЬ ====================

    /**
     * Коли Anya помирає:
     * 1. -1 life
     * 2. Якщо життя залишилось → респавн
     * 3. Якщо =0 → смерть назавжди (broadcast в чат)
     */
    @SubscribeEvent
    public static void onAnyaDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntity() instanceof EntityAnya)) return;

        World world = event.getEntity().world;
        AnyaRespawnData data = AnyaRespawnData.get(world);

        if (!data.hasHome()) {
            // Немає дому - просто помирає
            return;
        }

        // Позбавити одного життя
        boolean stillAlive = data.loseLife();

        if (!stillAlive) {
            // ========== СМЕРТЬ НАЗАВЖДИ ==========
            
            // Broadcast в чат для всіх гравців
            String deathMessage = TextFormatting.DARK_RED 
                + "❌ Аня мертва назавжди... (0 життів)" 
                + TextFormatting.RESET;
            
            world.getPlayers(net.minecraft.entity.player.EntityPlayer.class, player -> true)
                    .forEach(player -> player.sendMessage(new TextComponentString(deathMessage)));
            
            System.out.println("[AnyaMod] Anya умерла назавжди!");
            
            // Не робимо респавн
            return;
        }

        // ========== ЩЕ ЖИВЕ ==========
        
        int livesLeft = data.getLives();
        
        // Broadcast в чат
        String respawnMessage = TextFormatting.GOLD 
            + "⚠ Аня помирає... Лишилось " + livesLeft + "/" + data.getMaxLives() + " життів. "
            + "Респавн за 30 секунд..." 
            + TextFormatting.RESET;
        
        world.getPlayers(net.minecraft.entity.player.EntityPlayer.class, player -> true)
                .forEach(player -> player.sendMessage(new TextComponentString(respawnMessage)));
        
        // Встановити respawn countdown
        data.startRespawnCountdown(RESPAWN_DELAY_TICKS);
    }

    // ==================== РЕСПАВН ====================

    /**
     * Кожен тік світу: проверяємо респавн та новий день
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isRemote) return;

        AnyaRespawnData data = AnyaRespawnData.get(event.world);

        // 1. Проверяємо respawn countdown
        if (data.isPendingRespawn()) {
            if (data.tickRespawnCountdown()) {
                respawnAnya(event.world, data.getHome());
            }
            return; // Не перевіряємо день якщо в процесі респавну
        }

        // 2. Проверяємо новий день (+1 life)
        checkDailyLifeRegen(event.world, data);
    }

    /**
     * Респавнити Anya на дому
     */
    private static void respawnAnya(World world, BlockPos homePos) {
        EntityAnya entity = new EntityAnya(world);
        entity.setLocationAndAngles(
                homePos.getX() + 0.5D,
                homePos.getY() + 1.0D,  // +1 блок щоб не застрявала
                homePos.getZ() + 0.5D,
                0.0F, 0.0F
        );
        entity.setHealth(entity.getMaxHealth());
        world.spawnEntity(entity);

        // Broadcast про успішний респавн
        String respawnSuccessMessage = TextFormatting.GREEN 
            + "✓ Аня відродилась!" 
            + TextFormatting.RESET;
        
        world.getPlayers(net.minecraft.entity.player.EntityPlayer.class, player -> true)
                .forEach(player -> player.sendMessage(new TextComponentString(respawnSuccessMessage)));
    }

    // ==================== НОВИЙ ДЕНЬ (+1 LIFE) ====================

    /**
     * Кожен день: якщо livesCount < 5, то +1 life
     * День змінюється коли world.getWorldTime() стає кратно 24000
     */
    private static void checkDailyLifeRegen(World world, AnyaRespawnData data) {
        // Не робимо ничего якщо вона вже мертва
        if (data.isDeadForever()) {
            return;
        }

        long worldTime = world.getWorldTime();
        
        // Спробуємо дати life за новий день
        boolean gaveLife = data.tryGiveLifeForNewDay(worldTime);

        if (gaveLife) {
            int newLives = data.getLives();
            
            // Broadcast в чат про поновлене життя
            String lifeRegenMessage = TextFormatting.AQUA 
                + "✨ Аня почуває себе краще! +" 
                + TextFormatting.GREEN + "1 " 
                + TextFormatting.AQUA + "life " 
                + TextFormatting.GRAY + "(" + newLives + "/" + data.getMaxLives() + ")"
                + TextFormatting.RESET;
            
            world.getPlayers(net.minecraft.entity.player.EntityPlayer.class, player -> true)
                    .forEach(player -> player.sendMessage(new TextComponentString(lifeRegenMessage)));
            
            System.out.println("[AnyaMod] Anya отримала +1 life! Тепер: " + newLives);
        }
    }
}
