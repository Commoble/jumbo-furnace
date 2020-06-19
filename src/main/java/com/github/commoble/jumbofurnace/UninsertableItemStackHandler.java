package com.github.commoble.jumbofurnace;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class UninsertableItemStackHandler extends ItemStackHandler
{

	public UninsertableItemStackHandler(int slots)
	{
		super(slots);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return false;
	}
}
