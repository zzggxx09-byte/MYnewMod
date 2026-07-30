package com.anyamod.entity.ai;

import com.anyamod.entity.EntityAnya;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Anya дивиться точно в очі гравцю, якщо гравець дивиться на будь-яку
 * частину її тіла (перевірка через raytrace-перетин з усім хітбоксом,
 * а не лише головою - хрестик рідко потрапляє саме на голову).
 *
 * "Пам'ять контакту" (MEMORY_TICKS) - якщо хрестик на мить зіслизнув,
 * контакт не переривається одразу, а тримається ще якийсь час. Без цього
 * погляд Ані смикався б туди-сюди при щонайменшому тремтінні камери гравця.
 */
public class EntityAIEyeContact extends EntityAIBase {

    private static final double LOOK_RANGE = 8.0D;
    private static final double PLAYER_REACH = 8.0D;
    private static final double BODY_HITBOX_PADDING = 0.1D;

    // Скільки тіків тримати контакт після того, як хрестик пішов з Ані (10 тіків = 0.5 сек)
    private static final int MEMORY_TICKS = 10;

    private final EntityAnya anya;
    private EntityPlayer lookingPlayer;
    private int contactMemory;

    public EntityAIEyeContact(EntityAnya anya) {
        this.anya = anya;
        this.setMutexBits(2); // окремий від руху mutex "погляд"
    }

    @Override
    public boolean shouldExecute() {
        List<EntityPlayer> nearby = this.anya.world.getEntitiesWithinAABB(
                EntityPlayer.class, this.anya.getEntityBoundingBox().grow(LOOK_RANGE));

        for (EntityPlayer player : nearby) {
            if (isPlayerLookingAtAnya(player)) {
                this.lookingPlayer = player;
                this.contactMemory = MEMORY_TICKS;
                return true;
            }
        }
        return false;
    }

    private boolean isPlayerLookingAtAnya(EntityPlayer player) {
        Vec3d eyePos = player.getPositionEyes(1.0F);
        Vec3d look = player.getLook(1.0F);
        Vec3d endPos = eyePos.add(look.scale(PLAYER_REACH));

        // Тепер перевіряємо ВЕСЬ хітбокс сутності (не тільки голову) -
        // хрестиком у тулуб/руку/ногу теж рахується як "дивиться на Аню".
        AxisAlignedBB bodyBox = this.anya.getEntityBoundingBox().grow(BODY_HITBOX_PADDING);

        RayTraceResult result = bodyBox.calculateIntercept(eyePos, endPos);
        return result != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.lookingPlayer == null || !this.lookingPlayer.isEntityAlive()) {
            return false;
        }
        return this.contactMemory > 0;
    }

    @Override
    public void updateTask() {
        if (isPlayerLookingAtAnya(this.lookingPlayer)) {
            this.contactMemory = MEMORY_TICKS;
        } else {
            this.contactMemory--;
        }

        Vec3d eyePos = this.lookingPlayer.getPositionEyes(1.0F);
        this.anya.getLookHelper().setLookPosition(eyePos.x, eyePos.y, eyePos.z, 10.0F, 10.0F);
    }

    @Override
    public void resetTask() {
        this.lookingPlayer = null;
        this.contactMemory = 0;
    }
        }
