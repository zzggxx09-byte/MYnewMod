package com.anyamod.entity;

import com.anyamod.entity.ai.EntityAICounterAttack;
import com.anyamod.entity.ai.EntityAIEyeContact;
import com.anyamod.entity.ai.EntityAIFreezeForGui;
import com.anyamod.entity.ai.EntityAISeekShelterFromRain;
import com.anyamod.init.ModSounds;
import com.anyamod.network.AnyaNetwork;
import com.anyamod.network.PacketSyncAnyaInventory;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/**
 * Anya - кастомний моб з AI та системою 5 живів
 */
public class EntityAnya extends EntityCreature implements IAnimatable {

    // ==================== GECKOLIB АНІМАЦІЯ ====================

    private final AnimationFactory factory = new AnimationFactory(this);

    // ==================== AI ПАРАМЕТРИ ====================

    private static final double NORMAL_SPEED = 0.25D;
    private static final double CAUTIOUS_SPEED = 0.09D;
    private static final int MODE_CHECK_INTERVAL = 100;
    private static final int CAUTIOUS_CHANCE_PERCENT = 15;
    private static final int CAUTIOUS_DURATION_MIN = 40;
    private static final int CAUTIOUS_DURATION_MAX = 100;

    private int modeTimer;
    private boolean cautious;

    // ==================== СИСТЕМА ЖИВІВ ====================

    private static final int MAX_LIVES = 5;

    private static final DataParameter<Integer> LIVES =
            EntityDataManager.createKey(EntityAnya.class, DataSerializers.VARINT);

    private static final String NBT_LIVES = "anyaLives";
    private static final String NBT_HOME_X = "anyaHomeX";
    private static final String NBT_HOME_Y = "anyaHomeY";
    private static final String NBT_HOME_Z = "anyaHomeZ";
    private static final String NBT_HAS_HOME = "anyaHasHome";
    private static final String NBT_IS_DEAD_FOREVER = "anyaIsDeadForever";
    private static final String NBT_INVENTORY = "anyaInventory";

    private BlockPos homePos;
    private boolean hasHome = false;
    private boolean isDeadForever = false;

    private EntityPlayer guiViewer;

    // ==================== ІНВЕНТАР ====================

    private static final int INVENTORY_SIZE = 27;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE);

    // ==================== КОНСТРУКТОР ====================

    public EntityAnya(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.95F);
        this.setCustomNameTag(AnyaNameTag.NAME);
        this.setAlwaysRenderNameTag(true);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LIVES, MAX_LIVES);
    }

    // ==================== AI ІНІЦІАЛІЗАЦІЯ ====================

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIFreezeForGui(this));
        this.tasks.addTask(2, new EntityAICounterAttack(this));
        this.tasks.addTask(3, new EntityAIAvoidEntity<>(this, EntityMob.class, 8.0F, 1.0D, 1.2D));
        this.tasks.addTask(4, new EntityAIEyeContact(this));
        this.tasks.addTask(5, new EntityAISeekShelterFromRain(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(8, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(NORMAL_SPEED);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!this.world.isRemote) {
            updateMovementMode();
        }
    }

    private void updateMovementMode() {
        if (this.modeTimer > 0) {
            this.modeTimer--;
            return;
        }

        if (this.cautious) {
            this.cautious = false;
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(NORMAL_SPEED);
            this.modeTimer = MODE_CHECK_INTERVAL;
        } else if (this.rand.nextInt(100) < CAUTIOUS_CHANCE_PERCENT) {
            this.cautious = true;
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(CAUTIOUS_SPEED);
            this.modeTimer = CAUTIOUS_DURATION_MIN
                    + this.rand.nextInt(CAUTIOUS_DURATION_MAX - CAUTIOUS_DURATION_MIN + 1);
        } else {
            this.modeTimer = MODE_CHECK_INTERVAL;
        }
    }

    // ==================== ЗВУКИ ====================

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ANYA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return ModSounds.ANYA_HURT;
    }

    @Override
    public int getTalkInterval() {
        return 160;
    }

    @Override
    public boolean getAlwaysRenderNameTagForRender() {
        return true;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (this.world.isRemote) {
            com.anyamod.AnyaMod.proxy.openAnyaGui(this, player);
        }
        return true;
    }

    // ==================== СИСТЕМА ЖИВІВ ====================

    public void setHome(BlockPos pos) {
        this.homePos = pos;
        this.hasHome = true;
    }

    public BlockPos getHome() {
        return this.homePos;
    }

    public boolean hasHome() {
        return this.hasHome;
    }

    public int getLives() {
        return this.dataManager.get(LIVES);
    }

    public void setLives(int lives) {
        if (lives < 0) lives = 0;
        if (lives > MAX_LIVES) lives = MAX_LIVES;
        this.dataManager.set(LIVES, lives);
        this.isDeadForever = (lives <= 0);
    }

    public boolean loseLife() {
        if (this.isDeadForever) {
            return false;
        }

        int newLives = this.getLives() - 1;
        if (newLives <= 0) {
            newLives = 0;
            this.isDeadForever = true;
        }
        this.dataManager.set(LIVES, newLives);

        return newLives > 0;
    }

    public void addLife() {
        if (this.isDeadForever) {
            return;
        }
        int current = this.getLives();
        if (current < MAX_LIVES) {
            this.dataManager.set(LIVES, current + 1);
        }
    }

    public boolean isDeadForever() {
        return this.isDeadForever;
    }

    public int getMaxLives() {
        return MAX_LIVES;
    }

    public void setGuiViewer(EntityPlayer viewer) {
        this.guiViewer = viewer;
        if (viewer != null) {
            // Одразу відправляємо актуальний вміст інвентаря тому, хто щойно відкрив GUI
            this.syncInventoryToViewer();
        }
    }

    public EntityPlayer getGuiViewer() {
        return this.guiViewer;
    }

    // ==================== ІНВЕНТАР ====================

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    /**
     * Намагається покласти предмет у перший вільний/сумісний слот.
     * Повертає залишок, який не вмістився (ItemStack.EMPTY, якщо влізло все).
     */
    public ItemStack insertItem(ItemStack stack) {
        ItemStack remaining = stack;
        for (int i = 0; i < this.inventory.getSlots() && !remaining.isEmpty(); i++) {
            remaining = this.inventory.insertItem(i, remaining, false);
        }
        return remaining;
    }

    /**
     * Викидає предмет з вказаного слота на землю в бік гравця (towardsPlayer може бути null).
     */
    public void dropItem(int slot, EntityPlayer towardsPlayer) {
        if (this.world.isRemote) return;
        if (slot < 0 || slot >= this.inventory.getSlots()) return;

        ItemStack stack = this.inventory.extractItem(slot, this.inventory.getSlotLimit(slot), false);
        if (stack.isEmpty()) return;

        EntityItem entityItem = new EntityItem(this.world, this.posX, this.posY + 0.5D, this.posZ, stack);

        if (towardsPlayer != null) {
            double dx = towardsPlayer.posX - this.posX;
            double dz = towardsPlayer.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.001D) {
                entityItem.motionX = (dx / dist) * 0.3D;
                entityItem.motionZ = (dz / dist) * 0.3D;
            }
        }
        entityItem.motionY = 0.25D;
        entityItem.setPickupDelay(10);

        this.world.spawnEntity(entityItem);

        this.syncInventoryToViewer();
    }

    /**
     * Шле поточний вміст інвентаря гравцю, який зараз тримає GUI цієї Ані відкритим.
     */
    public void syncInventoryToViewer() {
        if (this.world.isRemote) return;
        if (this.guiViewer instanceof EntityPlayerMP) {
            AnyaNetwork.CHANNEL.sendTo(
                    new PacketSyncAnyaInventory(this.getEntityId(), this.inventory),
                    (EntityPlayerMP) this.guiViewer
            );
        }
    }

    // ==================== NBT ЗБЕРЕЖЕННЯ ====================

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        int lives = compound.getInteger(NBT_LIVES);
        this.setLives(lives);

        this.hasHome = compound.getBoolean(NBT_HAS_HOME);
        if (this.hasHome) {
            int x = compound.getInteger(NBT_HOME_X);
            int y = compound.getInteger(NBT_HOME_Y);
            int z = compound.getInteger(NBT_HOME_Z);
            this.homePos = new BlockPos(x, y, z);
        }

        this.isDeadForever = compound.getBoolean(NBT_IS_DEAD_FOREVER);

        if (compound.hasKey(NBT_INVENTORY)) {
            this.inventory.deserializeNBT(compound.getCompoundTag(NBT_INVENTORY));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        compound.setInteger(NBT_LIVES, this.getLives());

        compound.setBoolean(NBT_HAS_HOME, this.hasHome);
        if (this.hasHome) {
            compound.setInteger(NBT_HOME_X, this.homePos.getX());
            compound.setInteger(NBT_HOME_Y, this.homePos.getY());
            compound.setInteger(NBT_HOME_Z, this.homePos.getZ());
        }

        compound.setBoolean(NBT_IS_DEAD_FOREVER, this.isDeadForever);

        compound.setTag(NBT_INVENTORY, this.inventory.serializeNBT());
    }

    // ==================== АНІМАЦІЯ (GeckoLib) ====================

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.anya.walk", true));
        } else {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.anya.idle", true));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
    }
                           
