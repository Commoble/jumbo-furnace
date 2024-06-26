package net.commoble.jumbofurnace.jumbo_furnace;

import net.neoforged.neoforge.items.ItemStackHandler;

public class InputItemHandler extends ItemStackHandler
{
	public final JumboFurnaceCoreBlockEntity te;
	
	public InputItemHandler(JumboFurnaceCoreBlockEntity te)
	{
		super(9);
		this.te = te;
	}

	@Override
	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		this.te.setChanged();
		this.te.markInputInventoryChanged();
	}
}
