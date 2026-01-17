package net.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

public class JumboFurnaceOutputSlot extends ResourceHandlerSlot
{
	private final Player player;

	public JumboFurnaceOutputSlot(Player player, ItemStacksResourceHandler itemHandler, int index, int xPosition, int yPosition)
	{
		super(itemHandler, itemHandler::set, index, xPosition, yPosition);
		this.player = player;
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
			this.checkTakeAchievements(originalStack, i);
		}
	}
	
	@Override
	protected void checkTakeAchievements(ItemStack stack)
	{
		this.checkTakeAchievements(stack, stack.getCount());
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not
	 * ore and wood.
	 * @param stack ItemStack in slot. Size of stack is not necessarily the amount taken.
	 * @param amount int amount of stack which was actually taken
	 */
	protected void checkTakeAchievements(ItemStack stack, int amount)
	{
		stack.onCraftedBy(this.player, amount);
		
		if (!this.player.level().isClientSide() && this.getResourceHandler() instanceof OutputItemHandler outputHandler)
		{
			spawnExpOrbs(this.player, outputHandler.getAndConsumeExperience());
		}
		
		if (amount > 0)
		{
			EventHooks.firePlayerSmeltedEvent(this.player, stack, amount);	
		}
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
