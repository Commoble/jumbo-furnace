package net.commoble.jumbofurnace.jumbo_furnace;

import net.commoble.jumbofurnace.JumboFurnaceUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class FuelItemHandler extends ItemStacksResourceHandler
{
	public final JumboFurnaceCoreBlockEntity te;
	
	public FuelItemHandler(JumboFurnaceCoreBlockEntity te)
	{
		super(9);
		this.te = te;
	}

	@Override
	public boolean isValid(int slot, ItemResource resource)
	{
		return JumboFurnaceUtils.getJumboSmeltingBurnTime(resource.toStack(), te.getLevel().fuelValues()) > 0;
	}

	@Override
	protected void onContentsChanged(int slot, ItemStack oldStack)
	{
		super.onContentsChanged(slot, oldStack);
		this.te.setChanged();
		this.te.markFuelInventoryChanged();
	}
	
	
}
