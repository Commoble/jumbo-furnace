package com.github.commoble.jumbofurnace.recipes;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class ClaimableRecipeWrapper extends RecipeWrapper
{
	public static final IntSet ALL_SLOTS = new IntOpenHashSet(new int[]{0,1,2,3,4,5,6,7,8});
	
	public final IntSet unclaimedSlots;

	public ClaimableRecipeWrapper(IItemHandlerModifiable inv)
	{
		this(inv, ALL_SLOTS);
	}
	
	public ClaimableRecipeWrapper(IItemHandlerModifiable inv, IntSet unclaimedSlots)
	{
		super(inv);
		this.unclaimedSlots = unclaimedSlots;
	}
	
	public ClaimableRecipeWrapper withoutSlots(IntSet claimedSlots)
	{
		IntSet newSlots = new IntOpenHashSet();
		for (int slot : this.unclaimedSlots)
		{
			if (!claimedSlots.contains(slot))
			{
				newSlots.add(slot);
			}
		}
		
		return new ClaimableRecipeWrapper(this.inv, newSlots);
	}

	
}
