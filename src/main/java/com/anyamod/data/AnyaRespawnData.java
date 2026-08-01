package com.anyamod.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

/**
 * Система респавну Anya з життами
 * 
 * СИСТЕМА ЖИТТІВ:
 * - Anya стартує з 5 життів
 * - При смерті: livesCount -= 1
 * - Якщо livesCount = 0: смерть назавжди
 * - Кожен новий день: +1 life (максимум 5)
 */
public class AnyaRespawnData extends WorldSavedData {

    private static final String DATA_NAME = "anyamod_anya_respawn";
    private static final int MAX_LIVES = 5;

    private BlockPos homePos;
    private boolean hasHome;
    private boolean pendingRespawn;
    private int respawnTicksLeft;
    
    // НОВЕ: Система життів
    private int livesCount = MAX_LIVES;           // Поточне кількість життів (1-5)
    private boolean isDeadForever = false;         // Чи вона вмерла назавжди?
    private long lastDayLiveWasGiven = -1;         // День коли останній раз дали life

    public AnyaRespawnData(String name) {
        super(name);
    }

    public static AnyaRespawnData get(World world) {
        MapStorage storage = world.getMapStorage();
        AnyaRespawnData data = (AnyaRespawnData) storage.getOrLoadData(AnyaRespawnData.class, DATA_NAME);
        if (data == null) {
            data = new AnyaRespawnData(DATA_NAME);
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    // ==================== ДОМУ ====================
    
    public void setHome(BlockPos pos) {
        this.homePos = pos;
        this.hasHome = true;
        this.markDirty();
    }

    public boolean hasHome() {
        return this.hasHome;
    }

    public BlockPos getHome() {
        return this.homePos;
    }

    // ==================== РЕСПАВН ====================
    
    public void startRespawnCountdown(int ticks) {
        this.pendingRespawn = true;
        this.respawnTicksLeft = ticks;
        this.markDirty();
    }

    public boolean isPendingRespawn() {
        return this.pendingRespawn;
    }

    public boolean tickRespawnCountdown() {
        if (!this.pendingRespawn) {
            return false;
        }
        this.respawnTicksLeft--;
        this.markDirty();
        if (this.respawnTicksLeft <= 0) {
            this.pendingRespawn = false;
            return true;
        }
        return false;
    }

    // ==================== СИСТЕМА ЖИТТІВ ====================
    
    /**
     * Отримати поточне кількість життів (1-5)
     */
    public int getLives() {
        return this.livesCount;
    }

    /**
     * Позбавити одного життя при смерті
     * Повертає true якщо вона ще живе
     */
    public boolean loseLife() {
        if (this.isDeadForever) {
            return false; // Вже мертва
        }

        this.livesCount--;
        
        if (this.livesCount <= 0) {
            // СМЕРТЬ НАЗАВЖДИ!
            this.isDeadForever = true;
            this.livesCount = 0;
        }
        
        this.markDirty();
        return this.livesCount > 0;
    }

    /**
     * Чи вона вмерла назавжди?
     */
    public boolean isDeadForever() {
        return this.isDeadForever;
    }

    /**
     * Дати +1 life при новому дні
     * Повертає true якщо дали life
     */
    public boolean tryGiveLifeForNewDay(long worldTime) {
        // Визначаємо поточний день (1 день = 24000 тиків)
        long currentDay = worldTime / 24000L;
        
        // Перевіряємо чи це новий день і чи можна дати life
        if (currentDay > this.lastDayLiveWasGiven && this.livesCount < MAX_LIVES) {
            this.livesCount++;
            this.lastDayLiveWasGiven = currentDay;
            this.markDirty();
            return true;
        }
        
        return false;
    }

    /**
     * Отримати максимальне кількість життів
     */
    public int getMaxLives() {
        return MAX_LIVES;
    }

    // ==================== NBT ЗБЕРЕЖЕННЯ ====================
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.hasHome = nbt.getBoolean("hasHome");
        if (this.hasHome) {
            this.homePos = new BlockPos(
                nbt.getInteger("homeX"),
                nbt.getInteger("homeY"),
                nbt.getInteger("homeZ")
            );
        }
        this.pendingRespawn = nbt.getBoolean("pendingRespawn");
        this.respawnTicksLeft = nbt.getInteger("respawnTicksLeft");
        
        // НОВЕ: Життя
        this.livesCount = nbt.getInteger("livesCount");
        if (this.livesCount <= 0) this.livesCount = 0;
        if (this.livesCount > MAX_LIVES) this.livesCount = MAX_LIVES;
        
        this.isDeadForever = nbt.getBoolean("isDeadForever");
        this.lastDayLiveWasGiven = nbt.getLong("lastDayLiveWasGiven");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean("hasHome", this.hasHome);
        if (this.hasHome) {
            compound.setInteger("homeX", this.homePos.getX());
            compound.setInteger("homeY", this.homePos.getY());
            compound.setInteger("homeZ", this.homePos.getZ());
        }
        compound.setBoolean("pendingRespawn", this.pendingRespawn);
        compound.setInteger("respawnTicksLeft", this.respawnTicksLeft);
        
        // НОВЕ: Життя
        compound.setInteger("livesCount", this.livesCount);
        compound.setBoolean("isDeadForever", this.isDeadForever);
        compound.setLong("lastDayLiveWasGiven", this.lastDayLiveWasGiven);
        
        return compound;
    }
}
