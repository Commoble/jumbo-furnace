package com.github.commoble.jumbofurnace.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class JumboFurnaceRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<JumboFurnaceRecipe>
{
	public final IRecipeType<JumboFurnaceRecipe> recipeType;
	
	public JumboFurnaceRecipeSerializer(IRecipeType<JumboFurnaceRecipe> recipeType)
	{
		this.recipeType = recipeType;
	}

	@Override
	public JumboFurnaceRecipe read(ResourceLocation recipeId, JsonObject json)
	{
		String groupName = JSONUtils.getString(json, "group", "");
		NonNullList<Ingredient> ingredients = readIngredients(JSONUtils.getJsonArray(json, "ingredients"));
		if (ingredients.isEmpty())
		{
			throw new JsonParseException("No ingredients for jumbo furnace recipe");
		}
//		else if (ingredients.size() > JumboFurnaceContainer.INPUT_SLOTS)
//		{
//			throw new JsonParseException("Too many ingredients for jumbo furnace recipe! the max is " + (JumboFurnaceContainer.INPUT_SLOTS));
//		}
		else
		{
			ItemStack result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
			float experience = JSONUtils.getFloat(json, "experience", 0.0F);
			return new JumboFurnaceRecipe(this.recipeType, recipeId, groupName, ingredients, result, experience);
		}
	}

	public static NonNullList<Ingredient> readIngredients(JsonArray p_199568_0_)
	{
		NonNullList<Ingredient> nonnulllist = NonNullList.create();

		for (int i = 0; i < p_199568_0_.size(); ++i)
		{
			Ingredient ingredient = Ingredient.deserialize(p_199568_0_.get(i));
			if (!ingredient.hasNoMatchingItems())
			{
				nonnulllist.add(ingredient);
			}
		}

		return nonnulllist;
	}

	@Override
	public JumboFurnaceRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
        String groupName = buffer.readString(32767);
        int ingredientCount = buffer.readVarInt();
        NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientCount, Ingredient.EMPTY);

		for (int slot = 0; slot < ingredients.size(); ++slot)
		{
			ingredients.set(slot, Ingredient.read(buffer));
		}

        ItemStack result = buffer.readItemStack();
        float experience = buffer.readFloat();
		return new JumboFurnaceRecipe(this.recipeType, recipeId, groupName, ingredients, result, experience);
	}

	@Override
	public void write(PacketBuffer buffer, JumboFurnaceRecipe recipe)
	{
        buffer.writeString(recipe.group);
        buffer.writeVarInt(recipe.ingredients.size());

        for(Ingredient ingredient : recipe.ingredients) {
           ingredient.write(buffer);
        }

        buffer.writeItemStack(recipe.result);
        buffer.writeFloat(recipe.experience);
	}


}
