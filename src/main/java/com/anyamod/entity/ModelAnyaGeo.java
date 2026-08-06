package com.anyamod.entity;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ModelAnyaGeo extends AnimatedGeoModel<EntityAnya> {

    @Override
    public ResourceLocation getModelLocation(EntityAnya object) {
        return new ResourceLocation("anyamod", "geo/anya.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(EntityAnya object) {
        return new ResourceLocation("anyamod", "textures/entity/anya_skin.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityAnya animatable) {
        return new ResourceLocation("anyamod", "animations/anya.animation.json");
    }
}
