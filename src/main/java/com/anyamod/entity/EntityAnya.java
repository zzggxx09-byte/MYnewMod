package com.anyamod.entity;

import com.anyamod.entity.ai.EntityAISeekShelterFromRain;
import com.anyamod.init.ModSounds;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityAnya extends EntityCreature {

    public EntityAnya(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.95F);
        this.setCustomNameTag("Anya");
        this.setAlwaysRenderNameTag(true);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.2D, true));
        this.tasks.addTask(2, new EntityAIAvoidEntity<>(this, EntityMob.class, 8.0F, 1.0D, 1.2D));
        this.tasks.addTask(3, new EntityAISeekShelterFromRain(this, 1.0D));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.6D));
        this.tasks.addTask(6, new EntityAILookIdle(this));

        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityMob.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);

        // Радіус, у межах якого вона взагалі "помічає" мобів для атаки (близька дистанція)
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(4.0D);

        // ATTACK_DAMAGE не зареєстрований базовим EntityCreature - без цього краш при атаці
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ANYA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSounds.ANYA_HURT;
    }

    @Override
    public int getTalkInterval() {
        return 160;
    }

    @Override
    public boolean getAlwaysRenderNameTagForRender() {
        return true;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        return true;
    }
}
