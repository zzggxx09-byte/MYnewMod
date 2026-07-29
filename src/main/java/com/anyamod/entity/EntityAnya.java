package com.anyamod.entity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * Моб "Anya".
 * Базується на EntityCreature (базовий "живий" моб з пересуванням),
 * а не на EntityVillager - тому немає торгівлі, розмноження,
 * втечі від зомбі та іншої поведінки жителя.
 * Рендериться моделлю гравця через RenderAnya (див. proxy/RenderAnya.java).
 * Текст ніка живе окремо в AnyaNameTag.java.
 */
public class EntityAnya extends EntityCreature {

    public EntityAnya(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.95F); // розміри як у гравця
        this.setCustomNameTag(AnyaNameTag.NAME);
        this.setAlwaysRenderNameTag(true);
    }

    @Override
    protected void applyEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(2, new EntityAIWanderAvoidWater(this, 0.6D));
        this.tasks.addTask(3, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public boolean getAlwaysRenderNameTagForRender() {
        return true;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        // Заготовка: правий клік нічого не робить,
        // щоб не заважало майбутній кастомній логіці NPC.
        return true;
    }
}
