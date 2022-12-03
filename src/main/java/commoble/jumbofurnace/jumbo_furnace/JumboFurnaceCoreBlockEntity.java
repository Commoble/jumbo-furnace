package commoble.jumbofurnace.jumbo_furnace;

import java.util.ArrayList;
import java.util.List;

import commoble.jumbofurnace.JumboFurnace;
import commoble.jumbofurnace.JumboFurnaceUtils;
import commoble.jumbofurnace.recipes.ClaimableRecipeWrapper;
import commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import commoble.jumbofurnace.recipes.RecipeSorter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class JumboFurnaceCoreBlockEntity extends BlockEntity
{
	public static final String INPUT = "input";
	public static final String FUEL = "fuel";
	public static final String OUTPUT = "output";
	public static final String MULTIPROCESS_UPGRADES = "multiprocess_upgrades";
	public static final String COOK_PROGRESS = "cook_progress";
	public static final String BURN_TIME = "burn_time";
	public static final String BURN_VALUE = "burn_value";
	public static final BlockEntityTicker<JumboFurnaceCoreBlockEntity> SERVER_TICKER = (level,pos,state,core)->core.serverTick();
	
	public final InputItemHandler input = new InputItemHandler(this);
	public final ItemStackHandler fuel = new FuelItemHandler(this);
	public final OutputItemHandler output = new OutputItemHandler(this);
	public final MultiprocessUpgradeHandler multiprocessUpgradeHandler = new MultiprocessUpgradeHandler(this);
	
	public final LazyOptional<IItemHandler> inputOptional = LazyOptional.of(() -> this.input);
	public final LazyOptional<IItemHandler> fuelOptional = LazyOptional.of(() -> this.fuel);
	public final LazyOptional<IItemHandler> outputOptional = LazyOptional.of(() -> this.output);
	// multiprocess upgrade slot isn't exposed, no need to cache a lazy wrapper
	
	public int burnTimeRemaining = 0;
	public int lastItemBurnedValue = 200;
	public int cookProgress = 0;
	public boolean isRoomToCook = true;
	public boolean canConsumeFuel = false;
	public ClaimableRecipeWrapper cachedRecipes = this.input.getFreshRecipeInput();
	public boolean needsRecipeUpdate = false;
	public boolean needsOutputUpdate = false;
	public boolean needsFuelUpdate = false;
	
	public static JumboFurnaceCoreBlockEntity create(BlockPos pos, BlockState state)
	{
		return new JumboFurnaceCoreBlockEntity(JumboFurnace.get().jumboFurnaceCoreBlockEntityType.get(), pos, state);
	}
	
	protected JumboFurnaceCoreBlockEntity(BlockEntityType<? extends JumboFurnaceCoreBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public void invalidateCaps()
	{
		this.inputOptional.invalidate();
		this.fuelOptional.invalidate();
		this.outputOptional.invalidate();
	}

	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);
		this.input.deserializeNBT(compound.getCompound(INPUT));
		this.fuel.deserializeNBT(compound.getCompound(FUEL));
		this.output.deserializeNBT(compound.getCompound(OUTPUT));
		this.multiprocessUpgradeHandler.deserializeNBT(compound.getCompound(MULTIPROCESS_UPGRADES));
		this.cookProgress = compound.getInt(COOK_PROGRESS);
		this.burnTimeRemaining = compound.getInt(BURN_TIME);
		this.lastItemBurnedValue = compound.getInt(BURN_VALUE);
		this.onInputInventoryChanged();
		this.onOutputInventoryChanged();
		this.onFuelInventoryChanged();
	}

	@Override
	public void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.put(INPUT, this.input.serializeNBT());
		compound.put(FUEL, this.fuel.serializeNBT());
		compound.put(OUTPUT, this.output.serializeNBT());
		compound.put(MULTIPROCESS_UPGRADES, this.multiprocessUpgradeHandler.serializeNBT());
		compound.putInt(COOK_PROGRESS, this.cookProgress);
		compound.putInt(BURN_TIME, this.burnTimeRemaining);
		compound.putInt(BURN_VALUE, this.lastItemBurnedValue);
	}
	
	public int getBurnConsumption()
	{
		return Math.max(1, this.cachedRecipes.getRecipeCount());
	}
	
	public boolean isBurning()
	{
		return this.burnTimeRemaining > 0;
	}
	
	public void updateBurningBlockstates(boolean burning)
	{
		for (Direction direction : Direction.Plane.HORIZONTAL)
		{
			BlockPos adjacentPos = this.worldPosition.relative(direction);
			BlockState state = this.level.getBlockState(adjacentPos);
			if (state.getBlock() instanceof JumboFurnaceBlock)
			{
				this.level.setBlockAndUpdate(adjacentPos, state.setValue(JumboFurnaceBlock.LIT, burning));
			}
		}
	}
	
	public void markFuelInventoryChanged()
	{
		this.setChanged();
		this.onFuelInventoryChanged();
	}
	
	public void onFuelInventoryChanged()
	{
		this.needsFuelUpdate = true;
	}
	
	public void markInputInventoryChanged()
	{
		this.setChanged();
		this.onInputInventoryChanged();
	}
	
	public void onInputInventoryChanged()
	{
		this.needsRecipeUpdate = true;
	}
	
	public void markOutputInventoryCHanged()
	{
		this.setChanged();
		this.onOutputInventoryChanged();
	}
	
	public void onOutputInventoryChanged()
	{
		this.needsOutputUpdate = true;
	}
	
	public int getMaxSimultaneousRecipes()
	{
		return 1 + this.multiprocessUpgradeHandler.getStackInSlot(0).getCount();
	}
	
	/** Called at the start of a tick when the input inventory has changed **/
	public void updateRecipes()
	{
		ClaimableRecipeWrapper wrapper = this.input.getFreshRecipeInput();
		// get all recipes allowed by furnace or jumbo furnace
		// sort them by specificity (can we do this on recipe reload?)
		// recipes requiring multiple ingredients = most important, ingredients with more matching items (tags) = less important
		List<JumboFurnaceRecipe> recipes = RecipeSorter.INSTANCE.getSortedFurnaceRecipes(this.level.getRecipeManager());
		// start assigning input slots to usable recipes as they are found
		for (JumboFurnaceRecipe recipe : recipes)
		{
			// loop recipe over inputs until it can't match or we have no unused inputs left
			while (wrapper.getRecipeCount() < this.getMaxSimultaneousRecipes() && wrapper.matchAndClaimInputs(recipe, this.level) && wrapper.hasUnusedInputsLeft());
		}
		// when all input slots are claimed or the recipe list is exhausted, set the new recipe cache
		this.cachedRecipes = wrapper;
		this.needsRecipeUpdate = false;
		this.needsOutputUpdate = true;
	}
	
	/** Called at the start of a tick when the output inventory has changed, or if the recipe cache has updated**/
	public void updateOutput()
	{
		this.isRoomToCook = this.checkIfRoomToCook();
		this.needsOutputUpdate = false;
	}
	
	public boolean checkIfRoomToCook()
	{
		// make copy of output slots
		int slots = this.output.getSlots();
		ItemStackHandler outputSimulator = new ItemStackHandler(slots);
		for (int slot=0; slot<slots; slot++)
		{
			outputSimulator.setStackInSlot(slot, this.output.getStackInSlot(slot).copy());
		}
		
		// see if we can fill every output slot
		// items with container items almost always have a stack size of 1
		// we can generally assume that they can be placed back into the input slots
		// so we only need to be concerned with the canonical output
		for (Recipe<ClaimableRecipeWrapper> recipe : this.cachedRecipes.getRecipes())
		{
			ItemStack result = recipe.assemble(this.cachedRecipes).copy();
			for (int slot=0; slot < slots && !result.isEmpty(); slot++)
			{
				result = outputSimulator.insertItem(slot, result, false);
			}
			// if we couldn't fit the result in the output, no room to cook
			if (!result.isEmpty())
			{
				return false;
			}
		}
		
		// if we made it this far, all recipe results have room
		return true;
	}
	
	public void updateFuel()
	{
		this.canConsumeFuel = this.checkIfCanConsumeFuel();
		this.needsFuelUpdate = false;
	}
	
	public boolean checkIfCanConsumeFuel()
	{
		int slots = this.fuel.getSlots();
		for (int slot=0; slot<slots; slot++)
		{
			if (JumboFurnaceUtils.getJumboSmeltingBurnTime(this.fuel.getStackInSlot(slot)) > 0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	protected void serverTick()
	{
		// if burning, decrement burn time
		boolean dirty = false;
		boolean wasBurningBeforeTick = this.isBurning();
		if (wasBurningBeforeTick)
		{
			this.burnTimeRemaining -= this.getBurnConsumption();
			dirty = true;
		}
		
		if (!this.level.isClientSide)
		{
			// reinform self of own state if inventories have changed
			if (this.needsRecipeUpdate)
			{
				this.updateRecipes();
			}
			if (this.needsOutputUpdate)
			{
				this.updateOutput();
			}
			if (this.needsFuelUpdate)
			{
				this.updateFuel();
			}
			
			boolean hasSmeltableInputs = this.cachedRecipes.getRecipeCount() > 0;
			
			// if burning, or if it can consume fuel and has a smeltable input
			if (this.isBurning() || (this.canConsumeFuel && hasSmeltableInputs))
			{
				// if not burning but can start cooking
				// this also implies that we can consume fuel
				if (!this.isBurning() && hasSmeltableInputs)
				{
					// consume fuel and start burning
					this.consumeFuel();
				}
				
				// if burning and has smeltable inputs
				if (this.isBurning() && hasSmeltableInputs)
				{
					// increase cook progress
					this.cookProgress++;
					
					// if cook progress is complete, reset cook progress and do crafting
					if (this.cookProgress >= JumboFurnace.get().serverConfig.jumboFurnaceCookTime().get())
					{
						this.cookProgress = 0;
						this.craft();
					}
					dirty = true;
				}
				else // otherwise, reset cook progress
				{
					this.cookProgress = 0;
					dirty = true;
				}
			}
			// otherwise, if not burning but has cookprogress, reduce cook progress
			else if (!this.isBurning() && this.cookProgress > 0)
			{
				if (hasSmeltableInputs)
				{
					this.cookProgress = Math.max(0, this.cookProgress - 2);
				}
				else
				{
					this.cookProgress = 0;
				}
				dirty = true;
			}
			
			boolean isBurningAfterTick = this.isBurning();
			
			// if burning state changed since tick started, update furnace blockstates
			if (isBurningAfterTick != wasBurningBeforeTick)
			{
				this.updateBurningBlockstates(isBurningAfterTick);
			}
			
			if (dirty)
			{
				this.setChanged();
				BlockPos.betweenClosedStream(this.getBlockPos().offset(-1,-1,-1), this.getBlockPos().offset(1,1,1)).forEach(subPos ->
				{
					BlockState state = this.level.getBlockState(subPos);
					this.level.updateNeighborsAt(subPos, state.getBlock());
				});
					
			}
			
			
		}
	}
	
	public void consumeFuel()
	{
		int slots = this.fuel.getSlots();
		for (int slot=0; slot<slots; slot++)
		{
			ItemStack stackInSlot = this.fuel.extractItem(slot, 1, true);
			int burnTime = JumboFurnaceUtils.getJumboSmeltingBurnTime(stackInSlot);
			if (burnTime > 0)
			{
				this.lastItemBurnedValue = burnTime;
				this.fuel.extractItem(slot, 1, false);
				this.burnTimeRemaining += burnTime;
				// setStackInSlot ignores isItemValidForSlot, so we won't lose the container item here
				// TODO we'd lose a stack if someone makes a stackable item with a container item, though
				if (stackInSlot.hasCraftingRemainingItem())
				{
					this.fuel.setStackInSlot(slot, stackInSlot.getCraftingRemainingItem());
				}
				break;
			}
		}
	}
	
	public void craft()
	{
		// we have safeguards to make sure there's room for everything
		// but in case we're wrong, make a failsafe for items we can't put somewhere else
		// and eject them instead of just deleting them
		List<ItemStack> extraItems = new ArrayList<>();
		// put results in output
		List<Recipe<ClaimableRecipeWrapper>> recipes = this.cachedRecipes.getRecipes();
		IItemHandler unusedInputs = this.cachedRecipes.getUnusedInputs();
		int unusedInputSlots = unusedInputs.getSlots();
		for (Recipe<ClaimableRecipeWrapper> recipe : recipes)
		{
			ItemStack result = recipe.assemble(this.cachedRecipes);
			int outputSlots = this.output.getSlots();
			int resultCount = result.getCount();
			for (int slot=0; slot<outputSlots && !result.isEmpty(); slot++)
			{
				int oldCount = result.getCount();
				result = this.output.insertCraftResult(slot, result, false);
				int newCount = result.getCount();
				int itemsInserted = oldCount - newCount;
				float experience = ((float)itemsInserted / (float)resultCount) * (recipe instanceof JumboFurnaceRecipe jumboFurnaceRecipe ? jumboFurnaceRecipe.experience : 0F);
				this.output.addExperience(slot, experience);
			}
			if (!result.isEmpty())
			{
				extraItems.add(result);
			}
			for (ItemStack stack : recipe.getRemainingItems(this.cachedRecipes))
			{
				ItemStack containerItem = stack.copy();
				for (int slot=0; slot<unusedInputSlots && !containerItem.isEmpty(); slot++)
				{
					containerItem = unusedInputs.insertItem(slot, containerItem, false);
				}
				if (!containerItem.isEmpty())
				{
					extraItems.add(stack.copy());
				}
			}
			
		}
		
		// replace inputs with unused inputs + container items
		for (int slot=0; slot<unusedInputSlots; slot++)
		{
			this.input.setStackInSlot(slot, unusedInputs.getStackInSlot(slot));
		}
		
		// if we didn't have room for items, just yeet them
		// check for possible hoppers, etc first though
		BlockEntity te = this.level.getBlockEntity(this.worldPosition.below(2));
		
		for (ItemStack stack : extraItems)
		{
			ItemStack stackCopy = stack.copy();
			if (te != null)
			{
				stackCopy = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).map(handler ->
				{
					ItemStack innerStackCopy = stack.copy();
					int slots = handler.getSlots();
					for (int slot=0; slot<slots; slot++)
					{
						innerStackCopy = handler.insertItem(slot, innerStackCopy, false);
					}
					return innerStackCopy;
				})
				.orElse(stackCopy);
			}
			if (!stackCopy.isEmpty())
			{
				Containers.dropItemStack(this.level, this.worldPosition.getX()+0.5, this.worldPosition.getY() - 1.0, this.worldPosition.getZ() + 0.5, stack);
			}
		}
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		
		// make sure comparators reading from exterior blocks are updated as well
		MultiBlockHelper.get3x3CubeAround(this.worldPosition)
			.filter(exteriorPos -> !exteriorPos.equals(this.worldPosition))
			.forEach(exteriorPos -> this.level.updateNeighbourForOutputSignal(exteriorPos, this.getBlockState().getBlock()));
	}
	

}
