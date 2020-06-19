package com.github.commoble.jumbofurnace.recipes;

import com.google.gson.JsonObject;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class JumboFurnaceRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<JumboFurnaceRecipe>
{

	@Override
	public JumboFurnaceRecipe read(ResourceLocation recipeId, JsonObject json)
	{
		return null;
	}

	@Override
	public JumboFurnaceRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
	{
		return null;
	}

	@Override
	public void write(PacketBuffer buffer, JumboFurnaceRecipe recipe)
	{
	}


}
