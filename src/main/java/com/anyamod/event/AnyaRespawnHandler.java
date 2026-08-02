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

/**
 * СИСТЕМА РЕСПАВНУ ANYA З ЖИТТЯМИ (ІНДИВІДУАЛЬНІ)
 * 
 * КОЖНА ANYA має свої livesCount, homePos, тощо!
 * Дані зберігаються в NBT EntityAnya, а не у WorldSavedData.
 */
@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class AnyaRespawnHandler {

    private static final int RESPAWN_DELAY_TICKS = 600; // 30 секунд
    
    // Тимчасовий хранилище для очікування респавну
    // Ключ: entityId, Значення: тики до респавну
    private static java.util.Map<Integer, Integer> pendingRespawns = new java.util.HashMap<>();
    private static java.util.Map<Integer, BlockPos> respawnPositions = new java.util.HashMap<>();

    // ==================== СМЕРТЬ ====================

    /**
     * Коли Anya помирає:
     * 1. -1 life (з её NBT)
     * 2. Якщо життя залишилось → встановити респавн
     * 3. Якщо =0 → смерть назавжди (broadcast в чат)
     */
    @SubscribeEvent
    public static void onAnyaDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntity() instanceof EntityAnya)) return;

        EntityAnya anya = (EntityAnya) event.getEntity();
        World world = anya.world;

        if (!anya.hasHome()) {
            // Немає дому - просто помирає
            return;
        }

        // Позбавити одного життя ЦІЄЇ конкретної Anya
        boolean stillAlive = anya.loseLife();

        if (!stillAlive) {
            // ========== СМЕРТЬ НАЗАВЖДИ ==========
            
            String deathMessage = TextFormatting.DARK_RED 
                + "❌ Аня мертва назавжди... (0 живів)" 
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
    }

    // ==================== РЕСПАВН ====================

    /**
     * Кожен тік світу: проверяємо респавн для КОЖНОЇ Anya
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isRemote) return;

        // Обновити всі каунтдауни респавну
        java.util.Iterator<java.util.Map.Entry<Integer, Integer>> iterator = 
            pendingRespawns.entrySet().iterator();
        
        while (iterator.hasNext()) {
            java.util.Map.Entry<Integer, Integer> entry = iterator.next();
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
        
        System.out.println("[AnyaMod] Anya респавнена в " + homePos);
    }
}
