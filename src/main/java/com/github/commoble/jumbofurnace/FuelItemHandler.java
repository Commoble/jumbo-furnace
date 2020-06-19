package com.github.commoble.jumbofurnace;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemStackHandler;

public class FuelItemHandler extends ItemStackHandler
{
	public final JumboFurnaceCoreTileEntity te;
	
	public FuelItemHandler(JumboFurnaceCoreTileEntity te)
	{
		super(9);
		this.te = te;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return ForgeHooks.getBurnTime(stack) > 0;
	}

	@Override
	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		this.te.markDirty();
		this.te.onFuelInventoryChanged();
	}
	
	
}
