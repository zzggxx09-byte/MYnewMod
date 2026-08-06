package com.anyamod.proxy;

import com.anyamod.AnyaMod;
import com.anyamod.entity.EntityAnya;
import com.anyamod.entity.ModelAnyaGeo;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class RenderAnya extends GeoEntityRenderer<EntityAnya> {

    public RenderAnya(RenderManager renderManager) {
        super(renderManager, new ModelAnyaGeo());
        this.shadowSize = 0.5F;
    }
}
