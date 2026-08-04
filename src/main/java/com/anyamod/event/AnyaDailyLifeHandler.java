package com.anyamod.event;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Слідкує за настанням нового дня в кожному вимірі (dimension) і видає
 * всім заспавненим Anya в цьому вимірі +1 життя (до максимуму 5).
 *
 * "Новий день" визначається як зміна цілого числа world.getWorldTime() / 24000
 * (0 = перший день, 1 = другий і т.д.) - той самий підрахунок,
 * яким користується ванільний лічильник днів.
 */
@Mod.EventBusSubscriber(modid = AnyaMod.MODID)
public class AnyaDailyLifeHandler {

    // Ключ - id виміру (dimension), значення - останній оброблений день у ньому
    private static final Map<Integer, Long> lastDayByDimension = new HashMap<>();

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isRemote) return;

        World world = event.world;
        int dimensionId = world.provider.getDimension();
        long currentDay = world.getWorldTime() / 24000L;

        Long lastDay = lastDayByDimension.get(dimensionId);

        if (lastDay == null) {
            // Перший тік для цього виміру - просто запам'ятовуємо поточний день,
            // без видачі життя (щоб не давати бонус одразу при заході в світ).
            lastDayByDimension.put(dimensionId, currentDay);
            return;
        }

        if (currentDay > lastDay) {
            lastDayByDimension.put(dimensionId, currentDay);
            grantDailyLifeToAllAnya(world);
        }
    }

    /**
     * Проходить по всіх Anya в даному світі й додає їм по одному життю.
     */
    private static void grantDailyLifeToAllAnya(World world) {
        List<EntityAnya> allAnya = world.getEntities(EntityAnya.class, anya -> true);

        for (EntityAnya anya : allAnya) {
            if (anya.isDeadForever()) {
                continue; // мертвій назавжди життя не видаємо
            }

            int before = anya.getLives();
            anya.addLife();
            int after = anya.getLives();

            if (after > before) {
                System.out.println("[AnyaMod] Новий день: Anya (ID: " + anya.getEntityId()
                        + ") отримала +1 життя (" + before + " -> " + after + ")");
            }
        }

        if (!allAnya.isEmpty()) {
            String message = TextFormatting.AQUA
                    + "☀ Настав новий день - всі Ані відновили частину життів!"
                    + TextFormatting.RESET;

            world.getPlayers(net.minecraft.entity.player.EntityPlayer.class, player -> true)
                    .forEach(player -> player.sendMessage(new TextComponentString(message)));
        }
    }
    }
