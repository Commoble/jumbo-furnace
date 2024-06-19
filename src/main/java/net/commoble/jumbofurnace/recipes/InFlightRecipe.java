package net.commoble.jumbofurnace.recipes;

import java.util.List;

import net.minecraft.world.item.ItemStack;

public class InFlightRecipe
{
	private final JumboFurnaceRecipe recipe;
	private final List<ItemStack> inputs;
	private int progress = 0;
	
	public InFlightRecipe(JumboFurnaceRecipe recipe, List<ItemStack> inputs)
	{
		this.recipe = recipe;
		this.inputs = inputs;
	}
	
	public JumboFurnaceRecipe recipe()
	{
		return this.recipe;
	}
	
	public List<ItemStack> inputs()
	{
		return this.inputs;
	}
	
	public boolean incrementProgress()
	{
		this.progress++;
		return this.progress >= recipe.cookingTime();
	}
}
