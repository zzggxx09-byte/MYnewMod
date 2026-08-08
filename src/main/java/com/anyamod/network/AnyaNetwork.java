package com.anyamod.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class AnyaNetwork {

    public static SimpleNetworkWrapper CHANNEL;

    public static void init() {
        CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("anyamod_channel");
        CHANNEL.registerMessage(PacketAnyaGuiState.Handler.class, PacketAnyaGuiState.class, 0, Side.SERVER);
        CHANNEL.registerMessage(PacketGiveItemToAnya.Handler.class, PacketGiveItemToAnya.class, 1, Side.SERVER);
        CHANNEL.registerMessage(PacketDropItemFromAnya.Handler.class, PacketDropItemFromAnya.class, 2, Side.SERVER);
        CHANNEL.registerMessage(PacketSyncAnyaInventory.Handler.class, PacketSyncAnyaInventory.class, 3, Side.CLIENT);
    }
}
