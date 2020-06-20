package com.github.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class JumboFurnaceFuelSlot extends SlotItemHandler
{

	public JumboFurnaceFuelSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition)
	{
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return ForgeHooks.getBurnTime(stack) > 0;
	}

	
}
