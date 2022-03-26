package commoble.jumbofurnace.jumbo_furnace;

import commoble.jumbofurnace.JumboFurnaceUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class JumboFurnaceFuelSlot extends SlotItemHandler
{

	public JumboFurnaceFuelSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition)
	{
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack)
	{
		// items ought to be able to provide burn values for jumbo smelting specifically
		// check jumbo smelting burn time first, otherwise use regular furnace smelting
		
		return JumboFurnaceUtils.getJumboSmeltingBurnTime(stack) > 0;
	}

	
}
