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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Anya - кастомний моб з AI та системою 5 живів
 * 
 * КОЖНА ANYA має:
 * - 5 живів (livesCount)
 * - Свою точку дому (для респавну)
 * - Кастомний AI (избегание монстрів, контр-атака, тощо)
 * - NBT збереження для персистентності
 */
public class EntityAnya extends EntityCreature {

    // ==================== AI ПАРАМЕТРИ ====================

    // Звичайна швидкість ходи - близька до гравця.
    private static final double NORMAL_SPEED = 0.25D;

    // Повільний режим "оглядається" - помітно повільніше за звичайний.
    private static final double CAUTIOUS_SPEED = 0.09D;

    // Раз на скільки тіків перевіряємо, чи не пора змінити режим.
    private static final int MODE_CHECK_INTERVAL = 100; // ~5 сек

    // Ймовірність (в %) піти в "cautious" режим при кожній перевірці.
    private static final int CAUTIOUS_CHANCE_PERCENT = 15;

    // Скільки тіків триває один "cautious" епізод.
    private static final int CAUTIOUS_DURATION_MIN = 40;   // 2 сек
    private static final int CAUTIOUS_DURATION_MAX = 100;  // 5 сек

    private int modeTimer;
    private boolean cautious;

    // ==================== СИСТЕМА ЖИВІВ ====================

    private static final int MAX_LIVES = 5;
    private static final String NBT_LIVES = "anyaLives";
    private static final String NBT_HOME_X = "anyaHomeX";
    private static final String NBT_HOME_Y = "anyaHomeY";
    private static final String NBT_HOME_Z = "anyaHomeZ";
    private static final String NBT_HAS_HOME = "anyaHasHome";
    private static final String NBT_IS_DEAD_FOREVER = "anyaIsDeadForever";

    private int livesCount = MAX_LIVES;          // Поточне кількість живів (1-5)
    private BlockPos homePos;                    // Точка дому для респавну
    private boolean hasHome = false;             // Чи встановлено дім?
    private boolean isDeadForever = false;       // Чи вмерла назавжди?

    // ==================== КОНСТРУКТОР ====================

    public EntityAnya(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.95F);
        this.setCustomNameTag(AnyaNameTag.NAME);
        this.setAlwaysRenderNameTag(true);
    }

    // ==================== AI ІНІЦІАЛІЗАЦІЯ ====================

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAICounterAttack(this));
        this.tasks.addTask(2, new EntityAIAvoidEntity<>(this, EntityMob.class, 8.0F, 1.0D, 1.2D));
        this.tasks.addTask(3, new EntityAIEyeContact(this));
        this.tasks.addTask(4, new EntityAISeekShelterFromRain(this, 1.0D));
        this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        // targetTasks навмисно порожній - контр-атака в AI
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
     * AI режим: чередування звичайного ходу та "cautious" (оглядання)
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

    // ==================== ЗВУКИ ====================

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

    // ==================== СИСТЕМА ЖИВІВ ====================

    /**
     * Встановити дім для цієї Anya
     */
    public void setHome(BlockPos pos) {
        this.homePos = pos;
        this.hasHome = true;
    }

    /**
     * Отримати дім
     */
    public BlockPos getHome() {
        return this.homePos;
    }

    /**
     * Чи має дім?
     */
    public boolean hasHome() {
        return this.hasHome;
    }

    /**
     * Отримати поточне кількість живів
     */
    public int getLives() {
        return this.livesCount;
    }

    /**
     * Позбавити одного життя
     * Повертає true якщо ще живе
     */
    public boolean loseLife() {
        if (this.isDeadForever) {
            return false;
        }

        this.livesCount--;
        
        if (this.livesCount <= 0) {
            this.isDeadForever = true;
            this.livesCount = 0;
        }

        return this.livesCount > 0;
    }

    /**
     * Чи вона вмерла назавжди?
     */
    public boolean isDeadForever() {
        return this.isDeadForever;
    }

    /**
     * Отримати максимальне кількість живів
     */
    public int getMaxLives() {
        return MAX_LIVES;
    }

    // ==================== NBT ЗБЕРЕЖЕННЯ ====================

    /**
     * Читання даних з NBT (при завантаженні чанку)
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        
        // Житія
        this.livesCount = compound.getInteger(NBT_LIVES);
        if (this.livesCount <= 0) this.livesCount = 0;
        if (this.livesCount > MAX_LIVES) this.livesCount = MAX_LIVES;
        
        // Дім
        this.hasHome = compound.getBoolean(NBT_HAS_HOME);
        if (this.hasHome) {
            int x = compound.getInteger(NBT_HOME_X);
            int y = compound.getInteger(NBT_HOME_Y);
            int z = compound.getInteger(NBT_HOME_Z);
            this.homePos = new BlockPos(x, y, z);
        }
        
        // Мертва назавжди?
        this.isDeadForever = compound.getBoolean(NBT_IS_DEAD_FOREVER);
    }

    /**
     * Запис даних в NBT (при вивантаженні чанку)
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        
        // Житія
        compound.setInteger(NBT_LIVES, this.livesCount);
        
        // Дім
        compound.setBoolean(NBT_HAS_HOME, this.hasHome);
        if (this.hasHome) {
            compound.setInteger(NBT_HOME_X, this.homePos.getX());
            compound.setInteger(NBT_HOME_Y, this.homePos.getY());
            compound.setInteger(NBT_HOME_Z, this.homePos.getZ());
        }
        
        // Мертва назавжди?
        compound.setBoolean(NBT_IS_DEAD_FOREVER, this.isDeadForever);
    }
}
