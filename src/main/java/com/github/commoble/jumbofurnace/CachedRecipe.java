package com.github.commoble.jumbofurnace;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.item.ItemStack;

/** Represents a set of items in the jumbo furnace's input slot and the recipe they are going to be used for **/
public class CachedRecipe
{
	/** A set of the slot indices in the furnace's input inventory that the recipe will use **/
	public final IntSet slots;
	/** The item that the recipe results in when smelting concludes **/
	public final ItemStack output;
	
	public CachedRecipe(IntSet slots, ItemStack output)
	{
		this.slots = slots;
		this.output = output;
	}
}
