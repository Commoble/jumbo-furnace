package net.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class InputItemHandler extends ItemStacksResourceHandler
{
	public final JumboFurnaceCoreBlockEntity te;
	
	public InputItemHandler(JumboFurnaceCoreBlockEntity te)
	{
		super(9);
		this.te = te;
	}

	@Override
	protected void onContentsChanged(int slot, ItemStack oldStack)
	{
		super.onContentsChanged(slot, oldStack);
		this.te.setChanged();
		this.te.markInputInventoryChanged();
	}
}
