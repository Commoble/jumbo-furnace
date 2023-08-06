package commoble.jumbofurnace.recipes;

import commoble.jumbofurnace.JumboFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class JumboFurnaceRecipe implements Recipe<ClaimableRecipeWrapper>
{
	public final RecipeType<?> type;
	public final ResourceLocation id;
	public final String group;
	public final NonNullList<Ingredient> ingredients;
	public final ItemStack result;
	public final float experience;
	
	/** Wrapper around regular furnace recipes to make single-input jumbo furnace recipes **/
	public JumboFurnaceRecipe(SmeltingRecipe baseRecipe)
	{
		this(JumboFurnace.get().jumboSmeltingRecipeType.get(), baseRecipe.getId(), baseRecipe.getGroup(), baseRecipe.getIngredients(), baseRecipe.result.copy(), baseRecipe.getExperience());
	}
	
	public JumboFurnaceRecipe(RecipeType<?> type, ResourceLocation id, String group, NonNullList<Ingredient> ingredients, ItemStack result, float experience)
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
	public boolean matches(ClaimableRecipeWrapper inv, Level worldIn)
	{
		IItemHandler unusedInputs = inv.getUnusedInputs();
		for (Ingredient ingredient : this.getIngredients())
		{
			int amountOfIngredient = ingredient.getItems()[0].getCount();
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
	public ItemStack assemble(ClaimableRecipeWrapper inv, RegistryAccess registries)
	{
		return this.result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return true;
	}

	@Override
	public ItemStack getResultItem(RegistryAccess registries)
	{
		return this.result;
	}

	@Override
	public ResourceLocation getId()
	{
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return JumboFurnace.get().jumboSmeltingRecipeSerializer.get();
	}

	@Override
	public RecipeType<?> getType()
	{
		return this.type;
	}
	
	@Override
	public boolean isSpecial()
	{
		return true;
	}
	
	public int getSpecificity()
	{
		int specificity = (int)(this.experience*10);
		int totalItems = ForgeRegistries.ITEMS.getKeys().size();
		for (Ingredient ingredient : this.ingredients)
		{
			ItemStack[] matchingStacks = ingredient.getItems();
			if(matchingStacks.length < 1)
			{
				continue;
			}
			// safe to assume that ingredient has at least one matching stack, and at least two items are registered to forge
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
			if (stackInSlot.hasCraftingRemainingItem())
			{
				containerItems.set(slot, stackInSlot.getCraftingRemainingItem());
			}
		}
		
		return containerItems;
	}

}
