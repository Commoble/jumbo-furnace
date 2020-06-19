package com.github.commoble.jumbofurnace;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemStackHandler;

public class JumboFurnaceCoreTileEntity extends TileEntity
{
	public static final String INPUT = "input";
	public static final String FUEL = "fuel";
	public static final String OUTPUT = "output";
	
	public final ItemStackHandler input = new InputItemHandler(this);
	public final ItemStackHandler fuel = new FuelItemHandler(this);
	public final ItemStackHandler output = new OutputItemHandler(this);
	
	public int burnTimeRemaining = 0;
	public int cookProgress = 0;
	public int cookProgressRequired = 0;
	public List<CachedRecipe> cachedRecipes = new ArrayList<>();
	
	public JumboFurnaceCoreTileEntity()
	{
		super(JumboFurnaceObjects.CORE_TE_TYPE);
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.input.deserializeNBT(compound.getCompound(INPUT));
		this.fuel.deserializeNBT(compound.getCompound(FUEL));
		this.output.deserializeNBT(compound.getCompound(OUTPUT));
		this.updateRecipes();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.put(INPUT, this.input.serializeNBT());
		compound.put(FUEL, this.fuel.serializeNBT());
		compound.put(OUTPUT, this.output.serializeNBT());
		return compound;
	}
	
	public boolean isBurning()
	{
		BlockState sideState = this.world.getBlockState(this.pos.offset(Direction.NORTH));
		return (sideState.getBlock() instanceof JumboFurnaceBlock && sideState.get(JumboFurnaceBlock.LIT));
	}
	
	public void setBurning(boolean burning)
	{
		for (Direction direction : Direction.Plane.HORIZONTAL)
		{
			BlockPos adjacentPos = this.pos.offset(direction);
			BlockState state = this.world.getBlockState(adjacentPos);
			if (state.getBlock() instanceof JumboFurnaceBlock)
			{
				this.world.setBlockState(adjacentPos, state.with(JumboFurnaceBlock.LIT, burning));
			}
		}
	}
	
	public void onFuelUpdated()
	{
		
	}
	
	public void onInputUpdated()
	{
		
	}
	
	public void updateRecipes()
	{
		// get all recipes allowed by furnace or jumbo furnace
		// sort them by specificity (can we do this on recipe reload?)
		// recipes requiring multiple ingredients = most important, ingredients with more matching items (tags) = less important
		// start assigning input slots to usable recipes as they are found
		// when all input slots are claimed or the recipe list is exhausted, set the new recipe cache
	}

}
