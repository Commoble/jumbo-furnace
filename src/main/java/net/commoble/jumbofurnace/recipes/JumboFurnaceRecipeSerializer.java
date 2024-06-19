package net.commoble.jumbofurnace.recipes;

import java.util.ArrayList;
import java.util.List;

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
		for (int slot = 0; slot < ingredientCount; slot++)
		{
			ingredients.set(slot, Ingredient.fromNetwork(buffer));
		}
		
		int resultCount = buffer.readVarInt();
		List<ItemStack> results = new ArrayList<>(resultCount);
		for (int slot=0; slot < resultCount; slot++)
		{
			results.add(buffer.readItem());
		}

        float experience = buffer.readFloat();
        float cookingTime = buffer.readInt();
		return new JumboFurnaceRecipe( groupName, ingredients, results, experience);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, JumboFurnaceRecipe recipe)
	{
        buffer.writeUtf(recipe.group());
        
        buffer.writeVarInt(recipe.ingredients().size());
        for(Ingredient ingredient : recipe.ingredients())
        {
        	ingredient.toNetwork(buffer);
        }
        
        buffer.writeVarInt(recipe.results().size());
        for (ItemStack stack : recipe.results())
        {
        	buffer.writeItem(stack);
        }

        buffer.writeFloat(recipe.experience());
        buffer.writeInt(recipe.cookingTime());
	}

	@Override
	public Codec<JumboFurnaceRecipe> codec()
	{
		return JumboFurnaceRecipe.CODEC;
	}


}
