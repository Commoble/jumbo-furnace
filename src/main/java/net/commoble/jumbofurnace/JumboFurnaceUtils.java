package net.commoble.jumbofurnace;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class JumboFurnaceUtils
{
	/** 
	 * @param stack Itemstack to be used as fuel for a jumbo furnace.
	 * @return If the itemstack provides a burn time specific to jumbo smelting, returns that.
	 * Otherwise, returns the burn time for vanilla furnace smelting.
	 */
	public static int getJumboSmeltingBurnTime(ItemStack stack, FuelValues fuelValues)
	{
		int jumboSmeltingBurnTime = stack.getBurnTime(JumboFurnace.get().jumboSmeltingRecipeType.get(), fuelValues);
		return jumboSmeltingBurnTime >= 0
			? jumboSmeltingBurnTime
			: stack.getBurnTime(RecipeType.SMELTING, fuelValues);
	}
	
	public static ItemStacksResourceHandler copyItemHandler(ItemStacksResourceHandler itemHandler)
	{
		int slots = itemHandler.size();
		ItemStacksResourceHandler copy = new ItemStacksResourceHandler(slots);
		for (int i=0; i<slots; i++)
		{
			ItemStack stackInSlot = ItemUtil.getStack(itemHandler, i);
			copy.set(i, ItemResource.of(stackInSlot), stackInSlot.getCount());
		}
		return copy;
	}
	
	public static void copyItemHandlerTo(ItemStacksResourceHandler from, ItemStacksResourceHandler to)
	{
		int slots = to.size();
		int fromSlots = from.size();
		if (fromSlots < slots)
		{
			slots = fromSlots;
		}
		for (int i=0; i<slots; i++)
		{
			ItemStack stackInSlot = ItemUtil.getStack(from, i);
			to.set(i, ItemResource.of(stackInSlot), stackInSlot.getCount());
		}
	}
	
	public static ItemStack insertItemStacked(ItemStacksResourceHandler handler, ItemStack stack, @Nullable TransactionContext context)
	{
		int oldCount = stack.getCount();
		int inserted = ResourceHandlerUtil.insertStacking(handler, ItemResource.of(stack), oldCount, context);
		return stack.copyWithCount(oldCount - inserted);
	}
	
	public static ItemStack extract(int slot, ResourceHandler<ItemResource> handler, int amount, TransactionContext context)
	{
		ItemResource resource = handler.getResource(slot);
		if (resource.isEmpty())
			return ItemStack.EMPTY;
		int extracted = handler.extract(slot, resource, amount, context);
		return resource.toStack(extracted);
	}
	
	public static ItemStack extractImmediate(int slot, ResourceHandler<ItemResource> handler, int amount)
	{
		try (Transaction t = Transaction.open(null))
		{
			ItemStack stack = extract(slot, handler, amount, t);
			t.commit();
			return stack;
		}
	}
	
	public static ItemStack insertImmediate(int slot, ItemStacksResourceHandler handler, ItemStack stack)
	{
		int oldCount = stack.getCount();
		try (Transaction t = Transaction.open(null))
		{
			int inserted = handler.insert(slot, ItemResource.of(stack), oldCount, t);
			t.commit();
			return stack.copyWithCount(oldCount - inserted);
		}
	}
	
	public static boolean canBeJumboFurnaceInput(Item item, Level level)
	{
		return !RecipeSorter.INSTANCE.getSortedFurnaceRecipesValidForInputs(List.of(item), level.getRecipeManager()).isEmpty();
	}
}
