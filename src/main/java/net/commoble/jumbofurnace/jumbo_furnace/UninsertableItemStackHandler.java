package net.commoble.jumbofurnace.jumbo_furnace;

import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class UninsertableItemStackHandler extends ItemStacksResourceHandler
{

	public UninsertableItemStackHandler(int slots)
	{
		super(slots);
	}

	@Override
	public boolean isValid(int slot, ItemResource resource)
	{
		return false;
	}
}
