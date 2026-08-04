package com.anyamod.entity.ai;

import com.anyamod.entity.EntityAnya;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Поки хтось дивиться в UI Ані (guiViewer != null) - вона завмирає на місці
 * і постійно дивиться на гравця. Найвищий пріоритет серед move-тасків.
 */
public class EntityAIFreezeForGui extends EntityAIBase {

    private final EntityAnya anya;

    public EntityAIFreezeForGui(EntityAnya anya) {
        this.anya = anya;
        this.setMutexBits(3); // move + look - перебиває все інше
    }

    @Override
    public boolean shouldExecute() {
        EntityPlayer viewer = this.anya.getGuiViewer();
        return viewer != null && viewer.isEntityAlive();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.shouldExecute();
    }

    @Override
    public void startExecuting() {
        this.anya.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        EntityPlayer viewer = this.anya.getGuiViewer();
        if (viewer == null) {
            return;
        }

        this.anya.getNavigator().clearPath();
        this.anya.motionX = 0.0D;
        this.anya.motionZ = 0.0D;

        this.anya.getLookHelper().setLookPositionWithEntity(viewer, 30.0F, 30.0F);
        this.anya.faceEntity(viewer, 30.0F, 30.0F);
    }

    @Override
    public void resetTask() {
    }
}
