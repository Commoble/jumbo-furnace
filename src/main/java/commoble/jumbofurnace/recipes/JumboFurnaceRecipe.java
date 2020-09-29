package commoble.jumbofurnace.recipes;

import commoble.jumbofurnace.JumboFurnace;
import commoble.jumbofurnace.JumboFurnaceObjects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class JumboFurnaceRecipe implements IRecipe<ClaimableRecipeWrapper>
{
	public final IRecipeType<?> type;
	public final ResourceLocation id;
	public final String group;
	public final NonNullList<Ingredient> ingredients;
	public final ItemStack result;
	public final float experience;
	
	/** Wrapper around regular furnace recipes to make single-input jumbo furnace recipes **/
	public JumboFurnaceRecipe(FurnaceRecipe baseRecipe)
	{
		this(JumboFurnace.JUMBO_SMELTING_RECIPE_TYPE, baseRecipe.getId(), baseRecipe.getGroup(), baseRecipe.getIngredients(), baseRecipe.getRecipeOutput().copy(), baseRecipe.getExperience());
	}
	
	public JumboFurnaceRecipe(IRecipeType<?> type, ResourceLocation id, String group, NonNullList<Ingredient> ingredients, ItemStack result, float experience)
	{
		this.type = type;
		this.id = id;
		this.group = group;
		this.ingredients = ingredients;
		this.result = result;
		this.experience = experience;
	}

	@Override
	public NonNullList<Ingredient> getIngredients()
	{
		return this.ingredients;
	}

	@Override
	public boolean matches(ClaimableRecipeWrapper inv, World worldIn)
	{
		IItemHandler unusedInputs = inv.getUnusedInputs();
		NonNullList<Ingredient> ingredients = this.getIngredients();
		for (Ingredient ingredient : ingredients)
		{
			int amountOfIngredient = ingredient.getMatchingStacks()[0].getCount();
			int slots = unusedInputs.getSlots();
			for (int slot=0; slot < slots && amountOfIngredient > 0; slot++)
			{
				ItemStack stackInSlot = unusedInputs.getStackInSlot(slot);
				if (ingredient.test(stackInSlot) && stackInSlot.getCount() >= amountOfIngredient)
				{
					ItemStack usedStack = unusedInputs.extractItem(slot, amountOfIngredient, false);
					amountOfIngredient -= usedStack.getCount();
				}
			}
			
			// if we didn't fully match the ingredient, return false
			if (amountOfIngredient > 0)
			{
				return false;
			}
		}
		
		// if we made it this far, all ingredients matched, so return true
		return true;
	}

	@Override
	public ItemStack getCraftingResult(ClaimableRecipeWrapper inv)
	{
		return this.result.copy();
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return true;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return this.result;
	}

	@Override
	public ResourceLocation getId()
	{
		return this.id;
	}

	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return JumboFurnaceObjects.RECIPE_SERIALIZER;
	}

	@Override
	public IRecipeType<?> getType()
	{
		return this.type;
	}
	
	public int getSpecificity()
	{
		int specificity = (int)(this.experience*10);
		int totalItems = ForgeRegistries.ITEMS.getKeys().size();
		for (Ingredient ingredient : this.ingredients)
		{
			ItemStack[] matchingStacks = ingredient.getMatchingStacks();
			// safe to assume that recipe has at least one ingredient, and at least two items are registered to forge
			int matchingItems = Math.min(totalItems, matchingStacks.length);
			
			// this equation gives a value of 1D when matchingitems = 1, and 0D when matchingItems = totalItems
			double matchFactor = (double)(totalItems - matchingItems) / (double)(totalItems - 1);
			
			int ingredientWeight = (int)(100D * matchingStacks[0].getCount() * matchFactor);
			specificity += ingredientWeight;
		}
		return specificity;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(ClaimableRecipeWrapper inv)
	{
		IItemHandler items = inv.getItemsBeingSmelted();
		int slots = items.getSlots();
		NonNullList<ItemStack> containerItems = NonNullList.withSize(items.getSlots(), ItemStack.EMPTY);
		for (int slot=0; slot<slots; slot++)
		{
			ItemStack stackInSlot = items.getStackInSlot(slot);
			if (stackInSlot.hasContainerItem())
			{
				containerItems.set(slot, stackInSlot.getContainerItem());
			}
		}
		
		return containerItems;
	}

}
