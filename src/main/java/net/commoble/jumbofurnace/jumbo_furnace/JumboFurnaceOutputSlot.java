package net.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class JumboFurnaceOutputSlot extends SlotItemHandler
{
	private int removeCount = 0;
	private final Player player;

	public JumboFurnaceOutputSlot(Player player, IItemHandler itemHandler, int index, int xPosition, int yPosition)
	{
		super(itemHandler, index, xPosition, yPosition);
		this.player = player;
	}

	@Override
	public ItemStack remove(int amount)
	{
		// vanilla furnace tracks removecount weirdly, do it the same way in case it's important
		if (this.hasItem())
		{
			this.removeCount = this.removeCount + Math.min(amount, this.getItem().getCount());
		}

		return super.remove(amount);
	}

	@Override
	public void onTake(Player thePlayer, ItemStack stack)
	{
		this.checkTakeAchievements(stack);
		super.onTake(thePlayer, stack);
	}

	// why does SlotItemHandler override this to noop??
	@Override
	public void onQuickCraft(ItemStack modifiedStack, ItemStack originalStack)
	{
		int i = originalStack.getCount() - modifiedStack.getCount();
		if (i > 0)
		{
			this.onQuickCraft(originalStack, i);
		}
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not
	 * ore and wood. Typically increases an internal count then calls
	 * onCrafting(item).
	 */
	@Override
	protected void onQuickCraft(ItemStack stack, int amount)
	{
		this.removeCount += amount;
		this.checkTakeAchievements(stack);
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not
	 * ore and wood.
	 */
	@Override
	protected void checkTakeAchievements(ItemStack stack)
	{
		stack.onCraftedBy(this.player.level(), this.player, this.removeCount);
		this.removeCount = 0;
		
		if (!this.player.level().isClientSide() && this.getItemHandler() instanceof OutputItemHandler outputHandler)
		{
			spawnExpOrbs(this.player, outputHandler.getAndConsumeExperience());
		}
		
		EventHooks.firePlayerSmeltedEvent(this.player, stack);
	}

	public static void spawnExpOrbs(Player player, float experience)
	{
		int orbs = Mth.floor(experience);
		if (orbs < Mth.ceil(experience) && Math.random() < experience - orbs)
		{
			++orbs;
		}

		while (orbs > 0)
		{
			int amount = ExperienceOrb.getExperienceValue(orbs);
			orbs -= amount;
			player.level().addFreshEntity(new ExperienceOrb(player.level(), player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, amount));
		}

	}
}
