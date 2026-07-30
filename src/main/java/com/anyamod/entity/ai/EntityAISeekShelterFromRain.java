package com.anyamod.entity.ai;

import com.anyamod.entity.EntityAnya;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

/**
 * Проста втеча від дощу: якщо над мобом видно небо і йде дощ,
 * шукаємо найближчий "сухий" блок повітря поруч (немає видимості неба)
 * і йдемо туди. Перевірка не щотіку, а раз на ~20 тіків, щоб не гальмувати гру.
 */
public class EntityAISeekShelterFromRain extends EntityAIBase {

    private final EntityAnya entity;
    private final double speed;
    private double shelterX;
    private double shelterY;
    private double shelterZ;

    public EntityAISeekShelterFromRain(EntityAnya entity, double speed) {
        this.entity = entity;
        this.speed = speed;
        this.setMutexBits(1); // той самий мьютекс руху, що й у інших move-тасків
    }

    @Override
    public boolean shouldExecute() {
        if (!this.entity.world.isRaining()) {
            return false;
        }
        if (!this.entity.world.canSeeSky(this.entity.getPosition())) {
            return false; // вже під дахом/деревом
        }
        if (this.entity.getRNG().nextInt(20) != 0) {
            return false; // не перевіряємо щотіку - дорого
        }

        BlockPos shelter = this.findNearestShelter();
        if (shelter == null) {
            return false;
        }

        this.shelterX = shelter.getX() + 0.5D;
        this.shelterY = shelter.getY();
        this.shelterZ = shelter.getZ() + 0.5D;
        return true;
    }

    private BlockPos findNearestShelter() {
        BlockPos origin = this.entity.getPosition();
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;

        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos pos = origin.add(dx, dy, dz);
                    if (!this.entity.world.canSeeSky(pos)
                            && this.entity.world.isAirBlock(pos)
                            && this.entity.world.getBlockState(pos.down()).getMaterial().isSolid()) {
                        double distSq = pos.distanceSq(origin);
                        if (distSq < bestDistSq) {
                            bestDistSq = distSq;
                            best = pos;
                        }
                    }
                }
            }
        }
        return best;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.entity.world.isRaining() && !this.entity.getNavigator().noPath();
    }

    @Override
    public void startExecuting() {
        this.entity.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, this.speed);
    }
    }
