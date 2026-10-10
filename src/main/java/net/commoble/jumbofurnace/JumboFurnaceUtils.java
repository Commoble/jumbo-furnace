package net.commoble.jumbofurnace;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.commoble.jumbofurnace.recipes.RecipeSorter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
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
	 * @param lootContext LootContext 
	 * @return burn time
	 */
	public static int getJumboSmeltingBurnTime(ItemStack stack, LootContext lootContext)
	{
		int burnDuration = ResolvableInt.getFromItem(stack, DataComponents.COOKING_FUEL, CookingFuel::burnTime, lootContext, 0);
		return burnDuration;
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
		return !RecipeSorter.getSidedRecipes(level).getSortedFurnaceRecipesValidForInputs(List.of(item)).isEmpty();
	}
	
	public static ItemStack itemStackFromTemplate(@Nullable ItemStackTemplate template)
	{
		return template == null ? ItemStack.EMPTY : template.create();
	}
}
