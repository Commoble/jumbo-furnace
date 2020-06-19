package com.github.commoble.jumbofurnace.recipes;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;

public class RecipeHelper
{
	private static List<List<IRecipe<?>>> cachedRecipesByIngredientCount = ImmutableList.of();
	
	public static List<JumboFurnaceRecipe> getSortedFurnaceRecipes(RecipeManager manager)
	{
		return manager.getRecipes(IRecipeType.SMELTING).values().stream()
			.filter(recipe -> recipe instanceof FurnaceRecipe)
			.map(recipe -> new JumboFurnaceRecipe((FurnaceRecipe)recipe))
			.sorted(RecipeHelper::compareRecipes)
			.collect(Collectors.toList());
	}
	
	/*
	 * Compares two recipes such that the one that requires more ingredients and more specific ingredients
	 * will be first in a list when sorted
	 */
	public static int compareRecipes(JumboFurnaceRecipe a, JumboFurnaceRecipe b)
	{
		// recipe with higher specificity should be lower when compared, so flip the order
		return b.getSpecificity() - a.getSpecificity();
	}
}
