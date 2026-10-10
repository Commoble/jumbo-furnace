package net.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class JumboFurnaceFuelSlot extends ResourceHandlerSlot
{	
	public JumboFurnaceFuelSlot(ItemStacksResourceHandler itemHandler, int index, int xPosition, int yPosition)
	{
		super(itemHandler, itemHandler::set, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack)
	{
		// items ought to be able to provide burn values for jumbo smelting specifically
		// check jumbo smelting burn time first, otherwise use regular furnace smelting
		
		return stack.has(DataComponents.COOKING_FUEL);
	}

	
}
