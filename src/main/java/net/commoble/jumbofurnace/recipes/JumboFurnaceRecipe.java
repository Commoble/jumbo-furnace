package net.commoble.jumbofurnace.recipes;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.jumbofurnace.JumboFurnace;
import net.commoble.jumbofurnace.jumbo_furnace.JumboFurnaceMenu;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record JumboFurnaceRecipe(String group, List<SizedIngredient> ingredients, List<ItemStack> results, float experience, int cookingTime, Supplier<Integer> specificity) implements Recipe<RecipeInput>
{
	public static final MapCodec<JumboFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
			Codec.STRING.optionalFieldOf( "group", "").forGetter(JumboFurnaceRecipe::group),
			SizedIngredient.NESTED_CODEC.listOf()
				.comapFlatMap(JumboFurnaceRecipe::validateIngredients, Function.identity())
				.fieldOf("ingredients").forGetter(JumboFurnaceRecipe::ingredients),
			ItemStack.CODEC.listOf().fieldOf("results")
				.flatXmap(JumboFurnaceRecipe::readResults, JumboFurnaceRecipe::writeResults)
				.forGetter(JumboFurnaceRecipe::results),
			Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(JumboFurnaceRecipe::experience),
			Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(JumboFurnaceRecipe::cookingTime)
		).apply(builder, JumboFurnaceRecipe::new));
	
	public static final StreamCodec<RegistryFriendlyByteBuf, JumboFurnaceRecipe> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, JumboFurnaceRecipe::group,
		SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), JumboFurnaceRecipe::ingredients,
		ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), JumboFurnaceRecipe::results,
		ByteBufCodecs.FLOAT, JumboFurnaceRecipe::experience,
		ByteBufCodecs.INT, JumboFurnaceRecipe::cookingTime,
		JumboFurnaceRecipe::new);
	
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
		this(baseRecipe.group(), List.of(new SizedIngredient(baseRecipe.input(), 1)), List.of(baseRecipe.result), baseRecipe.experience(), baseRecipe.cookingTime());
	}

	@Override
	public boolean matches(RecipeInput inv, Level worldIn)
	{
		// TODO not implemented, we still use our custom matching system
		// might be easier to use the vanilla way now though?
		return false;
//		for (Ingredient ingredient : this.getIngredients())
//		{
//			int amountOfIngredient = ingredient.getItems()[0].getCount();
//			int slots = inv.size();
//			for (int slot=0; slot < slots && amountOfIngredient > 0; slot++)
//			{
//				ItemStack stackInSlot = inv.getItem(slot);
//				if (ingredient.test(stackInSlot) && stackInSlot.getCount() >= amountOfIngredient)
//				{
//					ItemStack usedStack = inv.removeItem(slot, amountOfIngredient);
//					amountOfIngredient -= usedStack.getCount();
//				}
//			}
//			
//			// if we didn't fully match the ingredient, return false
//			if (amountOfIngredient > 0)
//			{
//				return false;
//			}
//		}
//		
//		// if we made it this far, all ingredients matched, so return true
//		return true;
	}

	@Override
	public ItemStack assemble(RecipeInput input, Provider registries)
	{
		return this.results.get(0).copy();
	}

	@Override
	public RecipeSerializer<JumboFurnaceRecipe> getSerializer()
	{
		return JumboFurnace.get().jumboSmeltingRecipeSerializer.get();
	}

	@Override
	public RecipeType<JumboFurnaceRecipe> getType()
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
		for (SizedIngredient sizedIngredient : ingredients)
		{
			Ingredient ingredient = sizedIngredient.ingredient();
			@Nullable ICustomIngredient customIngredient = ingredient.getCustomIngredient();
			int matchingItems = customIngredient == null
				? ingredient.getValues().size()
				: (int)customIngredient.items().count();
			if(matchingItems < 1)
			{
				continue;
			}
			
			// this equation gives a value of 1D when matchingitems = 1, and 0D when matchingItems = totalItems
			double matchFactor = (double)(totalItems - matchingItems) / (double)(totalItems - 1);
			
			int ingredientWeight = (int)(100D * sizedIngredient.count() * matchFactor);
			if (customIngredient != null)
			{
				ingredientWeight *= 10;
			}
			specificity += ingredientWeight;
		}
		return specificity;
	}

	@Override
	public PlacementInfo placementInfo()
	{
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory()
	{
		return RecipeBookCategories.FURNACE_MISC;
	}
}
