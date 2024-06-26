package net.commoble.jumbofurnace.jumbo_furnace;

import net.commoble.jumbofurnace.JumboFurnace;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class MultiprocessUpgradeHandler extends ItemStackHandler
{	
	private final JumboFurnaceCoreBlockEntity te;
	
	public MultiprocessUpgradeHandler(JumboFurnaceCoreBlockEntity te)
	{
		super(1);
		this.te = te;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return stack.is(JumboFurnace.MULTIPROCESSING_UPGRADE_TAG);
	}

	@Override
	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		this.te.markInputInventoryChanged();
		this.te.setChanged();
	}
	
	// need a handler for the slot as well
	// the base SlotItemHandler respects the isItemValid of the parent,
	// but we only use the above handler of the parent on servers
	public static class MultiprocessUpgradeSlotHandler extends SlotItemHandler
	{

		public MultiprocessUpgradeSlotHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition)
		{
			super(itemHandler, index, xPosition, yPosition);
		}

		@Override
		public boolean mayPlace(ItemStack stack)
		{
			return stack.is(JumboFurnace.MULTIPROCESSING_UPGRADE_TAG);
		}
	}
}
