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
 * СИСТЕМА РЕСПАВНУ ANYA З ЖИТТЯМИ
 *
 * Обробляє LivingDeathEvent, викликає anya.loseLife() і встановлює
 * каунтдаун на респавн. При респавні переносить реальну кількість
 * життів, що лишилась, у нову сутність (щоб життя справді витрачались).
 */
@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class AnyaRespawnHandler {

    private static final int RESPAWN_DELAY_TICKS = 600; // 30 секунд

    private static final Map<Integer, Integer> pendingRespawns = new HashMap<>();
    private static final Map<Integer, BlockPos> respawnPositions = new HashMap<>();
    private static final Map<Integer, Integer> respawnLives = new HashMap<>(); // ДОДАНО

    // ==================== СМЕРТЬ ====================

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

        boolean stillAlive = anya.loseLife();

        System.out.println("[AnyaMod]   Житів ПІСЛЯ loseLife(): " + anya.getLives());
        System.out.println("[AnyaMod]   isDeadForever: " + anya.isDeadForever());

        if (!stillAlive) {
            String deathMessage = TextFormatting.DARK_RED
                    + "❌ Аня мертва назавжди... (0 живій)"
                    + TextFormatting.RESET;

            world.getPlayers(net.minecraft.entity.player.EntityPlayer.class, player -> true)
                    .forEach(player -> player.sendMessage(new TextComponentString(deathMessage)));

            System.out.println("[AnyaMod] Anya (ID: " + anya.getEntityId() + ") умерла назавжди!");
            return;
        }

        int livesLeft = anya.getLives();

        String respawnMessage = TextFormatting.GOLD
                + "⚠ Аня помирає... Лишилось " + livesLeft + "/" + anya.getMaxLives() + " живій. "
                + "Респавн за 30 секунд..."
                + TextFormatting.RESET;

        world.getPlayers(net.minecraft.entity.player.EntityPlayer.class, player -> true)
                .forEach(player -> player.sendMessage(new TextComponentString(respawnMessage)));

        int entityId = anya.getEntityId();
        pendingRespawns.put(entityId, RESPAWN_DELAY_TICKS);
        respawnPositions.put(entityId, anya.getHome());
        respawnLives.put(entityId, livesLeft); // ДОДАНО - зберігаємо залишок життів

        System.out.println("[AnyaMod] Anya (ID: " + entityId + ") має " + livesLeft + " живій.");
        System.out.println("[AnyaMod] Встановлено каунтдаун на " + RESPAWN_DELAY_TICKS + " тиків");
    }

    // ==================== РЕСПАВН ====================

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isRemote) return;

        Iterator<Map.Entry<Integer, Integer>> iterator = pendingRespawns.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            int entityId = entry.getKey();
            int ticksLeft = entry.getValue();

            ticksLeft--;

            if (ticksLeft <= 0) {
                BlockPos respawnPos = respawnPositions.get(entityId);
                int livesLeft = respawnLives.get(entityId); // ДОДАНО
                respawnAnya(event.world, respawnPos, livesLeft); // змінено виклик

                iterator.remove();
                respawnPositions.remove(entityId);
                respawnLives.remove(entityId); // ДОДАНО
            } else {
                pendingRespawns.put(entityId, ticksLeft);
            }
        }
    }

    /**
     * Респавнити Anya на дому з переданою кількістю життів,
     * що лишилась від попередньої смерті.
     */
    private static void respawnAnya(World world, BlockPos homePos, int livesLeft) {
        EntityAnya entity = new EntityAnya(world);
        entity.setLocationAndAngles(
                homePos.getX() + 0.5D,
                homePos.getY() + 1.0D,
                homePos.getZ() + 0.5D,
                0.0F, 0.0F
        );
        entity.setHome(homePos);
        entity.setLives(livesLeft); // ДОДАНО - переносимо реальний залишок життів
        entity.setHealth(entity.getMaxHealth());
        world.spawnEntity(entity);

        String respawnSuccessMessage = TextFormatting.GREEN
                + "✓ Аня відродилась! Житів: " + entity.getLives() + "/" + entity.getMaxLives()
                + TextFormatting.RESET;

        world.getPlayers(net.minecraft.entity.player.EntityPlayer.class, player -> true)
                .forEach(player -> player.sendMessage(new TextComponentString(respawnSuccessMessage)));

        System.out.println("[AnyaMod] Anya (ID: " + entity.getEntityId() + ") респавнена в " + homePos
                + " з " + entity.getLives() + " життями");
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
