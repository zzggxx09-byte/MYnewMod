package com.anyamod.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class AnyaRespawnData extends WorldSavedData {

    private static final String DATA_NAME = "anyamod_anya_respawn";

    private BlockPos homePos;
    private boolean hasHome;
    private boolean pendingRespawn;
    private int respawnTicksLeft;

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

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.hasHome = nbt.getBoolean("hasHome");
        if (this.hasHome) {
            this.homePos = new BlockPos(nbt.getInteger("homeX"), nbt.getInteger("homeY"), nbt.getInteger("homeZ"));
        }
        this.pendingRespawn = nbt.getBoolean("pendingRespawn");
        this.respawnTicksLeft = nbt.getInteger("respawnTicksLeft");
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
        return compound;
    }
}
