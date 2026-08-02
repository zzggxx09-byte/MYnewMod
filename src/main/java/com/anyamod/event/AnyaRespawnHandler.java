package com.anyamod.event;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * СИСТЕМА РЕСПАВНУ ANYA З ЖИТТЯМИ (ПОВНІСТЮ ПЕРЕДІЛАНА)
 * 
 * ✓ Система живих ПОВНІСТЮ в EntityAnya.java
 * ✓ Кожна Anya має свої livesCount, homePos, тощо
 * ✓ NBT сохранення в EntityAnya
 * ✓ AnyaRespawnData більш НЕ ВИКОРИСТОВУЄТЬСЯ
 * 
 * Цей обробник тільки:
 * - Обробляє LivingDeathEvent
 * - Вызивает anya.loseLife()
 * - Встановлює каунтдаун на респавн
 */
@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class AnyaRespawnHandler {

    private static final int RESPAWN_DELAY_TICKS = 600; // 30 секунд
    
    // Тимчасовий хранилище для очікування респавну
    // Ключ: entityId, Значення: тики до респавну
    private static final Map<Integer, Integer> pendingRespawns = new HashMap<>();
    private static final Map<Integer, BlockPos> respawnPositions = new HashMap<>();

    // ==================== СМЕРТЬ ====================

    /**
     * Коли EntityAnya помирає:
     * 1. Вызываємо anya.loseLife() (це змінює livesCount в NBT)
     * 2. Якщо livesCount > 0: встановлюємо каунтдаун на респавн
     * 3. Якщо livesCount = 0: мертва назавжди (нема респавну)
     */
    @SubscribeEvent
    public static void onAnyaDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntity() instanceof EntityAnya)) return;

        EntityAnya anya = (EntityAnya) event.getEntity();
        World world = anya.world;

        System.out.println("[AnyaMod] onAnyaDeath: Anya (ID: " + anya.getEntityId() + ") помирає");
        System.out.println("[AnyaMod]   Житів ДО: " + anya.getLives());

        if (!anya.hasHome()) {
            System.out.println("[AnyaMod]   Немає дому - просто помирає");
            return;
        }

        // ========== ПОЗБАВИТИ ОДНОГО ЖИТТЯ ==========
        boolean stillAlive = anya.loseLife();
        
        System.out.println("[AnyaMod]   Житів ПІСЛЯ loseLife(): " + anya.getLives());
        System.out.println("[AnyaMod]   isDeadForever: " + anya.isDeadForever());

        if (!stillAlive) {
            // ========== СМЕРТЬ НАЗАВЖДИ ==========
            
            String deathMessage = TextFormatting.DARK_RED 
                + "❌ Аня мертва назавжди... (0 живій)" 
                + TextFormatting.RESET;
            
            world.getPlayers(net.minecraft.entity.player.EntityPlayer.class, player -> true)
                    .forEach(player -> player.sendMessage(new TextComponentString(deathMessage)));
            
            System.out.println("[AnyaMod] Anya (ID: " + anya.getEntityId() + ") умерла назавжди!");
            
            // Не робимо респавн
            return;
        }

        // ========== ЩЕ ЖИВЕ ==========
        
        int livesLeft = anya.getLives();
        
        String respawnMessage = TextFormatting.GOLD 
            + "⚠ Аня помирає... Лишилось " + livesLeft + "/" + anya.getMaxLives() + " живій. "
            + "Респавн за 30 секунд..." 
            + TextFormatting.RESET;
        
        world.getPlayers(net.minecraft.entity.player.EntityPlayer.class, player -> true)
                .forEach(player -> player.sendMessage(new TextComponentString(respawnMessage)));
        
        // Встановити respawn countdown ДЛЯ ЦІЄЇ Anya
        int entityId = anya.getEntityId();
        pendingRespawns.put(entityId, RESPAWN_DELAY_TICKS);
        respawnPositions.put(entityId, anya.getHome());
        
        System.out.println("[AnyaMod] Anya (ID: " + entityId + ") має " + livesLeft + " живій.");
        System.out.println("[AnyaMod] Встановлено каунтдаун на " + RESPAWN_DELAY_TICKS + " тиків");
    }

    // ==================== РЕСПАВН ====================

    /**
     * Кожен тік світу: обновляємо каунтдауни респавну для КОЖНОЇ Anya
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isRemote) return;

        // Обновити всі каунтдауни респавну
        Iterator<Map.Entry<Integer, Integer>> iterator = pendingRespawns.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int entityId = entry.getKey();
            int ticksLeft = entry.getValue();
            
            ticksLeft--;
            
            if (ticksLeft <= 0) {
                // РЕСПАВН!
                BlockPos respawnPos = respawnPositions.get(entityId);
                respawnAnya(event.world, respawnPos);
                
                iterator.remove();
                respawnPositions.remove(entityId);
            } else {
                // Обновити каунтдаун
                pendingRespawns.put(entityId, ticksLeft);
            }
        }
    }

    /**
     * Респавнити Anya на дому
     */
    private static void respawnAnya(World world, BlockPos homePos) {
        EntityAnya entity = new EntityAnya(world);
        entity.setLocationAndAngles(
                homePos.getX() + 0.5D,
                homePos.getY() + 1.0D,
                homePos.getZ() + 0.5D,
                0.0F, 0.0F
        );
        entity.setHome(homePos);  // ВАЖЛИВО: встановити дім заново
        entity.setHealth(entity.getMaxHealth());
        world.spawnEntity(entity);

        String respawnSuccessMessage = TextFormatting.GREEN 
            + "✓ Аня відродилась!" 
            + TextFormatting.RESET;
        
        world.getPlayers(net.minecraft.entity.player.EntityPlayer.class, player -> true)
                .forEach(player -> player.sendMessage(new TextComponentString(respawnSuccessMessage)));
        
        System.out.println("[AnyaMod] Anya (ID: " + entity.getEntityId() + ") респавнена в " + homePos);
    }
                                    }
