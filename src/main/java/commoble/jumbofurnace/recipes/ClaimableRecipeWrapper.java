package commoble.jumbofurnace.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class ClaimableRecipeWrapper extends RecipeWrapper
{
	private final IItemHandler unusedInputs;
	private final IItemHandler inputsBeingSmelted;
	private List<IRecipe<ClaimableRecipeWrapper>> recipes = new ArrayList<>();

	public ClaimableRecipeWrapper(IItemHandlerModifiable inv)
	{
		super(inv);
		int slots = inv.getSlots();
		this.unusedInputs = new ItemStackHandler(slots);
		this.inputsBeingSmelted = new ItemStackHandler(slots);
		
		// copy original inventory into unused inputs
		for (int slot=0; slot<slots; slot++)
		{
			ItemStack stackCopy = inv.getStackInSlot(slot).copy();
			for (int i=0; i<slots && stackCopy.getCount() > 0; i++)
			{
				stackCopy = this.unusedInputs.insertItem(i, stackCopy, false);
			}
		}
		
	}
	
	public List<IRecipe<ClaimableRecipeWrapper>> getRecipes()
	{
		return this.recipes;
	}
	
	public IItemHandler getItemsBeingSmelted()
	{
		return this.inputsBeingSmelted;
	}
	
	public int getRecipeCount()
	{
		return this.recipes.size();
	}
	
	public boolean hasUnusedInputsLeft()
	{
		int slots = this.unusedInputs.getSlots();
		for (int slot=0; slot < slots; slot++)
		{
			if (!this.unusedInputs.getStackInSlot(slot).isEmpty())
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns a COPY of the list of non-empty items that haven't been claimed as recipe inputs. The items are also copies.
	 * @return
	 */
	public IItemHandler getUnusedInputs()
	{
		int slots = this.unusedInputs.getSlots();
		IItemHandler result = new ItemStackHandler(slots);
		for (int slot=0; slot < slots; slot++)
		{
			ItemStack stackInSlot = this.unusedInputs.getStackInSlot(slot);
			result.insertItem(slot, stackInSlot.copy(), false);
		}
		return result;
	}
	
	public boolean matchAndClaimInputs(IRecipe<ClaimableRecipeWrapper> recipe, World world)
	{
		boolean matched = recipe.matches(this, world);
		
		if (matched)
		{
			NonNullList<Ingredient> ingredients = recipe.getIngredients();
			for (Ingredient ingredient : ingredients)
			{
				int amountOfIngredient = ingredient.getMatchingStacks()[0].getCount();
				int slots = this.unusedInputs.getSlots();
				for (int slot=0; slot < slots && amountOfIngredient > 0; slot++)
				{
					ItemStack stackInSlot = this.unusedInputs.getStackInSlot(slot);
					if (ingredient.test(stackInSlot) && stackInSlot.getCount() >= amountOfIngredient)
					{
						ItemStack usedStack = this.unusedInputs.extractItem(slot, amountOfIngredient, false);
						int usedInputSlots = this.inputsBeingSmelted.getSlots();
						amountOfIngredient -= usedStack.getCount();
						for (int usedInputSlot = 0; usedInputSlot < usedInputSlots && usedStack.getCount() > 0; usedInputSlot++)
						{
							usedStack = this.inputsBeingSmelted.insertItem(usedInputSlot, usedStack, false);
						}
					}
				}
			}
			this.recipes.add(recipe);
		}
		
		return matched;
	}

	
}
