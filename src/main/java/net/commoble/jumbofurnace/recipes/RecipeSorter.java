package net.commoble.jumbofurnace.recipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.commoble.jumbofurnace.JumboFurnace;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class RecipeSorter extends SimplePreparableReloadListener<Void>
{
	public static final RecipeSorter SERVER_INSTANCE = new RecipeSorter();
	
	// keep track of when recipes have reloaded
	private int currentGeneration = 0;
	private int lastKnownGeneration = -1;
	private Map<Item, List<JumboFurnaceRecipe>> cachedSortedRecipes = new Reference2ObjectOpenHashMap<>();
	private List<JumboFurnaceRecipe> allRecipes = new ArrayList<>();
	
	public SortedSet<JumboFurnaceRecipe> getSortedFurnaceRecipesValidForInputs(Collection<Item> inputItems, RecipeMap recipeMap)
	{
		if (this.currentGeneration != this.lastKnownGeneration)
		{
			this.sortFurnaceRecipes(recipeMap);
			this.lastKnownGeneration = this.currentGeneration;
		}
		
		SortedSet<JumboFurnaceRecipe> recipesForItems = new ObjectRBTreeSet<>(RecipeSorter::compareRecipes);
		for (Item item : inputItems)
		{
			var recipesForItem = this.cachedSortedRecipes.get(item);
			if (recipesForItem != null)
			{
				recipesForItems.addAll(recipesForItem);
			}
		}
		
		return recipesForItems;
	}
	
	public List<JumboFurnaceRecipe> getAllSortedFurnaceRecipes(RecipeMap recipeMap)
	{
		if (this.currentGeneration != this.lastKnownGeneration)
		{
			this.sortFurnaceRecipes(recipeMap);
			this.lastKnownGeneration = this.currentGeneration;
		}
		return allRecipes;
	}
	
	private void sortFurnaceRecipes(RecipeMap recipeMap)
	{
		Map<Item, List<JumboFurnaceRecipe>> results = new Reference2ObjectOpenHashMap<>();
		SortedSet<JumboFurnaceRecipe> allRecipes = new ObjectRBTreeSet<>(RecipeSorter::compareRecipes);
		
		// we need to track the wrapped recipes so we don't put two copies of them in the results map
		Map<ResourceKey<Recipe<?>>, JumboFurnaceRecipe> wrappedRecipes = new HashMap<>();
		for (var holder : recipeMap.byType(RecipeType.SMELTING))
		{
			ResourceKey<Recipe<?>> key = holder.id();
			JumboFurnaceRecipe recipe = new JumboFurnaceRecipe(holder.value());
			allRecipes.add(recipe);
			Ingredient ingredient = holder.value().input();
			ingredient.getValues().forEach(itemHolder -> 
			{
				results.computeIfAbsent(itemHolder.value(), x -> new ArrayList<>())
					.add(wrappedRecipes.computeIfAbsent(key, x -> recipe));
			});
		}
		for (var holder : recipeMap.byType(JumboFurnace.get().jumboSmeltingRecipeType.get()))
		{
			JumboFurnaceRecipe recipe = holder.value();
			allRecipes.add(recipe);
			for (SizedIngredient sizedIngredient : recipe.ingredients())
			{
				sizedIngredient.ingredient().getValues().forEach(itemHolder -> {
					results.computeIfAbsent(itemHolder.value(), x -> new ArrayList<>())
					.add(recipe);
				});
			}
		}
		this.cachedSortedRecipes = results;
		this.allRecipes = allRecipes.stream().toList();
	}
	
	/*
	 * Compares two recipes such that the one that requires more ingredients and more specific ingredients
	 * will be first in a list when sorted
	 */
	public static int compareRecipes(JumboFurnaceRecipe a, JumboFurnaceRecipe b)
	{
		// recipe with higher specificity should be lower when compared, so flip the order
		return b.specificity().get() - a.specificity().get();
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
