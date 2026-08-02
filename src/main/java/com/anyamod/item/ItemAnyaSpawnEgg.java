package com.anyamod.item;

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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Яйце спавну Anya
 * 
 * При використанні:
 * 1. Спавниш НОВУ Anya
 * 2. Встановлюється точка дому (для респавну)
 * 3. Anya отримує 5 живів (із NBT)
 * 
 * ВАЖЛИВО: Кожна Anya - НЕЗАЛЕЖНА! Власний livesCount, homePos!
 */
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

        // Спавниш НОВУ Anya
        EntityAnya entity = new EntityAnya(worldIn);
        entity.setLocationAndAngles(
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                MathHelper.wrapDegrees(worldIn.rand.nextFloat() * 360.0F),
                0.0F
        );
        
        // ========== ІНІЦІАЛІЗАЦІЯ ЦІЄЇ ANYA ==========
        
        // Встановлюємо дім для ЦІЄЇ Anya
        entity.setHome(spawnPos);
        
        // livesCount встановлюється в NBT при читанні (за замовчуванням 5)
        // Нічого робити не потрібно - в конструкторі EntityAnya це вже 5
        
        // Спавниш entity у світ
        worldIn.spawnEntity(entity);
        
        int startLives = entity.getMaxLives();
        
        // Broadcast в чат
        String spawnMessage = TextFormatting.GREEN 
            + "✓ Аня спавнена! ❤ " + startLives + "/" + startLives + " живій"
            + TextFormatting.RESET;
        
        worldIn.getPlayers(EntityPlayer.class, p -> true)
                .forEach(p -> p.sendMessage(new TextComponentString(spawnMessage)));
        
        System.out.println("[AnyaMod] Anya (ID: " + entity.getEntityId() + ") спавнена з домом в " + spawnPos);
        System.out.println("[AnyaMod] Anya має " + startLives + " живій");

        // Видалити яйце зі стеку
        itemstack.shrink(1);
        
        return EnumActionResult.SUCCESS;
    }
}
