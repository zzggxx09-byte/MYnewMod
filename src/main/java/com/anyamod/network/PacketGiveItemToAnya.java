package com.anyamod.network;

import com.anyamod.entity.EntityAnya;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Клієнт -> сервер: гравець навівся хрестиком на Аню, тримає предмет в руці і натиснув G.
 * Сервер забирає предмет (або частину стаку) з руки гравця в інвентар Ані.
 */
public class PacketGiveItemToAnya implements IMessage {

    private int entityId;

    public PacketGiveItemToAnya() {
    }

    public PacketGiveItemToAnya(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
    }

    public static class Handler implements IMessageHandler<PacketGiveItemToAnya, IMessage> {
        @Override
        public IMessage onMessage(PacketGiveItemToAnya message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                World world = player.world;
                Entity entity = world.getEntityByID(message.entityId);
                if (!(entity instanceof EntityAnya)) return;
                EntityAnya anya = (EntityAnya) entity;

                // Захист від читерства / лагу - гравець має бути поруч
                if (player.getDistanceSq(anya) > 36.0D) return;

                ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);
                if (held.isEmpty()) return;

                ItemStack toInsert = held.copy();
                ItemStack leftover = anya.insertItem(toInsert);

                int inserted = toInsert.getCount() - leftover.getCount();
                if (inserted <= 0) return;

                held.shrink(inserted);
                player.setHeldItem(EnumHand.MAIN_HAND, held);

                anya.syncInventoryToViewer();
            });
            return null;
        }
    }
}
