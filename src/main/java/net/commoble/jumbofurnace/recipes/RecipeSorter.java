package net.commoble.jumbofurnace.recipes;

import java.util.ArrayList;
import java.util.List;

import net.commoble.jumbofurnace.JumboFurnace;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class RecipeSorter extends SimplePreparableReloadListener<Void>
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
		List<JumboFurnaceRecipe> recipes = new ArrayList<>();
		for (var holder : manager.getAllRecipesFor(RecipeType.SMELTING))
		{
			SmeltingRecipe recipe = holder.value();
			if (recipe.getCookingTime() <= JumboFurnace.get().serverConfig.jumboFurnaceCookTime().get())
			{
				recipes.add(new JumboFurnaceRecipe(recipe));
			}
		}
		for (var holder : manager.getAllRecipesFor(JumboFurnace.get().jumboSmeltingRecipeType.get()))
		{
			recipes.add(holder.value());
		}
		recipes.sort(RecipeSorter::compareRecipes);
		return recipes;
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
	protected Void prepare(ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
	{
		return null;
	}

	@Override
	protected void apply(Void objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
	{
		this.currentGeneration++;
	}
}
