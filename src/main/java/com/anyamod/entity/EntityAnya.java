package com.anyamod.entity;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * ЗАГОТОВКА моба "Anya".
 * Поки що це просто житель (модель/анімації/AI успадковані від EntityVillager).
 * Текст ніка живе окремо в AnyaNameTag.java.
 *
 * У майбутньому:
 *  - замінити super-клас або рендер на кастомну модель
 *  - прибрати/змінити торгівлю (зараз вимкнена через processInteract)
 *  - додати власний AI/діалоги
 */
public class EntityAnya extends EntityVillager {

    public EntityAnya(World worldIn) {
        super(worldIn);
        this.setCustomNameTag(AnyaNameTag.NAME);
        this.setAlwaysRenderNameTag(true);
        // Прибираємо професію-специфічний вигляд (одяг), лишаємо базову модель жителя
        this.setProfession(0);
    }

    @Override
    public boolean getAlwaysRenderNameTagForRender() {
        // Явний override гарантує, що нік видно завжди (як у гравця),
        // а не тільки коли хрестик наведено на моба.
        return true;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        // Заготовка: правий клік нічого не робить (торгівля вимкнена),
        // щоб не заважало майбутній кастомній логіці NPC.
        return true;
    }

    @Override
    public EntityVillager createChild(EntityAgeable ageable) {
        return null;
    }
}
