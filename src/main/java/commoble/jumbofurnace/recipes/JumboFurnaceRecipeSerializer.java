package commoble.jumbofurnace.recipes;

import com.mojang.serialization.Codec;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class JumboFurnaceRecipeSerializer implements RecipeSerializer<JumboFurnaceRecipe>
{
	public final RecipeType<JumboFurnaceRecipe> recipeType;
	
	public JumboFurnaceRecipeSerializer(RecipeType<JumboFurnaceRecipe> recipeType)
	{
		this.recipeType = recipeType;
	}

	@Override
	public JumboFurnaceRecipe fromNetwork(FriendlyByteBuf buffer)
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
		return new JumboFurnaceRecipe( groupName, ingredients, result, experience);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, JumboFurnaceRecipe recipe)
	{
        buffer.writeUtf(recipe.group());
        buffer.writeVarInt(recipe.ingredients().size());

        for(Ingredient ingredient : recipe.ingredients()) {
           ingredient.toNetwork(buffer);
        }

        buffer.writeItem(recipe.result());
        buffer.writeFloat(recipe.experience());
	}

	@Override
	public Codec<JumboFurnaceRecipe> codec()
	{
		return JumboFurnaceRecipe.CODEC;
	}


}
