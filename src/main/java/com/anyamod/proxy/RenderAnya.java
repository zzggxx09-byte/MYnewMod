package com.anyamod.proxy;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * Рендер Anya на основі стандартної моделі гравця (ModelPlayer).
 * Анімації (хода, розмахування руками, поворот голови) - вбудовані у ModelPlayer,
 * нічого додатково шукати/писати не треба.
 *
 * ModelPlayer(0.0F, true) - true означає slim-руки (тип Alex, 3px).
 * Якщо колись знадобиться Steve (широкі руки) - постав тут false.
 *
 * Текстура (сам скін) лежить в assets/anyamod/textures/entity/anya_skin.png -
 * щоб замінити скін, просто перезапиши цей файл своїм 64x64 PNG.
 */
public class RenderAnya extends RenderLiving<EntityAnya> {

    private static final ResourceLocation SKIN =
            new ResourceLocation(AnyaMod.MODID, "textures/entity/anya_skin.png");

    public RenderAnya(RenderManager renderManager) {
        super(renderManager, new ModelPlayer(0.0F, true), 0.5F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityAnya entity) {
        return SKIN;
    }
}
