package net.commoble.jumbofurnace.recipes;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.jumbofurnace.JumboFurnace;
import net.commoble.jumbofurnace.jumbo_furnace.JumboFurnaceMenu;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record JumboFurnaceRecipe(String group, List<SizedIngredient> ingredients, List<ItemStack> results, float experience, int cookingTime, Supplier<Integer> specificity) implements Recipe<Container>
{
	public static final Codec<JumboFurnaceRecipe> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			Codec.STRING.optionalFieldOf( "group", "").forGetter(JumboFurnaceRecipe::group),
			SizedIngredient.FLAT_CODEC.listOf()
				.comapFlatMap(JumboFurnaceRecipe::validateIngredients, Function.identity())
				.fieldOf("ingredients").forGetter(JumboFurnaceRecipe::ingredients),
			ItemStack.CODEC.listOf().fieldOf("results")
				.flatXmap(JumboFurnaceRecipe::readResults, JumboFurnaceRecipe::writeResults)
				.forGetter(JumboFurnaceRecipe::results),
			Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(JumboFurnaceRecipe::experience),
			Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(JumboFurnaceRecipe::cookingTime)
		).apply(builder, JumboFurnaceRecipe::new));
	
	public JumboFurnaceRecipe(String group, List<SizedIngredient> ingredients, List<ItemStack> results, float experience, int cookingTime)
	{
		this(group, ingredients, results, experience, cookingTime, Suppliers.memoize(() -> getSpecificity(ingredients, experience)));
	}
	
	public static DataResult<List<SizedIngredient>> validateIngredients(List<SizedIngredient> ingredients)
	{
		int size = ingredients.size();
		if (size < 1)
		{
			return DataResult.error(() -> "No ingredients for jumbo smelting recipe");
		}
		if (size > JumboFurnaceMenu.INPUT_SLOTS)
		{
			return DataResult.error(() -> "Too many ingredients for jumbo smelting recipe! the max is " + (JumboFurnaceMenu.INPUT_SLOTS));
		}
		return DataResult.success(ingredients);
	}
	
	public static DataResult<List<ItemStack>> readResults(List<ItemStack> list)
	{
		return list.isEmpty()
			? DataResult.error(() -> "Empty result list for jumbo smelting recipe")
			: DataResult.success(list);
	}
	
	public static DataResult<List<ItemStack>> writeResults(List<ItemStack> results)
	{
		return results.isEmpty() ? DataResult.error(() -> "Empty result list for jumbo smelting recipe")
			: DataResult.success(results);
			
	}
	
	/** Wrapper around regular furnace recipes to make single-input jumbo furnace recipes **/
	public JumboFurnaceRecipe(SmeltingRecipe baseRecipe)
	{
		this(baseRecipe.getGroup(), NonNullList.copyOf(baseRecipe.getIngredients().stream().map(ing -> new SizedIngredient(ing, 1)).toList()), List.of(baseRecipe.result.copy()), baseRecipe.getExperience(), baseRecipe.getCookingTime());
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients()
	{
		return NonNullList.copyOf(this.ingredients().stream().map(SizedIngredient::ingredient).toList());
	}

	@Override
	public boolean matches(Container inv, Level worldIn)
	{
		for (Ingredient ingredient : this.getIngredients())
		{
			int amountOfIngredient = ingredient.getItems()[0].getCount();
			int slots = inv.getContainerSize();
			for (int slot=0; slot < slots && amountOfIngredient > 0; slot++)
			{
				ItemStack stackInSlot = inv.getItem(slot);
				if (ingredient.test(stackInSlot) && stackInSlot.getCount() >= amountOfIngredient)
				{
					ItemStack usedStack = inv.removeItem(slot, amountOfIngredient);
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
	public boolean canCraftInDimensions(int width, int height)
	{
		return true;
	}

	@Override
	public ItemStack assemble(Container p_44001_, Provider p_336092_)
	{
		return this.results.get(0).copy();
	}

	@Override
	public ItemStack getResultItem(Provider p_336125_)
	{
		return this.results.get(0).copy();
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return JumboFurnace.get().jumboSmeltingRecipeSerializer.get();
	}

	@Override
	public RecipeType<?> getType()
	{
		return JumboFurnace.get().jumboSmeltingRecipeType.get();
	}
	
	@Override
	public boolean isSpecial()
	{
		return true;
	}
	
	public static int getSpecificity(List<SizedIngredient> ingredients, float experience)
	{
		int specificity = (int)(experience*10);
		int totalItems = BuiltInRegistries.ITEM.size();
		for (SizedIngredient ingredient : ingredients)
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
	public NonNullList<ItemStack> getRemainingItems(Container inv)
	{
		// nope, we use a different system for remainder items
		return NonNullList.create();
	}

}
