package com.anyamod.entity;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.geo.render.built.GeoBone;
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

    @Override
    public void setLivingAnimations(EntityAnya entity, Integer uniqueID, software.bernie.geckolib3.core.event.predicate.AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);

        // ДОДАНО: окремий поворот кістки "Head" - голова дивиться на гравця,
        // тіло лишається повернутим за напрямком руху/AI.
        GeoBone head = this.getAnimationProcessor().getBone("Head");
        if (head != null) {
            head.setRotationZ(0);
            head.setRotationY((float) Math.toRadians(entity.rotationYawHead - entity.renderYawOffset));
            head.setRotationX((float) Math.toRadians(entity.rotationPitch));
        }
    }
}
