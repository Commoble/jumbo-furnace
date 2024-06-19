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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class RecipeSorter extends SimplePreparableReloadListener<Void>
{
	public static final RecipeSorter INSTANCE = new RecipeSorter();
	
	// keep track of when recipes have reloaded
	private int currentGeneration = 0;
	private int lastKnownGeneration = -1;
	private Map<Item, List<JumboFurnaceRecipe>> cachedSortedRecipes = new Reference2ObjectOpenHashMap<>();
	
	public SortedSet<JumboFurnaceRecipe> getSortedFurnaceRecipes(Collection<Item> inputItems, RecipeManager manager)
	{
		if (this.currentGeneration != this.lastKnownGeneration)
		{
			this.cachedSortedRecipes = this.sortFurnaceRecipes(manager);
			this.lastKnownGeneration = this.currentGeneration;
		}
		
		SortedSet<JumboFurnaceRecipe> recipesForItems = new ObjectRBTreeSet<>(RecipeSorter::compareRecipes);
		for (Item item : inputItems)
		{
			var recipesForItem = this.cachedSortedRecipes.get(item);
			if (recipesForItem != null)
			{
				recipesForItems.addAll(recipesForItems);
			}
		}
		
		return recipesForItems;
	}
	
	private Map<Item, List<JumboFurnaceRecipe>> sortFurnaceRecipes(RecipeManager manager)
	{
		Map<Item, List<JumboFurnaceRecipe>> results = new Reference2ObjectOpenHashMap<>();
		// we need to track the wrapped recipes so we don't put two copies of them in the results map
		Map<ResourceLocation, JumboFurnaceRecipe> wrappedRecipes = new HashMap<>();
		for (var holder : manager.getAllRecipesFor(RecipeType.SMELTING))
		{
			ResourceLocation key = holder.id();
			SmeltingRecipe recipe = holder.value();
			for (Ingredient ingredient : recipe.getIngredients())
			{
				for (ItemStack stack : ingredient.getItems())
				{
					results.computeIfAbsent(stack.getItem(), x -> new ArrayList<>())
						.add(wrappedRecipes.computeIfAbsent(key, x -> new JumboFurnaceRecipe(recipe)));
				}
			}
		}
		for (var holder : manager.getAllRecipesFor(JumboFurnace.get().jumboSmeltingRecipeType.get()))
		{
			JumboFurnaceRecipe recipe = holder.value();
			for (Ingredient ingredient : recipe.getIngredients())
			{
				for (ItemStack stack : ingredient.getItems())
				{
					results.computeIfAbsent(stack.getItem(), x -> new ArrayList<>())
						.add(recipe);
				}
			}
		}
		return results;
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
