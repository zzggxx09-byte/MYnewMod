package com.anyamod.entity;

import com.anyamod.entity.ai.EntityAICounterAttack;
import com.anyamod.entity.ai.EntityAIEyeContact;
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

    // Звичайна швидкість ходи - близька до гравця.
    // Раніше wander-таск множив цю швидкість на 0.6, тому вона плелась як житель.
    private static final double NORMAL_SPEED = 0.25D;

    // Рідкісний повільний режим "оглядається" - помітно повільніше за звичайний.
    private static final double CAUTIOUS_SPEED = 0.09D;

    // Раз на скільки тіків (в середньому) перевіряємо, чи не пора змінити режим.
    private static final int MODE_CHECK_INTERVAL = 100; // ~5 сек

    // Ймовірність (в %) піти в "cautious" режим при кожній перевірці.
    private static final int CAUTIOUS_CHANCE_PERCENT = 15;

    // Скільки тіків триває один "cautious" епізод, коли він стався.
    private static final int CAUTIOUS_DURATION_MIN = 40;  // 2 сек
    private static final int CAUTIOUS_DURATION_MAX = 100; // 5 сек

    private int modeTimer;
    private boolean cautious;

    public EntityAnya(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.95F);
        this.setCustomNameTag(AnyaNameTag.NAME);
        this.setAlwaysRenderNameTag(true);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAICounterAttack(this));
        this.tasks.addTask(2, new EntityAIAvoidEntity<>(this, EntityMob.class, 8.0F, 1.0D, 1.2D));
        this.tasks.addTask(3, new EntityAIEyeContact(this));
        this.tasks.addTask(4, new EntityAISeekShelterFromRain(this, 1.0D));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        // Множник тепер 1.0 - реальну швидкість дає атрибут MOVEMENT_SPEED,
        // яким ми керуємо самі в onLivingUpdate() (два режими).
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        // targetTasks навмисно порожній - вона не переслідує, лише контратакує впритул
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(NORMAL_SPEED);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!this.world.isRemote) {
            updateMovementMode();
        }
    }

    /**
     * Раз на MODE_CHECK_INTERVAL тіків - шанс перейти в повільний "cautious" режим
     * (ніби оглядається), або повернутись у звичайний темп, якщо епізод скінчився.
     */
    private void updateMovementMode() {
        if (this.modeTimer > 0) {
            this.modeTimer--;
            return;
        }

        if (this.cautious) {
            this.cautious = false;
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(NORMAL_SPEED);
            this.modeTimer = MODE_CHECK_INTERVAL;
        } else if (this.rand.nextInt(100) < CAUTIOUS_CHANCE_PERCENT) {
            this.cautious = true;
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(CAUTIOUS_SPEED);
            this.modeTimer = CAUTIOUS_DURATION_MIN
                    + this.rand.nextInt(CAUTIOUS_DURATION_MAX - CAUTIOUS_DURATION_MIN + 1);
        } else {
            this.modeTimer = MODE_CHECK_INTERVAL;
        }
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
