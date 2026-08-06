package com.anyamod;

import com.anyamod.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = AnyaMod.MODID, name = AnyaMod.NAME, version = AnyaMod.VERSION)
public class AnyaMod {

    public static final String MODID = "anyamod";
    public static final String NAME = "Anya Mod";
    public static final String VERSION = "1.0.0";

    public static final CreativeTabs TAB_ANYA = new CreativeTabs("anyamodTab") {
        @Override
        public net.minecraft.item.ItemStack getTabIconItem() {
            return new net.minecraft.item.ItemStack(net.minecraft.init.Items.SPAWN_EGG);
        }
    };

    @SidedProxy(clientSide = "com.anyamod.proxy.ClientProxy", serverSide = "com.anyamod.proxy.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        software.bernie.geckolib3.GeckoLib.initialize(); // ДОДАНО - обов'язкова ініціалізація GeckoLib
        com.anyamod.network.AnyaNetwork.init();
        proxy.registerRenderers();
    }
}
