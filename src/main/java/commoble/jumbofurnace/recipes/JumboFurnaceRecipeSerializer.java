package commoble.jumbofurnace.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceMenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class JumboFurnaceRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<JumboFurnaceRecipe>
{
	public final RecipeType<JumboFurnaceRecipe> recipeType;
	
	public JumboFurnaceRecipeSerializer(RecipeType<JumboFurnaceRecipe> recipeType)
	{
		this.recipeType = recipeType;
	}

	@Override
	public JumboFurnaceRecipe fromJson(ResourceLocation recipeId, JsonObject json)
	{
		String groupName = GsonHelper.getAsString(json, "group", "");
		NonNullList<Ingredient> ingredients = readIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
		if (ingredients.isEmpty())
		{
			throw new JsonParseException("No ingredients for jumbo furnace recipe");
		}
		else if (ingredients.size() > JumboFurnaceMenuType.INPUT_SLOTS)
		{
			throw new JsonParseException("Too many ingredients for jumbo furnace recipe! the max is " + (JumboFurnaceMenuType.INPUT_SLOTS));
		}
		else
		{
			ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
			float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
			return new JumboFurnaceRecipe(this.recipeType, recipeId, groupName, ingredients, result, experience);
		}
	}

	public static NonNullList<Ingredient> readIngredients(JsonArray p_199568_0_)
	{
		NonNullList<Ingredient> nonnulllist = NonNullList.create();

		for (int i = 0; i < p_199568_0_.size(); ++i)
		{
			Ingredient ingredient = Ingredient.fromJson(p_199568_0_.get(i));
			if (!ingredient.isEmpty())
			{
				nonnulllist.add(ingredient);
			}
		}

		return nonnulllist;
	}

	@Override
	public JumboFurnaceRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
        String groupName = buffer.readUtf(32767);
        int ingredientCount = buffer.readVarInt();
        NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientCount, Ingredient.EMPTY);

		for (int slot = 0; slot < ingredients.size(); ++slot)
		{
			ingredients.set(slot, Ingredient.fromNetwork(buffer));
		}

        ItemStack result = buffer.readItem();
        float experience = buffer.readFloat();
		return new JumboFurnaceRecipe(this.recipeType, recipeId, groupName, ingredients, result, experience);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, JumboFurnaceRecipe recipe)
	{
        buffer.writeUtf(recipe.group);
        buffer.writeVarInt(recipe.ingredients.size());

        for(Ingredient ingredient : recipe.ingredients) {
           ingredient.toNetwork(buffer);
        }

        buffer.writeItem(recipe.result);
        buffer.writeFloat(recipe.experience);
	}


}
