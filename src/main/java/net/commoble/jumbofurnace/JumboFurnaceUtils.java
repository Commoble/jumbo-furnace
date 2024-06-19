package net.commoble.jumbofurnace;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

public class JumboFurnaceUtils
{
	/** 
	 * @param stack Itemstack to be used as fuel for a jumbo furnace.
	 * @return If the itemstack provides a burn time specific to jumbo smelting, returns that.
	 * Otherwise, returns the burn time for vanilla furnace smelting.
	 */
	public static int getJumboSmeltingBurnTime(ItemStack stack)
	{
		int jumboSmeltingBurnTime = stack.getBurnTime(JumboFurnace.get().jumboSmeltingRecipeType.get());
		return jumboSmeltingBurnTime >= 0
			? jumboSmeltingBurnTime
			: stack.getBurnTime(RecipeType.SMELTING);
	}
	
	public static IItemHandler copyItemHandler(IItemHandler itemHandler)
	{
		int slots = itemHandler.getSlots();
		ItemStackHandler copy = new ItemStackHandler(slots);
		for (int i=0; i<slots; i++)
		{
			copy.setStackInSlot(i, itemHandler.getStackInSlot(i).copy());
		}
		return copy;
	}
	
	public static void copyItemHandlerTo(IItemHandler from, IItemHandlerModifiable to)
	{
		int slots = to.getSlots();
		int fromSlots = from.getSlots();
		if (fromSlots < slots)
		{
			slots = fromSlots;
		}
		for (int i=0; i<slots; i++)
		{
			to.setStackInSlot(i, from.getStackInSlot(i));
		}
	}
}
