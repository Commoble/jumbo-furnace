package net.commoble.jumbofurnace.jumbo_furnace;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.commoble.jumbofurnace.JumboFurnace;
import net.commoble.jumbofurnace.JumboFurnaceUtils;
import net.commoble.jumbofurnace.recipes.InFlightRecipe;
import net.commoble.jumbofurnace.recipes.RecipeSorter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class JumboFurnaceCoreBlockEntity extends BlockEntity
{
	public static final String INPUT = "input";
	public static final String FUEL = "fuel";
	public static final String OUTPUT = "output";
	public static final String MULTIPROCESS_UPGRADES = "multiprocess_upgrades";
	public static final String BURN_TIME = "burn_time";
	public static final String BURN_VALUE = "burn_value";
	public static final String RECIPES = "recipes";
	public static final String BACKSTOCK = "backstock";
	public static final BlockEntityTicker<JumboFurnaceCoreBlockEntity> SERVER_TICKER = (level,pos,state,core)->core.serverTick();
	
	public static final Codec<List<InFlightRecipe>> INFLIGHT_RECIPES_CODEC = InFlightRecipe.CODEC.listOf();
	public static final Codec<List<ItemStack>> BACKSTOCK_CODEC = ItemStack.CODEC.listOf();
	
	public final InputItemHandler input = new InputItemHandler(this);
	public final ItemStackHandler fuel = new FuelItemHandler(this);
	public final OutputItemHandler output = new OutputItemHandler(this);
	public final MultiprocessUpgradeHandler multiprocessUpgradeHandler = new MultiprocessUpgradeHandler(this);

	public List<InFlightRecipe> inFlightRecipes = new ArrayList<>();
	public List<ItemStack> backstock = new ArrayList<>();
	
	/**
	 * cached copy of output slots and inflight recipe results, tossed on relevant updates
	 */
	public IItemHandler outputSimulatorCache = null;
	
	public int burnTimeRemaining = 0;
	public int lastItemBurnedValue = 200;
	 // check inventory on the first tick in case the furnace somehow comes into existence with stuff already in it
	public boolean shouldCheckRecipes = true;
	
	public static JumboFurnaceCoreBlockEntity create(BlockPos pos, BlockState state)
	{
		return new JumboFurnaceCoreBlockEntity(JumboFurnace.get().jumboFurnaceCoreBlockEntityType.get(), pos, state);
	}
	
	protected JumboFurnaceCoreBlockEntity(BlockEntityType<? extends JumboFurnaceCoreBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public void loadAdditional(CompoundTag compound, HolderLookup.Provider registries)
	{
		super.loadAdditional(compound, registries);
		this.input.deserializeNBT(registries, compound.getCompoundOrEmpty(INPUT));
		this.fuel.deserializeNBT(registries, compound.getCompoundOrEmpty(FUEL));
		this.output.deserializeNBT(registries, compound.getCompoundOrEmpty(OUTPUT));
		this.multiprocessUpgradeHandler.deserializeNBT(registries, compound.getCompoundOrEmpty(MULTIPROCESS_UPGRADES));
		this.inFlightRecipes = Lists.newArrayList(INFLIGHT_RECIPES_CODEC.parse(NbtOps.INSTANCE, compound.getCompoundOrEmpty(RECIPES)).result().orElse(List.of()));
		this.backstock = Lists.newArrayList(BACKSTOCK_CODEC.parse(NbtOps.INSTANCE, compound.getCompoundOrEmpty(BACKSTOCK)).result().orElse(List.of()));
		this.burnTimeRemaining = compound.getIntOr(BURN_TIME,0);
		this.lastItemBurnedValue = compound.getIntOr(BURN_VALUE,0);
	}

	@Override
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider registries)
	{
		super.saveAdditional(compound, registries);
		compound.put(INPUT, this.input.serializeNBT(registries));
		compound.put(FUEL, this.fuel.serializeNBT(registries));
		compound.put(OUTPUT, this.output.serializeNBT(registries));
		compound.put(MULTIPROCESS_UPGRADES, this.multiprocessUpgradeHandler.serializeNBT(registries));
		INFLIGHT_RECIPES_CODEC.encodeStart(NbtOps.INSTANCE, this.inFlightRecipes).ifSuccess(tag -> compound.put(RECIPES, tag));
		BACKSTOCK_CODEC.encodeStart(NbtOps.INSTANCE, this.backstock).ifSuccess(tag -> compound.put(BACKSTOCK, tag));
		compound.putInt(BURN_TIME, this.burnTimeRemaining);
		compound.putInt(BURN_VALUE, this.lastItemBurnedValue);
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
		this.shouldCheckRecipes = true;
		this.setChanged();
	}
	
	public void markInputInventoryChanged()
	{
		this.shouldCheckRecipes = true;
		this.setChanged();
	}
	
	public void markOutputInventoryChanged()
	{
		this.outputSimulatorCache = null;
		this.shouldCheckRecipes = true;
		this.setChanged();
	}
	
	public int getMaxSimultaneousRecipes()
	{
		return 1 + this.multiprocessUpgradeHandler.getStackInSlot(0).getCount();
	}
	
	protected void serverTick()
	{
		if (!(this.level instanceof ServerLevel serverLevel))
			return;
		boolean wasBurningBeforeTick = this.burnTimeRemaining > 0;
		// if we're on fire, we're definitely going to call setChanged later because we're going to decrement heat
		boolean dirty = wasBurningBeforeTick;
		
		// okay this is everything we need to do:
		
		// we theoretically have a list of inflight recipes which should progress as long as heat and/or fuel are available
		// we can consume fuel on-demand to add more heat
		
		// firstly check if we can consume any inputs and transition the itemstacks to inflight recipes
		// if we have heat, great
		// if we don't have heat, but we have fuel, we may be able to burn it depending on the inventory state
		
		// then if we have heat or fuel, process each inflight recipe
		if (this.hasHeatOrFuel())
		{
			boolean processedAnyRecipes = this.processInflightRecipes();
			if (processedAnyRecipes)
			{
				dirty = true;
			}
			// if we didn't increment any recipes, decrement heat (always lose at least one heat/tick)
			else if (this.burnTimeRemaining > 0)
			{
				this.burnTimeRemaining --;
				dirty = true;
			}
		}
		
		// we don't want to check recipes or fuel stock unless we need to
		// for fuel stock, "we need to" = "fuel inventory has updated since we last checked"
		// for recipes, we check if
			// we are unlit and fuel stock has increased since we last checked, or
			// inputs have been added to since we last checked recipes, or
			// outputs have decreased since we last checked recipes
		if (this.hasHeatOrFuel() && this.shouldCheckRecipes())
		{
			boolean processedAnyInputs = this.processInputs(serverLevel.recipeAccess().recipeMap());
			if (processedAnyInputs)
			{
				dirty = true;
			}
		}
		// then update litness and update neighbors if necessary
		boolean isBurningAfterTick = this.burnTimeRemaining > 0;
		
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
		
		
//		// if burning, decrement burn time
//		boolean dirty = false;
//		boolean wasBurningBeforeTick = this.isBurning();
//		if (wasBurningBeforeTick)
//		{
//			this.burnTimeRemaining -= this.getBurnConsumption();
//			dirty = true;
//		}
//		// reinform self of own state if inventories have changed
//		if (this.needsRecipeUpdate)
//		{
//			this.updateRecipes();
//		}
//		if (this.needsOutputUpdate)
//		{
//			this.updateOutput();
//		}
//		if (this.needsFuelUpdate)
//		{
//			this.updateFuel();
//		}
//		
//		boolean hasSmeltableInputs = this.cachedRecipes.getRecipeCount() > 0;
//		
//		// if burning, or if it can consume fuel and has a smeltable input
//		if (this.isBurning() || (this.canConsumeFuel && hasSmeltableInputs))
//		{
//			// if not burning but can start cooking
//			// this also implies that we can consume fuel
//			if (!this.isBurning() && hasSmeltableInputs)
//			{
//				// consume fuel and start burning
//				this.consumeFuel();
//			}
//			
//			// if burning and has smeltable inputs
//			if (this.isBurning() && hasSmeltableInputs)
//			{
//				// increase cook progress
//				this.cookProgress++;
//				
//				// if cook progress is complete, reset cook progress and do crafting
//				if (this.cookProgress >= JumboFurnace.get().serverConfig.jumboFurnaceCookTime().get())
//				{
//					this.cookProgress = 0;
//					this.craft();
//				}
//				dirty = true;
//			}
//			else // otherwise, reset cook progress
//			{
//				this.cookProgress = 0;
//				dirty = true;
//			}
//		}
//		// otherwise, if not burning but has cookprogress, reduce cook progress
//		else if (!this.isBurning() && this.cookProgress > 0)
//		{
//			if (hasSmeltableInputs)
//			{
//				this.cookProgress = Math.max(0, this.cookProgress - 2);
//			}
//			else
//			{
//				this.cookProgress = 0;
//			}
//			dirty = true;
//		}
	}
	
	private boolean hasHeatOrFuel()
	{
		if (this.burnTimeRemaining > 0)
			return true;
		
		int slots = this.fuel.getSlots();
		for (int i=0; i<slots; i++)
		{
			if (!this.fuel.getStackInSlot(i).isEmpty())
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean shouldCheckRecipes()
	{
		if (this.shouldCheckRecipes)
		{
			this.shouldCheckRecipes = false;
			return true;
		}
		return false;
	}
	
	/**
	 * @return true if we processed any inputs
	 */
	private boolean processInputs(RecipeMap recipeMap)
	{
		int freeRecipeSlots = this.getMaxSimultaneousRecipes() - this.inFlightRecipes.size();
		if (freeRecipeSlots <= 0)
			return false;
		
		// we want to check the conceivably-craftable recipes in order of specificity
		// if we find a startable recipe, we start it
		// a recipe is startable if
		// A) the ingredients are findable in our input inventory
		// B) the ingredient's results, and any necessary fuel remainders, fit into the output inventory (along with current output contents and inflight recipe results)
		// so how do we determine that
		// firstly, we make sure not to check recipes to begin with unless we have any heat or fuel inventory
		// (we did that before we called this)
		
		// then we iterate over the relevant recipes
		// we can ask the recipe sorter to get all the recipes that the items that are actually in our furnace can be used in
		// the neat thing about furnace recipes is that most items are going to be used in like one recipe at most,
		// maybe more if the server has lots of multi-input recipes or there's a lot of mod overlap in the modpack
		// but this list is expected to be much smaller than just iterating over the entire recipe list each time
		Set<Item> currentInputItems = new ReferenceOpenHashSet<>();
		int slots = this.input.getSlots();
		for (int i=0; i<slots; i++)
		{
			ItemStack stack = this.input.getStackInSlot(i);
			if (!stack.isEmpty())
			{
				currentInputItems.add(stack.getItem());
			}
		}
		// create output simulator, we will use this to make sure we have room for recipe results + remainders
		IItemHandler outputSimulator = JumboFurnaceUtils.copyItemHandler(this.getOutputAndInFlightRecipeResults());
		
		// if we have no heat, create fuel simulator, consume fuel from it, add fuel remainder (if any) to output simulator
		// we do this because we may have an edge case where we must consume a lava bucket,
		// and we have room for a recipe result or a fuel remainder, but not both
		// we want to make sure we only begin a recipe if we have room for everything
		// so we simulate the consumption of fuel to determine whether we need to include fuel remainder
		ItemStack consumableFuel = ItemStack.EMPTY;
		int consumableFuelValue = 0;
		IItemHandler newFuelInventory = this.fuel;
		
		if (!this.isBurning())
		{
			FuelValues fuelValues = this.level.fuelValues();
			newFuelInventory = JumboFurnaceUtils.copyItemHandler(this.fuel); 
			consumableFuel = simulateConsumeFuel(newFuelInventory, outputSimulator, fuelValues);
			// if we can't consume fuel then we can't do anything else anyway
			consumableFuelValue = JumboFurnaceUtils.getJumboSmeltingBurnTime(consumableFuel, fuelValues);
			if (consumableFuelValue <= 0)
			{
				return false;
			}
			
		}
		
		boolean startedAnyRecipes = false;
		var recipes = RecipeSorter.SERVER_INSTANCE.getSortedFurnaceRecipesValidForInputs(currentInputItems, recipeMap);
		
		iterateRecipes:
		for (var recipe : recipes)
		{
			if (this.inFlightRecipes.size() >= this.getMaxSimultaneousRecipes())
			{
				break;
			}
			// if we find a matching recipe, we verify fuel for that recipe (either we have heat already or we can find and consume fuel respecting room for craft remainder of the fuel in output)
			// how do we efficiently match and pull out multiple items...
			// we can make a copy of the input inventory, and pull items out of that
			// then a few steps later, if we decide that the recipe matches and fits, we can replace the original inventory with our copy
			// (only fuel produces crafting remainders, recipe inputs do not)
			// (crafting remainders for inputs will have to be specified in the recipe itself)
			
			// attempt to pull each ingredient out
			// if we have all the ingredients:
			// if we have room in the output simulator for the results and remainders:
			// update the input from the simulator
			// update the real output simulator
			// repeat for this recipe until we run out of ingredients or recipe slots
			int maxIterations = this.getMaxSimultaneousRecipes() - this.inFlightRecipes.size();
			for (int recipeIteration = 0; recipeIteration < maxIterations; recipeIteration++)
			{
				IItemHandler inputSimulator = JumboFurnaceUtils.copyItemHandler(this.input);
				List<ItemStack> recipeInputs = new ArrayList<>();
				List<ItemStack> remainders = new ArrayList<>();
				for (SizedIngredient sizedIngredient : recipe.ingredients())
				{
					Ingredient ingredient = sizedIngredient.ingredient();
					int requiredPulls = sizedIngredient.count();
					for (int pull = 0; pull < requiredPulls; pull++)
					{
						// attempt to find matching stacks, pull them out of the input simulator
						boolean foundInput = false;
						int inputSlots = inputSimulator.getSlots();
						for (int inputSlot=0; inputSlot < inputSlots; inputSlot++)
						{
							// check each ingredient slot until we find one
							if (ingredient.test(inputSimulator.extractItem(inputSlot, 1, true)))
							{
								ItemStack inputStack = inputSimulator.extractItem(inputSlot, 1, false);
								recipeInputs.add(inputStack.copy());
								foundInput = true;
								break;
							}
						}
						// if we didn't find this ingredient, skip to the next recipe
						if (!foundInput) {
							continue iterateRecipes;
						}
					}
					// if we're still here, we found the required number of this ingredient
				}
				// if we're still here, we found every required ingredient
				// now check if we have room in the output simulator for results
				IItemHandler outputSimulatorForRecipe = JumboFurnaceUtils.copyItemHandler(outputSimulator);
				// TODO at some point we should support ingredient-sensitive outputs
				// currently neither vanilla smelting recipes nor jumbo recipes support this
				// (vanilla #assemble doesn't support multiple outputs)
				// this requires custom recipe serializers that use a jumbo smelting recipe type
				// so adding this feature would be in support of mods that are adding their own custom jumbo recipes
				// so we can add support if it's requested by other mods but otherwise we'll leave it alone
				for (ItemStack stack : recipe.results())
				{
					// use insertItemStacked to prioritize slots that already have partial stacks of that item in them
					if (!ItemHandlerHelper.insertItemStacked(outputSimulatorForRecipe, stack.copy(), false).isEmpty())
					{
						continue iterateRecipes;
					}
				}
				for (ItemStack stack : remainders)
				{
					if (!ItemHandlerHelper.insertItemStacked(outputSimulatorForRecipe, stack, false).isEmpty())
					{
						continue iterateRecipes;
					}
				}
				// this recipe is 100% valid, we have all inputs and we have room for all outputs
				// update the output simulator as it now has the recipe results and remainders
				outputSimulator = outputSimulatorForRecipe;
				// update the input from the simulator
				JumboFurnaceUtils.copyItemHandlerTo(inputSimulator, this.input);
				// start a new inflight recipe
				this.inFlightRecipes.add(new InFlightRecipe(recipe, recipeInputs));
				// add the remainders to the real output
				for (ItemStack stack : remainders)
				{
					addToOutputOrBackstock(stack);
				}
				startedAnyRecipes = true;
			}
			
		}
		// if we simulated consuming fuel AND processed at least one recipe,
		// update burn time and fuel inventory
		
		if (consumableFuelValue > 0 && startedAnyRecipes)
		{
			this.burnTimeRemaining += consumableFuelValue;
			this.lastItemBurnedValue = consumableFuelValue;
			JumboFurnaceUtils.copyItemHandlerTo(newFuelInventory, this.fuel);
			ItemStack fuelRemainder = consumableFuel.getCraftingRemainder();
			if (!fuelRemainder.isEmpty())
			{
				this.addToFuelOrOutputOrBackstock(fuelRemainder.copy());
			}
		}
		
		// what about the output simulator...
		// if we simulated consuming fuel, any remainders are already in the output simulator copy
		// if we began any recipes, any remainders are added to the output simulator copy
		// so we can promote the output simulator copy to the cache in either of these cases
		// wait, no, what if we had no fuel, so we consumed a lava bucket, but didn't process any recipes
		// then we should NOT promote the output simulator
		// starting any recipes is both the necessary and sufficient condition
		// (but if we broke the cache because of unexpected backstock, do not update the cache, let it be re-evaluated later)
		if (startedAnyRecipes && this.outputSimulatorCache != null)
		{
			this.outputSimulatorCache = outputSimulator;
		}
		
		return startedAnyRecipes;
	}
	
	/**
	 * @return true if any recipes were progressed
	 */
	private boolean processInflightRecipes()
	{
		if (this.inFlightRecipes.isEmpty())
			return false;
		
		// we'll also need to remember whether any fuel can be conceivably consumed
		// so we don't keep checking on each subsequent recipe
		boolean anyFuelLeftToCheck = true;
		boolean progressedAnyRecipes = false;
		
		// we can't modify a list while we iterate over it, so we need to copy the results to a new list as we iterate
		List<InFlightRecipe> remainingRecipes = new ArrayList<>();
		for (InFlightRecipe recipe : this.inFlightRecipes)
		{
			if (anyFuelLeftToCheck)
			{
				// for each recipe, try to progress
				// if we have no heat, try to consume fuel
				if (this.tryHaveHeat())
				{
					// then if we have heat, increment recipe progress
					progressedAnyRecipes = true;
					this.burnTimeRemaining--;
					boolean completed = recipe.incrementProgress();
					// also, when a recipe gains 100% progress, we remove it from the inflight recipe list, and add its result to the output inventory
					if (completed)
					{
						// if we concluded a recipe
						for (ItemStack stack : recipe.recipe().results())
						{
							this.addToOutputOrBackstock(stack.copy());
						}
						this.output.addExperience(recipe.recipe().experience());
						// we have more room for new recipes so we should check them again
						this.shouldCheckRecipes = true;
					}
					else
					{
						// if we didn't complete the recipe, return it to the list
						remainingRecipes.add(recipe);
					}
				}
				// but if we weren't able to have heat, then we can't progress any further recipes
				else
				{
					anyFuelLeftToCheck = false;
					remainingRecipes.add(recipe);
				}
			}
			else
			{
				remainingRecipes.add(recipe);
			}
		}
		this.inFlightRecipes = remainingRecipes;
		return progressedAnyRecipes;
	}
	
	/**
	 * Tries to put the furnace into a state of having heat.
	 * If we already have heat, we are good.
	 * Otherwise, tries to consume fuel based on current output simulation.
	 * @return
	 */
	private boolean tryHaveHeat()
	{
		return this.burnTimeRemaining > 0 || this.tryConsumeFuel(this.getOutputAndInFlightRecipeResults());
	}

	// let's say we're about to consume fuel so we can initiate or progress a recipe
	// usually this has no complications as long as we have fuel in the slots
	// but, suppose one of the fuel itemstacks has a crafting remaining item, like iron buckets
	// (this gets particularly nasty if the fuel itemstack is stackable)
	// we only want to consume that fuel if we can fit the remainder in the output slots at the time
	// (without compromising the outputtability of current inflight recipes;
	// inflight recipes must be guaranteed that they have room for output at their conclusion)
	// so we iterate over the slots and get a list of "what happens when this fuel is consumed"
	// i.e. the burn value and the craft remainder (if any)
	// (and the slot the fuel resides in)
	// then we can iterate over them and check the output fitness of each fuel stack.
	// Since remainderless fuels (most of them) are ALWAYS consumable, if we find one,
	// then we don't have to check further.
	
	// if we want to consume fuel to progress inflight recipes, that's fine, we already have the output+inflight results cache
	// so just iterate over fuel until we find one that fits
	
	// if we want to consume fuel to progress a potential recipe to start...
	// we're iterating over recipes first, then iterating over fuel until we find fuel that matches recipe results + inflight + output
	// so we don't need to return a list of all nine slots, just the first one that fits with the recipe
	
	// and we only need an output simulator, so we can use the same impl for both cases
	private boolean tryConsumeFuel(IItemHandler outputSimulator)
	{
		int slots = this.fuel.getSlots();
		FuelValues fuelValues = this.level.fuelValues();
		for (int slot=0; slot<slots; slot++)
		{
			ItemStack stackInSlot = this.fuel.extractItem(slot, 1, true);
			int burnTime = JumboFurnaceUtils.getJumboSmeltingBurnTime(stackInSlot, fuelValues);
			if (burnTime > 0)
			{
				ItemStack remainder = stackInSlot.getCraftingRemainder().copy();
				// if there is no remainder item, no further checks needed.
				// if there is a remainder item, use the fuel if it fits in the output simulator
				if (remainder.isEmpty()
					|| (JumboFurnaceUtils.getJumboSmeltingBurnTime(remainder, fuelValues) > 0 && ItemHandlerHelper.insertItemStacked(this.fuel, remainder, true).isEmpty())
					|| ItemHandlerHelper.insertItemStacked(outputSimulator, remainder, true).isEmpty())
				{
					this.fuel.extractItem(slot, 1, false);
					this.burnTimeRemaining += burnTime;
					this.lastItemBurnedValue = burnTime;
					this.addToFuelOrOutputOrBackstock(remainder);
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param fuelInventory Fuel inventory (simulated or otherwise). Will be modified if fuel would be consumed.
	 * @param outputInventory Output inventory (simulated or otherwise). Will be modified if consumed fuel has remainder item.
	 * @return ItemStack of the fuel which would be consumed. Returns EMPTY if no consumable fuel exists.
	 */
	private static ItemStack simulateConsumeFuel(IItemHandler fuelInventory, IItemHandler outputInventory, FuelValues fuelValues)
	{
		int slots = fuelInventory.getSlots();
		for (int slot=0; slot<slots; slot++)
		{
			ItemStack stackInSlot = fuelInventory.extractItem(slot, 1, false);
			if (stackInSlot.isEmpty())
			{
				continue;
			}
			int burnTime = JumboFurnaceUtils.getJumboSmeltingBurnTime(stackInSlot, fuelValues);
			if (burnTime > 0)
			{
				ItemStack remainder = stackInSlot.getCraftingRemainder();
				// if there is a remainder item, use the fuel if it fits in the output simulator
				// if there is no remainder item, no further checks needed.
				if (remainder.isEmpty())
				{
					return stackInSlot.copy(); 
				}
				// if remainder item is also a fuel, try to return it to the fuel inventory
				if (JumboFurnaceUtils.getJumboSmeltingBurnTime(remainder, fuelValues) > 0)
				{
					remainder = ItemHandlerHelper.insertItemStacked(fuelInventory, remainder.copy(), true);
					if (remainder.isEmpty())
					{
						return stackInSlot.copy();
					}
				}
				if (ItemHandlerHelper.insertItemStacked(outputInventory, remainder.copy(), false).isEmpty())
				{
					return stackInSlot.copy();
				}
			}
			// we didn't use the fuel so put it back
			fuelInventory.insertItem(slot, stackInSlot, false);
		}
		return ItemStack.EMPTY;
	}
	
	private IItemHandler getOutputAndInFlightRecipeResults()
	{
		if (this.outputSimulatorCache == null)
		{
			IItemHandler outputSimulator = JumboFurnaceUtils.copyItemHandler(this.output);
			for (InFlightRecipe recipe : this.inFlightRecipes)
			{
				for (ItemStack stack : recipe.recipe().results())
				{
					ItemHandlerHelper.insertItemStacked(outputSimulator, stack.copy(), false);
				}
			}
			this.outputSimulatorCache = outputSimulator;
		}
		return this.outputSimulatorCache;
	}
	
//	public void craft()
//	{
//		// we have safeguards to make sure there's room for everything
//		// but in case we're wrong, make a failsafe for items we can't put somewhere else
//		// and eject them instead of just deleting them
//		List<ItemStack> extraItems = new ArrayList<>();
//		// put results in output
//		List<Recipe<ClaimableRecipeWrapper>> recipes = this.cachedRecipes.getRecipe();
//		IItemHandler unusedInputs = this.cachedRecipes.getUnusedInputs();
//		int unusedInputSlots = unusedInputs.getSlots();
//		for (Recipe<ClaimableRecipeWrapper> recipe : recipes)
//		{
//			ItemStack result = recipe.assemble(this.cachedRecipes, this.level.registryAccess());
//			int outputSlots = this.output.getSlots();
//			int resultCount = result.getCount();
//			for (int slot=0; slot<outputSlots && !result.isEmpty(); slot++)
//			{
//				int oldCount = result.getCount();
//				result = this.output.insertCraftResult(slot, result, false);
//				int newCount = result.getCount();
//				int itemsInserted = oldCount - newCount;
//				float experience = ((float)itemsInserted / (float)resultCount) * (recipe instanceof JumboFurnaceRecipe jumboFurnaceRecipe ? jumboFurnaceRecipe.experience() : 0F);
//				this.output.addExperience(slot, experience);
//			}
//			if (!result.isEmpty())
//			{
//				extraItems.add(result);
//			}
//			for (ItemStack stack : recipe.getRemainingItems(this.cachedRecipes))
//			{
//				ItemStack containerItem = stack.copy();
//				for (int slot=0; slot<unusedInputSlots && !containerItem.isEmpty(); slot++)
//				{
//					containerItem = unusedInputs.insertItem(slot, containerItem, false);
//				}
//				if (!containerItem.isEmpty())
//				{
//					extraItems.add(stack.copy());
//				}
//			}
//			
//		}
//		
//		// replace inputs with unused inputs + container items
//		for (int slot=0; slot<unusedInputSlots; slot++)
//		{
//			this.input.setStackInSlot(slot, unusedInputs.getStackInSlot(slot));
//		}
//		
//		// if we didn't have room for items, just yeet them
//		// check for possible hoppers, etc first though
//		IItemHandler belowHandler = this.level.getCapability(Capabilities.ItemHandler.BLOCK, this.worldPosition.below(2), Direction.UP);
//		
//		for (ItemStack stack : extraItems)
//		{
//			ItemStack stackCopy = stack.copy();
//			if (belowHandler != null)
//			{
//				int slots = belowHandler.getSlots();
//				for (int slot=0; slot<slots; slot++)
//				{
//					stackCopy = belowHandler.insertItem(slot, stackCopy, false);
//				}
//			}
//			if (!stackCopy.isEmpty())
//			{
//				Containers.dropItemStack(this.level, this.worldPosition.getX()+0.5, this.worldPosition.getY() - 1.0, this.worldPosition.getZ() + 0.5, stack);
//			}
//		}
//	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		
		// make sure comparators reading from exterior blocks are updated as well
		MultiBlockHelper.get3x3CubeAround(this.worldPosition)
			.filter(exteriorPos -> !exteriorPos.equals(this.worldPosition))
			.forEach(exteriorPos -> this.level.updateNeighbourForOutputSignal(exteriorPos, this.getBlockState().getBlock()));
	}
	
	private void addToFuelOrOutputOrBackstock(ItemStack stack)
	{
		if (JumboFurnaceUtils.getJumboSmeltingBurnTime(stack, this.level.fuelValues()) > 0)
		{
			stack = ItemHandlerHelper.insertItemStacked(this.fuel, stack, false);
		}
		if (!stack.isEmpty())
		{
			addToOutputOrBackstock(stack);
		}
	}
	
	private void addToOutputOrBackstock(ItemStack stack)
	{
		ItemStack extraRemainder = this.output.insertCraftResult(stack, false);
		if (!extraRemainder.isEmpty())
		{
			// if we can't put the remainder in the output for some reason, keep it and we can maybe sneak it into player inventory later
			this.backstock.add(extraRemainder);
			this.outputSimulatorCache = null;
			this.shouldCheckRecipes = true;
		}
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state)
	{
		super.preRemoveSideEffects(pos, state);
		List<ItemStack> drops = new ArrayList<>();
		double x = pos.getX() + 0.5D;
		double y = pos.getY() + 0.5D;
		double z = pos.getZ() + 0.5D;
		float experience = this.output.storedExperience;
		// drop everything in the inventory slots
		for (int i=0; i<JumboFurnaceMenu.INPUT_SLOTS; i++)
		{
			drops.add(this.input.getStackInSlot(i));
			drops.add(this.fuel.getStackInSlot(i));
			drops.add(this.output.getStackInSlot(i));
		}
		drops.add(this.multiprocessUpgradeHandler.getStackInSlot(0));
		// drop the internal inventories too
		for (InFlightRecipe inflight : this.inFlightRecipes)
		{
			for (ItemStack input : inflight.inputs())
			{
				drops.add(input);
			}
		}
		for (ItemStack stack : this.backstock)
		{
			drops.add(stack);
		}
		// take all those items and spawn them in the world
		for (ItemStack drop : drops)
		{
			Containers.dropItemStack(level, x, y, z, drop);
		}
		Player player = level.getNearestPlayer(x, y, z, 16D, null);
		if (player != null)
		{
			JumboFurnaceOutputSlot.spawnExpOrbs(player, experience);
		}
	}
	
	
}
