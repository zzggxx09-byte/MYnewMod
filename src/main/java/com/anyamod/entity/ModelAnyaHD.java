package com.anyamod.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Кастомна гуманоїдна модель під текстуру 128x128 (замість 64x64 у ModelPlayer).
 * Геометрія і UV подвоєні, а фактичний розмір на екрані компенсується
 * масштабом 0.5F у RenderAnya.preRenderCallback - тому зріст персонажа
 * не змінюється, зростає лише деталізація текстури.
 */
public class ModelAnyaHD extends ModelBase {

    public final ModelRenderer bipedHead;
    public final ModelRenderer bipedBody;
    public final ModelRenderer bipedRightArm;
    public final ModelRenderer bipedLeftArm;
    public final ModelRenderer bipedRightLeg;
    public final ModelRenderer bipedLeftLeg;

    public ModelAnyaHD() {
        this.textureWidth = 128;
        this.textureHeight = 128;

        this.bipedHead = new ModelRenderer(this, 0, 0);
        this.bipedHead.addBox(-8.0F, -16.0F, -8.0F, 16, 16, 16, 0.0F);
        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedBody = new ModelRenderer(this, 32, 32);
        this.bipedBody.addBox(-8.0F, 0.0F, -4.0F, 16, 24, 8, 0.0F);
        this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedRightArm = new ModelRenderer(this, 80, 32);
        this.bipedRightArm.addBox(-6.0F, -4.0F, -4.0F, 8, 24, 8, 0.0F);
        this.bipedRightArm.setRotationPoint(-10.0F, 4.0F, 0.0F);

        this.bipedLeftArm = new ModelRenderer(this, 80, 32);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-2.0F, -4.0F, -4.0F, 8, 24, 8, 0.0F);
        this.bipedLeftArm.setRotationPoint(10.0F, 4.0F, 0.0F);

        this.bipedRightLeg = new ModelRenderer(this, 0, 32);
        this.bipedRightLeg.addBox(-4.0F, 0.0F, -4.0F, 8, 24, 8, 0.0F);
        this.bipedRightLeg.setRotationPoint(-3.8F, 24.0F, 0.0F);

        this.bipedLeftLeg = new ModelRenderer(this, 0, 32);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-4.0F, 0.0F, -4.0F, 8, 24, 8, 0.0F);
        this.bipedLeftLeg.setRotationPoint(3.8F, 24.0F, 0.0F);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.bipedHead.render(scale);
        this.bipedBody.render(scale);
        this.bipedRightArm.render(scale);
        this.bipedLeftArm.render(scale);
        this.bipedRightLeg.render(scale);
        this.bipedLeftLeg.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        this.bipedHead.rotateAngleY = netHeadYaw * 0.017453292F;
        this.bipedHead.rotateAngleX = headPitch * 0.017453292F;

        this.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
        this.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        this.bipedRightArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;

        this.bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.bipedRightLeg.rotateAngleY = 0.0F;
        this.bipedLeftLeg.rotateAngleY = 0.0F;
    }
}
