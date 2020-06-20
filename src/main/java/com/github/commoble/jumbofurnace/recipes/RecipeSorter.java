package com.github.commoble.jumbofurnace.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.commoble.jumbofurnace.JumboFurnace;
import com.google.common.collect.Streams;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;

public class RecipeSorter extends ReloadListener<Void>
{
	public static final RecipeSorter INSTANCE = new RecipeSorter();
	
	// keep track of when recipes have reloaded
	private int currentGeneration = 0;
	private int lastKnownGeneration = -1;
	private List<JumboFurnaceRecipe> cachedSortedRecipes = new ArrayList<>();
	
	public List<JumboFurnaceRecipe> getSortedFurnaceRecipes(RecipeManager manager)
	{
		if (this.currentGeneration != this.lastKnownGeneration)
		{
			this.cachedSortedRecipes = this.sortFurnaceRecipes(manager);
			this.lastKnownGeneration = this.currentGeneration;
		}
		
		return this.cachedSortedRecipes;
	}
	
	private List<JumboFurnaceRecipe> sortFurnaceRecipes(RecipeManager manager)
	{
		Stream<JumboFurnaceRecipe> basicRecipes = manager.getRecipes(IRecipeType.SMELTING).values().stream()
			.filter(recipe -> recipe instanceof FurnaceRecipe)
			.map(recipe -> new JumboFurnaceRecipe((FurnaceRecipe)recipe));
		Stream<JumboFurnaceRecipe> advancedRecipes = manager.getRecipes(JumboFurnace.JUMBO_SMELTING_RECIPE_TYPE).values().stream()
			.filter(recipe -> recipe instanceof JumboFurnaceRecipe)
			.map(recipe -> (JumboFurnaceRecipe)recipe);
		
		return Streams.concat(basicRecipes, advancedRecipes)
			.sorted(RecipeSorter::compareRecipes)
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

	@Override
	protected Void prepare(IResourceManager resourceManagerIn, IProfiler profilerIn)
	{
		return null;
	}

	@Override
	protected void apply(Void objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn)
	{
		this.currentGeneration++;
	}
}
