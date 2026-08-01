package com.anyamod.item;

import com.anyamod.data.AnyaRespawnData;
import com.anyamod.entity.EntityAnya;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemAnyaSpawnEgg extends Item {

    private final int primaryColor;
    private final int secondaryColor;

    public ItemAnyaSpawnEgg(int primaryColor, int secondaryColor) {
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.setMaxStackSize(64);
    }

    public int getColor(int tintIndex) {
        return tintIndex == 0 ? this.primaryColor : this.secondaryColor;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                       EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        ItemStack itemstack = player.getHeldItem(hand);
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        BlockPos spawnPos = pos.offset(facing);
        double extraY = 0.0D;

        EntityAnya entity = new EntityAnya(worldIn);
        entity.setLocationAndAngles(
                spawnPos.getX() + 0.5D,
                spawnPos.getY() + extraY,
                spawnPos.getZ() + 0.5D,
                MathHelper.wrapDegrees(worldIn.rand.nextFloat() * 360.0F),
                0.0F
        );
        worldIn.spawnEntity(entity);

        // Ця точка стає домашньою - сюди Аня повернеться після смерті
        AnyaRespawnData.get(worldIn).setHome(spawnPos);

        itemstack.shrink(1);
        return EnumActionResult.SUCCESS;
    }
}
