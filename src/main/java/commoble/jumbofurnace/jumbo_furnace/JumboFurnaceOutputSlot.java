package com.github.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class JumboFurnaceOutputSlot extends SlotItemHandler
{
	private int removeCount = 0;
	private final PlayerEntity player;

	public JumboFurnaceOutputSlot(PlayerEntity player, IItemHandler itemHandler, int index, int xPosition, int yPosition)
	{
		super(itemHandler, index, xPosition, yPosition);
		this.player = player;
	}

	@Override
	public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack)
	{
		this.onCrafting(stack);
		super.onTake(thePlayer, stack);
		return stack;
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not
	 * ore and wood. Typically increases an internal count then calls
	 * onCrafting(item).
	 */
	@Override
	protected void onCrafting(ItemStack stack, int amount)
	{
		this.removeCount += amount;
		this.onCrafting(stack);
	}

	/**
	 * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not
	 * ore and wood.
	 */
	@Override
	protected void onCrafting(ItemStack stack)
	{
		stack.onCrafting(this.player.world, this.player, this.removeCount);
		this.removeCount = 0;
		
		if (!this.player.world.isRemote && this.getItemHandler() instanceof OutputItemHandler)
		{
			spawnExpOrbs(this.player, ((OutputItemHandler)this.getItemHandler()).getAndConsumeExperience(this.getSlotIndex()));
		}
		
		net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerSmeltedEvent(this.player, stack);
	}

	public static void spawnExpOrbs(PlayerEntity player, float experience)
	{
		int orbs = MathHelper.floor(experience);
		if (orbs < MathHelper.ceil(experience) && Math.random() < experience - orbs)
		{
			++orbs;
		}

		while (orbs > 0)
		{
			int amount = ExperienceOrbEntity.getXPSplit(orbs);
			orbs -= amount;
			player.world.addEntity(new ExperienceOrbEntity(player.world, player.getPosX(), player.getPosY() + 0.5D, player.getPosZ() + 0.5D, amount));
		}

	}
}
