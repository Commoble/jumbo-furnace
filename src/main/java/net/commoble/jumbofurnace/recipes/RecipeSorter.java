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
import net.commoble.jumbofurnace.client.ClientProxy;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class RecipeSorter
{
	public static RecipeSorter serverInstance = new RecipeSorter(RecipeMap.EMPTY);
	
	private final RecipeMap recipeMap;
	private RecipeCache cache = null;
	
	public static void onServerReload(RecipeMap recipeMap)
	{
		RecipeSorter theNewOne = new RecipeSorter(recipeMap);
		serverInstance = theNewOne;
	}
	
	public RecipeSorter(RecipeMap recipeMap)
	{
		this.recipeMap = recipeMap;
	}
	
	public RecipeMap recipeMap()
	{
		return this.recipeMap;
	}
	
	private RecipeCache getOrCreateCache()
	{
		RecipeCache cache = this.cache;
		if (cache == null)
		{
			cache = RecipeCache.create(this.recipeMap);
			this.cache = cache;
		}
		return cache;
	}
	
	public static RecipeSorter getSidedRecipes(Level level)
	{
		return level.isClientSide()
			? ClientProxy.recipeSorter
			: serverInstance;
	}
	
	public SortedSet<JumboFurnaceRecipe> getSortedFurnaceRecipesValidForInputs(Collection<Item> inputItems)
	{		
		SortedSet<JumboFurnaceRecipe> recipesForItems = new ObjectRBTreeSet<>(RecipeSorter::compareRecipes);
		var cachedSortedRecipes = this.getOrCreateCache().cachedSortedRecipes;
		for (Item item : inputItems)
		{
			var recipesForItem = cachedSortedRecipes.get(item);
			if (recipesForItem != null)
			{
				recipesForItems.addAll(recipesForItem);
			}
		}
		
		return recipesForItems;
	}
	
	public List<JumboFurnaceRecipe> getAllSortedFurnaceRecipes()
	{
		return this.getOrCreateCache().allRecipes;
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
	
	private static record RecipeCache(Map<Item, List<JumboFurnaceRecipe>> cachedSortedRecipes, List<JumboFurnaceRecipe> allRecipes)
	{
		public static RecipeCache create(RecipeMap recipeMap)
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
			return new RecipeCache(results, allRecipes.stream().toList());
		}
	}
}
