package commoble.jumbofurnace.jumbo_furnace;

import commoble.jumbofurnace.JumboFurnace;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MultiprocessUpgradeHandler extends ItemStackHandler
{	
	private final JumboFurnaceCoreTileEntity te;
	
	public MultiprocessUpgradeHandler(JumboFurnaceCoreTileEntity te)
	{
		super(1);
		this.te = te;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return JumboFurnace.MULTIPROCESSING_UPGRADE_TAG.contains(stack.getItem());
	}

	@Override
	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		this.te.onInputInventoryChanged();
		this.te.markDirty();
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
		public boolean isItemValid(ItemStack stack)
		{
			return JumboFurnace.MULTIPROCESSING_UPGRADE_TAG.contains(stack.getItem());
		}
	}
}
