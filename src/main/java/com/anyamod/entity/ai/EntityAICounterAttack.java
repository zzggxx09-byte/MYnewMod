package com.anyamod.entity.ai;

import com.anyamod.entity.EntityAnya;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.EnumHand;

import java.util.List;

public class EntityAICounterAttack extends EntityAIBase {

    private static final double TRIGGER_RANGE = 1.7D;
    private static final double KEEP_RANGE = 2.2D;
    private static final int COOLDOWN_TICKS = 13;

    private final EntityAnya anya;
    private EntityMob target;
    private int cooldown;

    public EntityAICounterAttack(EntityAnya anya) {
        this.anya = anya;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        List<EntityMob> nearby = this.anya.world.getEntitiesWithinAABB(EntityMob.class,
                this.anya.getEntityBoundingBox().grow(TRIGGER_RANGE));
        for (EntityMob mob : nearby) {
            if (mob.isEntityAlive() && this.anya.getDistanceSq(mob) <= TRIGGER_RANGE * TRIGGER_RANGE) {
                this.target = mob;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.target != null && this.target.isEntityAlive()
                && this.anya.getDistanceSq(this.target) <= KEEP_RANGE * KEEP_RANGE;
    }

    @Override
    public void updateTask() {
        // ЗМІНЕНО: кулдаун тепер тікає щотік, поки таск виконується,
        // а не тільки між запусками shouldExecute().
        if (this.cooldown > 0) {
            this.cooldown--;
        }

        this.anya.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);

        if (this.cooldown <= 0) {
            this.anya.swingArm(EnumHand.MAIN_HAND);
            this.anya.attackEntityAsMob(this.target);

            double dx = this.anya.posX - this.target.posX;
            double dz = this.anya.posZ - this.target.posZ;
            this.target.knockBack(this.anya, 0.6F, dx, dz);

            this.cooldown = COOLDOWN_TICKS;
        }
    }

    @Override
    public void resetTask() {
        this.target = null;
    }
}
