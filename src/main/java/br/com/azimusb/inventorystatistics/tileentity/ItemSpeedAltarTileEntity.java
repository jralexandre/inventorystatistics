package br.com.azimusb.inventorystatistics.tileentity;

import br.com.azimusb.inventorystatistics.block.ItemSpeedAltarBlock;
import br.com.azimusb.inventorystatistics.helpers.SpeedHelper;
import br.com.azimusb.inventorystatistics.setup.ModTileEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemSpeedAltarTileEntity extends TileEntity implements ITickableTileEntity {
    private ItemStack displayed = ItemStack.EMPTY;
    long tickCount = 0;

    private final SpeedHelper helper = new SpeedHelper();
    private double lastSpeed = 0.0;
    private String lastHumanReadableText = "0.0/" + helper.getUnit();

    public ItemSpeedAltarTileEntity() {
        super(ModTileEntityTypes.ITEM_SPEED_ALTAR.get());
    }

    public void setDisplayed(ItemStack itemstack) {
        displayed = itemstack.copy();
        if (!getWorld().isRemote()) {
            helper.reset();
            sendUpdates();
        }
    }

    @Override
    public void tick() {
        if (displayed.isEmpty() || this.getWorld() == null || getWorld().isRemote())
            return;

        tickCount++;

        if (tickCount > 5) {
            Direction d = getBlockState().get(ItemSpeedAltarBlock.FACING).getOpposite();
            TileEntity tileEntity = this.getWorld().getTileEntity(getPos().offset(d));

            if (!(tileEntity instanceof IInventory))
                return;

            IInventory inv = (IInventory) tileEntity;
            long count = inv.count(displayed.getItem());
            long time = world.getGameTime();

            if (helper.getStartTime() == -1) {
                helper.setStart(count, time);
            } else {
                if (count != helper.getCurrentCount()) {
                    helper.add(count, world.getGameTime());
                }

                if (time - helper.getStartTime() > 400) {
                    helper.reset();
                } else {
                    helper.setCurrent(count, world.getGameTime());
                }

                lastSpeed = helper.getItemSpeed();
                lastHumanReadableText = helper.getRenderedItemSpeed(lastSpeed);
            }

            sendUpdates();

            tickCount = 0;
        }
    }


    public ItemStack getDisplayed() {
        return displayed;
    }

    public double getLastSpeed() {
        return lastSpeed;
    }

    public String getLastHumanReadableText() {
        return lastHumanReadableText;
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        super.write(compound);

        this.displayed.write(compound);
        compound.put("lastSpeed", DoubleNBT.valueOf(lastSpeed));
        compound.put("lastHumanReadableText", StringNBT.valueOf(lastHumanReadableText));
        return compound;
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        this.displayed = ItemStack.read(nbt);
        this.lastSpeed = nbt.getDouble("lastSpeed");
        this.lastHumanReadableText = nbt.getString("lastHumanReadableText");
    }

    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        int tileEntityType = 42;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new SUpdateTileEntityPacket(this.pos, tileEntityType, nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        BlockState blockState = world.getBlockState(pos);
        read(blockState, pkt.getNbtCompound());   // read from the nbt in the packet
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        return nbtTagCompound;
    }

    @Override
    public void handleUpdateTag(BlockState blockState, CompoundNBT tag)
    {
        this.read(blockState, tag);
    }

    private void sendUpdates() {
        World worldObj = getWorld();
        worldObj.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);

        markDirty();
    }

    public void setNextUnit() {
        helper.setNextUnit();
        lastHumanReadableText = helper.getRenderedItemSpeed(lastSpeed);
        sendUpdates();
    }
}
