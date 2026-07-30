package com.anyamod.entity.ai;

import com.anyamod.entity.EntityAnya;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Anya дивиться точно в очі гравцю, якщо гравець дивиться саме на її голову
 * (перевірка через raytrace-перетин з хітбоксом голови, а не просто "в бік").
 *
 * "Пам'ять контакту" (MEMORY_TICKS) - якщо хрестик на мить зіслизнув з голови,
 * контакт не переривається одразу, а тримається ще якийсь час. Без цього
 * погляд Ані смикався б туди-сюди при щонайменшому тремтінні камери гравця.
 */
public class EntityAIEyeContact extends EntityAIBase {

    private static final double LOOK_RANGE = 8.0D;
    private static final double PLAYER_REACH = 8.0D;
    private static final double HEAD_HITBOX_PADDING = 0.15D;

    // Скільки тіків тримати контакт після того, як хрестик пішов з голови (10 тіків = 0.5 сек)
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
            if (isPlayerLookingAtHead(player)) {
                this.lookingPlayer = player;
                this.contactMemory = MEMORY_TICKS;
                return true;
            }
        }
        return false;
    }

    private boolean isPlayerLookingAtHead(EntityPlayer player) {
        Vec3d eyePos = player.getPositionEyes(1.0F);
        Vec3d look = player.getLook(1.0F);
        Vec3d endPos = eyePos.add(look.scale(PLAYER_REACH));

        // Приблизна позиція голови Ані - трохи нижче верху хітбокса сутності
        double headY = this.anya.posY + this.anya.height - 0.25D;
        AxisAlignedBB headBox = new AxisAlignedBB(
                this.anya.posX - HEAD_HITBOX_PADDING, headY - HEAD_HITBOX_PADDING, this.anya.posZ - HEAD_HITBOX_PADDING,
                this.anya.posX + HEAD_HITBOX_PADDING, headY + HEAD_HITBOX_PADDING, this.anya.posZ + HEAD_HITBOX_PADDING
        );

        RayTraceResult result = headBox.calculateIntercept(eyePos, endPos);
        return result != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.lookingPlayer == null || !this.lookingPlayer.isEntityAlive()) {
            return false;
        }
        // Продовжуємо, поки є пам'ять контакту, навіть якщо хрестик щойно зіслизнув
        return this.contactMemory > 0;
    }

    @Override
    public void updateTask() {
        if (isPlayerLookingAtHead(this.lookingPlayer)) {
            // Хрестик знову (чи досі) на голові - оновлюємо пам'ять до максимуму
            this.contactMemory = MEMORY_TICKS;
        } else {
            // Хрестик зіслизнув - витрачаємо пам'ять, але погляд поки тримаємо
            this.contactMemory--;
        }

        // Дивиться саме в очі, а не просто "в голову" - тому й беремо eye position
        Vec3d eyePos = this.lookingPlayer.getPositionEyes(1.0F);
        this.anya.getLookHelper().setLookPosition(eyePos.x, eyePos.y, eyePos.z, 10.0F, 10.0F);
    }

    @Override
    public void resetTask() {
        this.lookingPlayer = null;
        this.contactMemory = 0;
    }
}
