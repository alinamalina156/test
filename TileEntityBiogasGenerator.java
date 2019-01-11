package com.alinamalina.animaniafarming.Machines.BiogasGenerator;



import javax.annotation.Nullable;

import com.alinamalina.animaniafarming.Energy.CustomEnergyStorage;
import com.alinamalina.animaniafarming.init.ModFluids;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityBiogasGenerator extends TileEntity implements ITickable

{
	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(4, ItemStack.EMPTY);
	private String customName;



	private static final int PRODUCE = 100;
	int fuelUsed = 100;
	public final CustomEnergyStorage storage = new CustomEnergyStorage(1000);
	public FluidTank tank = new FluidTank(ModFluids.BIOGAS, 0, 1000);
	public int energy = storage.getCustomEnergyStored();
	public int capacity = storage.getMaxCustomEnergyStored();
	public int fluidInTank = tank.getFluidAmount();
	public int maxBurnTime = 100;
	public int burnTime;
	public int currentBurnTime;



	@Override
	public ITextComponent getDisplayName()
	{
		return new TextComponentTranslation("Biogas generator");
	}


	public void setCustomName(String customName) 
	{
		this.customName = customName;
	}


	@SideOnly(Side.CLIENT)
	public int getEnergyScaled(int i)
	{
		return this.storage.getEnergyStored()*i/this.storage.getMaxEnergyStored();
	}


	@SideOnly(Side.CLIENT)
	public int getBurningScaled(int j){
		return this.burnTime*j/this.maxBurnTime;
	}

	public boolean isBurning() 
	{
		return this.burnTime > 0;  	
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) 
	{
		if(capability == CapabilityEnergy.ENERGY) return (T)this.storage;
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) this.tank;	
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) 
	{
		if(capability == CapabilityEnergy.ENERGY) return true;
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
		return super.hasCapability(capability, facing);
	} 

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
	{
		this.readFromNBT(pkt.getNbtCompound());

	}

	@Override
	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		NBTTagCompound tagCompound = new NBTTagCompound();
		this.writeToNBT(tagCompound);
		return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag()
	{
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) 
	{
		NBTTagCompound tag = super.writeToNBT(compound);
		tag.setInteger("burnTime", this.burnTime);
		tag.setInteger("fluidInTank", this.fluidInTank);
		tag.setInteger("energy", this.energy);
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) 
	{
		super.readFromNBT(compound);
		this.burnTime = compound.getInteger("burnTime");
		this.fluidInTank = compound.getInteger("fluidInTank");
		this.energy = compound.getInteger("energy");
	}


	@Override
	public void update() 
	{


		if(fluidInTank >= fuelUsed && energy + PRODUCE < capacity)

			burnTime++;
		if(burnTime == maxBurnTime)
		{
			fluidInTank -= fuelUsed;
			energy += PRODUCE;
			burnTime = 0;
			markDirty();
		}

	}

	public int getEnergyStored()
	{
		return this.energy;
	}

	public int getMaxEnergyStored()
	{
		return this.storage.getMaxEnergyStored();
	}

	public int getFluidAmount()
	{
		return this.fluidInTank;
	}

	@Override
	public void markDirty() 
	{
		super.markDirty();

	}

	private IBlockState getState() 
	{
		return world.getBlockState(pos);
	}



	public int getField(int id) {
		switch(id) 
		{
		case 1:
			return energy;
		case 2:
			return capacity;
		case 3:
			return fluidInTank;
		case 4:
			return this.tank.getCapacity();
		case 5:
			return this.burnTime;
		case 6:
			return this.maxBurnTime;
		default:
			return 0;
		}
	}


	public void setField(int id, int value) {
		// TODO Auto-generated method stub

	}


	public int getFieldCount() 
	{
		return 6;
	}

	public boolean isUsableByPlayer(EntityPlayer player) 
	{
		return this.world.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
	}

}
