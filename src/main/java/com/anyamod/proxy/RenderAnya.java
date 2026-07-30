package com.anyamod.proxy;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import com.anyamod.entity.ModelAnyaHD;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderAnya extends RenderLiving<EntityAnya> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(AnyaMod.MODID, "textures/entity/anya_skin.png");

    public RenderAnya(RenderManager renderManager) {
        super(renderManager, new ModelAnyaHD(), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityAnya entity) {
        return TEXTURE;
    }

    @Override
    protected void preRenderCallback(EntityAnya entitylivingbaseIn, float partialTickTime) {
        // Модель вдвічі більша (під текстуру 128x128) -
        // компенсуємо масштабом назад до нормального зросту.
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
    }
}
