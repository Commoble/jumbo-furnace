package net.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

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
