package com.anyamod.proxy;

public class CommonProxy {
    public void registerRenderers() {
        // На сервері нічого не рендеримо
    }
    public void openAnyaGui(com.anyamod.entity.EntityAnya anya, net.minecraft.entity.player.EntityPlayer player) {
    // no-op на сервері
    }
}
