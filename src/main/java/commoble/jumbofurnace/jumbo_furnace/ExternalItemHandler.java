package commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ExternalItemHandler implements IItemHandler
{

	@Override
	public int getSlots()
	{
		return 0;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return null;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		return null;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return null;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return false;
	}

}
