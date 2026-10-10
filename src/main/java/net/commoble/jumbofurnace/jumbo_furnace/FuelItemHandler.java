package net.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class FuelItemHandler extends ItemStacksResourceHandler
{
	public final JumboFurnaceCoreBlockEntity core;
	
	public FuelItemHandler(JumboFurnaceCoreBlockEntity core)
	{
		super(9);
		this.core = core;
	}

	@Override
	public boolean isValid(int slot, ItemResource resource)
	{		
		return resource.get(DataComponents.COOKING_FUEL) != null;
	}

	@Override
	protected void onContentsChanged(int slot, @SuppressWarnings("null") ItemStack oldStack)
	{
		super.onContentsChanged(slot, oldStack);
		this.core.setChanged();
		this.core.markFuelInventoryChanged();
	}
	
	
}
