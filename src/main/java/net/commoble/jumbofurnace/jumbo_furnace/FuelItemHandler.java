package net.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FuelItemHandler extends ItemStackHandler
{
	public final JumboFurnaceCoreBlockEntity te;
	
	public FuelItemHandler(JumboFurnaceCoreBlockEntity te)
	{
		super(9);
		this.te = te;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return CommonHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
	}

	@Override
	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		this.te.setChanged();
		this.te.markFuelInventoryChanged();
	}
	
	
}
