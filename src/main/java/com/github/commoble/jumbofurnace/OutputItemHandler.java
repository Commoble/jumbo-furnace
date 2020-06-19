package com.github.commoble.jumbofurnace;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class OutputItemHandler extends ItemStackHandler
{
	public final JumboFurnaceCoreTileEntity te;
	
	public OutputItemHandler(JumboFurnaceCoreTileEntity te)
	{
		super(9);
		this.te = te;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return false;
	}

	@Override
	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		this.te.markDirty();
	}
	
}
