package com.anyamod.network;

import com.anyamod.entity.EntityAnya;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Сервер -> клієнт: актуальний вміст інвентаря Ані, для показу у вкладці "Інвентар" в GUI.
 * Клієнт просто перезаписує inventory-поле локальної (client-side) сутності EntityAnya,
 * і GUI читає дані напряму з неї - окремий кеш не потрібен.
 */
public class PacketSyncAnyaInventory implements IMessage {

    private int entityId;
    private NBTTagCompound inventoryTag;

    public PacketSyncAnyaInventory() {
    }

    public PacketSyncAnyaInventory(int entityId, ItemStackHandler inventory) {
        this.entityId = entityId;
        this.inventoryTag = inventory.serializeNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.inventoryTag = ByteBufUtils.readTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeTag(buf, this.inventoryTag);
    }

    public static class Handler implements IMessageHandler<PacketSyncAnyaInventory, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketSyncAnyaInventory message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.entityId);
                if (entity instanceof EntityAnya) {
                    ((EntityAnya) entity).getInventory().deserializeNBT(message.inventoryTag);
                }
            });
            return null;
        }
    }
}
