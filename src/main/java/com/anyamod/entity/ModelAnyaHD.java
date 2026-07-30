package com.anyamod.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelAnyaHD extends ModelBase {

    // Перший (базовий) шар
    public final ModelRenderer bipedHead;
    public final ModelRenderer bipedBody;
    public final ModelRenderer bipedRightArm;
    public final ModelRenderer bipedLeftArm;
    public final ModelRenderer bipedRightLeg;
    public final ModelRenderer bipedLeftLeg;

    // Другий (overlay) шар - капелюх/куртка/рукави/штани
    public final ModelRenderer bipedHeadwear;
    public final ModelRenderer bipedBodyWear;
    public final ModelRenderer bipedRightArmwear;
    public final ModelRenderer bipedLeftArmwear;
    public final ModelRenderer bipedRightLegwear;
    public final ModelRenderer bipedLeftLegwear;

    public ModelAnyaHD() {
        this.textureWidth = 128;
        this.textureHeight = 128;

        // ===== Базовий шар =====
        this.bipedHead = new ModelRenderer(this, 0, 0);
        this.bipedHead.addBox(-8.0F, -16.0F, -8.0F, 16, 16, 16, 0.0F);
        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedBody = new ModelRenderer(this, 32, 32);
        this.bipedBody.addBox(-8.0F, 0.0F, -4.0F, 16, 24, 8, 0.0F);
        this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedRightArm = new ModelRenderer(this, 80, 32);
        this.bipedRightArm.addBox(-4.0F, -4.0F, -4.0F, 6, 24, 8, 0.0F);
        this.bipedRightArm.setRotationPoint(-10.0F, 5.0F, 0.0F);

        this.bipedLeftArm = new ModelRenderer(this, 80, 32);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-2.0F, -4.0F, -4.0F, 6, 24, 8, 0.0F);
        this.bipedLeftArm.setRotationPoint(10.0F, 5.0F, 0.0F);

        this.bipedRightLeg = new ModelRenderer(this, 0, 32);
        this.bipedRightLeg.addBox(-4.0F, 0.0F, -4.0F, 8, 24, 8, 0.0F);
        this.bipedRightLeg.setRotationPoint(-3.8F, 24.0F, 0.0F);

        this.bipedLeftLeg = new ModelRenderer(this, 0, 32);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-4.0F, 0.0F, -4.0F, 8, 24, 8, 0.0F);
        this.bipedLeftLeg.setRotationPoint(3.8F, 24.0F, 0.0F);

        // ===== Overlay шар (той самий розмір + невеликий "надув" назовні) =====
        this.bipedHeadwear = new ModelRenderer(this, 64, 0);
        this.bipedHeadwear.addBox(-8.0F, -16.0F, -8.0F, 16, 16, 16, 1.0F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedBodyWear = new ModelRenderer(this, 32, 64);
        this.bipedBodyWear.addBox(-8.0F, 0.0F, -4.0F, 16, 24, 8, 0.5F);
        this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.bipedRightArmwear = new ModelRenderer(this, 80, 64);
        this.bipedRightArmwear.addBox(-4.0F, -4.0F, -4.0F, 6, 24, 8, 0.5F);
        this.bipedRightArmwear.setRotationPoint(-10.0F, 5.0F, 0.0F);

        this.bipedLeftArmwear = new ModelRenderer(this, 80, 64);
        this.bipedLeftArmwear.mirror = true;
        this.bipedLeftArmwear.addBox(-2.0F, -4.0F, -4.0F, 6, 24, 8, 0.5F);
        this.bipedLeftArmwear.setRotationPoint(10.0F, 5.0F, 0.0F);

        this.bipedRightLegwear = new ModelRenderer(this, 0, 64);
        this.bipedRightLegwear.addBox(-4.0F, 0.0F, -4.0F, 8, 24, 8, 0.5F);
        this.bipedRightLegwear.setRotationPoint(-3.8F, 24.0F, 0.0F);

        this.bipedLeftLegwear = new ModelRenderer(this, 0, 64);
        this.bipedLeftLegwear.mirror = true;
        this.bipedLeftLegwear.addBox(-4.0F, 0.0F, -4.0F, 8, 24, 8, 0.5F);
        this.bipedLeftLegwear.setRotationPoint(3.8F, 24.0F, 0.0F);
    }

    @Override
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);

        // Overlay-частини мають рухатись синхронно з базовими (та сама рука/нога/голова)
        this.bipedHeadwear.rotateAngleX = this.bipedHead.rotateAngleX;
        this.bipedHeadwear.rotateAngleY = this.bipedHead.rotateAngleY;
        this.bipedHeadwear.rotateAngleZ = this.bipedHead.rotateAngleZ;

        this.bipedRightArmwear.rotateAngleX = this.bipedRightArm.rotateAngleX;
        this.bipedRightArmwear.rotateAngleZ = this.bipedRightArm.rotateAngleZ;
        this.bipedLeftArmwear.rotateAngleX = this.bipedLeftArm.rotateAngleX;
        this.bipedLeftArmwear.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;

        this.bipedRightLegwear.rotateAngleX = this.bipedRightLeg.rotateAngleX;
        this.bipedLeftLegwear.rotateAngleX = this.bipedLeftLeg.rotateAngleX;

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        this.bipedHead.render(scale);
        this.bipedBody.render(scale);
        this.bipedRightArm.render(scale);
        this.bipedLeftArm.render(scale);
        this.bipedRightLeg.render(scale);
        this.bipedLeftLeg.render(scale);

        this.bipedHeadwear.render(scale);
        this.bipedBodyWear.render(scale);
        this.bipedRightArmwear.render(scale);
        this.bipedLeftArmwear.render(scale);
        this.bipedRightLegwear.render(scale);
        this.bipedLeftLegwear.render(scale);

        GlStateManager.popMatrix();
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
