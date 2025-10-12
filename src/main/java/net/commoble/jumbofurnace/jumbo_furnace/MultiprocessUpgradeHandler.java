package net.commoble.jumbofurnace.jumbo_furnace;

import net.commoble.jumbofurnace.JumboFurnace;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class MultiprocessUpgradeHandler extends ItemStacksResourceHandler
{	
	private final JumboFurnaceCoreBlockEntity te;
	
	public MultiprocessUpgradeHandler(JumboFurnaceCoreBlockEntity te)
	{
		super(1);
		this.te = te;
	}

	@Override
	public boolean isValid(int slot, ItemResource stack)
	{
		return stack.is(JumboFurnace.MULTIPROCESSING_UPGRADE_TAG);
	}

	@Override
	protected void onContentsChanged(int slot, ItemStack oldStack)
	{
		super.onContentsChanged(slot, oldStack);
		this.te.markInputInventoryChanged();
		this.te.setChanged();
	}
	
	// need a handler for the slot as well
	// the base SlotItemHandler respects the isItemValid of the parent,
	// but we only use the above handler of the parent on servers
	public static class MultiprocessUpgradeSlotHandler extends ResourceHandlerSlot
	{

		public MultiprocessUpgradeSlotHandler(ItemStacksResourceHandler itemHandler, int index, int xPosition, int yPosition)
		{
			super(itemHandler, itemHandler::set, index, xPosition, yPosition);
		}

		@Override
		public boolean mayPlace(ItemStack stack)
		{
			return stack.is(JumboFurnace.MULTIPROCESSING_UPGRADE_TAG);
		}
	}
}
