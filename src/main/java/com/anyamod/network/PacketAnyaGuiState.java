package com.anyamod.network;

import com.anyamod.entity.EntityAnya;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Клієнт -> сервер: гравець відкрив/закрив UI конкретної Ані.
 * Сервер виставляє або скидає "глядача" на сутності.
 */
public class PacketAnyaGuiState implements IMessage {

    private int entityId;
    private boolean opening;

    public PacketAnyaGuiState() {
    }

    public PacketAnyaGuiState(int entityId, boolean opening) {
        this.entityId = entityId;
        this.opening = opening;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.opening = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeBoolean(this.opening);
    }

    public static class Handler implements IMessageHandler<PacketAnyaGuiState, IMessage> {
        @Override
        public IMessage onMessage(PacketAnyaGuiState message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                World world = player.world;
                Entity entity = world.getEntityByID(message.entityId);
                if (entity instanceof EntityAnya) {
                    EntityAnya anya = (EntityAnya) entity;
                    if (message.opening) {
                        anya.setGuiViewer(player);
                    } else if (anya.getGuiViewer() == player) {
                        anya.setGuiViewer(null);
                    }
                }
            });
            return null;
        }
    }
}
