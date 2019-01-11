package com.alinamalina.animaniafarming.Machines.BiogasGenerator;

import com.alinamalina.animaniafarming.Machines.GuiIDs;
import com.alinamalina.animaniafarming.init.ModBlocks;
import com.alinamalina.animaniafarming.init.ModFluids;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

import com.alinamalina.animaniafarming.Main;
import com.alinamalina.animaniafarming.Blocks.BlockBase;

public class BlockBiogasGenerator extends BlockBase implements ITileEntityProvider
{
	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	public BlockBiogasGenerator(String name)
	{
		super(name, Material.IRON);
		setSoundType(SoundType.METAL);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitY, float hitX, float hitZ)
	{
		ItemStack heldItem = playerIn.getHeldItem(hand);
		TileEntityBiogasGenerator te = (TileEntityBiogasGenerator) worldIn.getTileEntity(pos);

		if(!worldIn.isRemote)
			if(!FluidUtil.interactWithFluidHandler(playerIn, hand, te.tank))
			{
				playerIn.openGui(Main.instance, GuiIDs.GUI_BIOGAS_GENERATOR, worldIn, pos.getX(), pos.getY(), pos.getZ());//Opens GUI of generator
			}
		return true;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return Item.getItemFromBlock(ModBlocks.BIOGAS_GENERATOR);
	}

	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
	{
		return new ItemStack(ModBlocks.BIOGAS_GENERATOR);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityBiogasGenerator();
	}

	public static boolean hasFluid(ItemStack stack, Fluid fluid)
	{
		return FluidUtil.getFluidContained(stack) != null && FluidUtil.getFluidContained(stack).amount >= 1000 && FluidUtil.getFluidContained(stack).getFluid() == fluid;
	}


	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
	{
		if (!worldIn.isRemote)
		{
			IBlockState north = worldIn.getBlockState(pos.north());
			IBlockState south = worldIn.getBlockState(pos.south());
			IBlockState west = worldIn.getBlockState(pos.west());
			IBlockState east = worldIn.getBlockState(pos.east());
			EnumFacing face = (EnumFacing)state.getValue(FACING);

			if(face == EnumFacing.NORTH && north.isFullBlock() && !south.isFullBlock()) face = EnumFacing.SOUTH;//Calculating how to face the block so when the player faces some direction the block will be placed facing the player
			else if(face == EnumFacing.SOUTH && south.isFullBlock() && !north.isFullBlock()) face = EnumFacing.NORTH;
			else if(face == EnumFacing.WEST && west.isFullBlock() && !east.isFullBlock()) face = EnumFacing.EAST;
			else if(face == EnumFacing.EAST && east.isFullBlock() && !west.isFullBlock()) face = EnumFacing.WEST;
			worldIn.setBlockState(pos, state.withProperty(FACING, face), 2);
		}	
	}


	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack){
		world.setBlockState(pos, state.withProperty(BlockHorizontal.FACING, player.getHorizontalFacing().getOpposite()), 2);

		super.onBlockPlacedBy(world, pos, state, player, stack);
	}


	@Override
	public IBlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.getHorizontal(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, BlockHorizontal.FACING);
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot){
		return state.withProperty(BlockHorizontal.FACING, rot.rotate(state.getValue(BlockHorizontal.FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror){
		return this.withRotation(state, mirror.toRotation(state.getValue(BlockHorizontal.FACING)));
	}

}
