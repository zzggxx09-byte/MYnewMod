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
 * Клієнт -> сервер: гравець клікнув по предмету у вкладці "Інвентар" в GUI Ані.
 * Аня викидає цей предмет на землю в бік гравця. Забрати напряму не можна.
 */
public class PacketDropItemFromAnya implements IMessage {

    private int entityId;
    private int slot;

    public PacketDropItemFromAnya() {
    }

    public PacketDropItemFromAnya(int entityId, int slot) {
        this.entityId = entityId;
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.slot = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeInt(this.slot);
    }

    public static class Handler implements IMessageHandler<PacketDropItemFromAnya, IMessage> {
        @Override
        public IMessage onMessage(PacketDropItemFromAnya message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                World world = player.world;
                Entity entity = world.getEntityByID(message.entityId);
                if (!(entity instanceof EntityAnya)) return;
                EntityAnya anya = (EntityAnya) entity;

                // Забирати може тільки той, хто зараз відкрив GUI цієї Ані
                if (anya.getGuiViewer() != player) return;

                anya.dropItem(message.slot, player);
            });
            return null;
        }
    }
}
