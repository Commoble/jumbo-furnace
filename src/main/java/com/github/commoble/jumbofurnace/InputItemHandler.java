package com.github.commoble.jumbofurnace;

import com.github.commoble.jumbofurnace.recipes.ClaimableRecipeWrapper;

import net.minecraftforge.items.ItemStackHandler;

public class InputItemHandler extends ItemStackHandler
{
	public final JumboFurnaceCoreTileEntity te;
	
	public InputItemHandler(JumboFurnaceCoreTileEntity te)
	{
		super(9);
		this.te = te;
	}
	
	public ClaimableRecipeWrapper getFreshRecipeInput()
	{
		return new ClaimableRecipeWrapper(this);
	}

	@Override
	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		this.te.markDirty();
		this.te.onInputUpdated();
	}
}
