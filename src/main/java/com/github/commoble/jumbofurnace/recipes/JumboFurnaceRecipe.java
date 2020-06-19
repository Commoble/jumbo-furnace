package com.github.commoble.jumbofurnace.recipes;

import com.github.commoble.jumbofurnace.JumboFurnaceObjects;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class JumboFurnaceRecipe implements IRecipe<ClaimableRecipeWrapper>
{
	public final IRecipeType<?> type;
	public final ResourceLocation id;
	public final String group;
	public final Ingredient ingredient;
	public final ItemStack result;
	public final float experience;
	
	public JumboFurnaceRecipe(IRecipeType<?> type, ResourceLocation id, String group, Ingredient ingredient, ItemStack result, float experience)
	{
		this.type = type;
		this.id = id;
		this.group = group;
		this.ingredient = ingredient;
		this.result = result;
		this.experience = experience;
	}

	@Override
	public boolean matches(ClaimableRecipeWrapper inv, World worldIn)
	{
		IntSet remainingSlots = inv.unclaimedSlots;
		IntSet foundSlots = new IntOpenHashSet();
		remainingSlots.addAll(inv.unclaimedSlots);
		NonNullList<Ingredient> ingredients = this.getIngredients();
		int ingredientCount = ingredients.size();
		
		// if there are more ingredients we need to match than slots that they can match, we can't match
		if (ingredientCount > remainingSlots.size())
		{
			return false;
		}
		
		for (int i=0; i < ingredientCount; i++)
		{
			boolean matchedIngredient = false;
			for (int slot : remainingSlots)
			{
				if (!foundSlots.contains(slot) && ingredients.get(i).test(inv.getStackInSlot(slot)))
				{
					foundSlots.add(slot);
					matchedIngredient = true;
					break;
				}
			}
			if (!matchedIngredient)
			{
				return false;
			}
		}
		
		// if we got this far, all of our ingredients matched
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

}
