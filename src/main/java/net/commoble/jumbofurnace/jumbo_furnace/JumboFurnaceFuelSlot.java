package net.commoble.jumbofurnace.jumbo_furnace;

import net.commoble.jumbofurnace.JumboFurnaceUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class JumboFurnaceFuelSlot extends ResourceHandlerSlot
{
	private final FuelValues fuelValues;
	
	public JumboFurnaceFuelSlot(ItemStacksResourceHandler itemHandler, int index, int xPosition, int yPosition, FuelValues fuelValues)
	{
		super(itemHandler, itemHandler::set, index, xPosition, yPosition);
		this.fuelValues = fuelValues;
	}

	@Override
	public boolean mayPlace(ItemStack stack)
	{
		// items ought to be able to provide burn values for jumbo smelting specifically
		// check jumbo smelting burn time first, otherwise use regular furnace smelting
		
		return JumboFurnaceUtils.getJumboSmeltingBurnTime(stack, this.fuelValues) > 0;
	}

	
}
