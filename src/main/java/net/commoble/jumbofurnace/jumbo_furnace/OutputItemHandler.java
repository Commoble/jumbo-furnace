package net.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class OutputItemHandler extends ItemStackHandler
{
	public static final String EXPERIENCE = "xp";
	
	public final JumboFurnaceCoreBlockEntity te;
	public boolean forcingInserts = false;
	public float storedExperience = 0F;
	
	public OutputItemHandler(JumboFurnaceCoreBlockEntity te)
	{
		super(JumboFurnaceMenu.INPUT_SLOTS);
		this.te = te;
	}
	
	public void addExperience(float experience)
	{
		this.storedExperience += experience;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return this.forcingInserts;
	}
	
	public ItemStack insertCraftResult(ItemStack stack, boolean simulate)
	{
		this.forcingInserts = true;
		ItemStack result = ItemHandlerHelper.insertItemStacked(this, stack, simulate);
		this.forcingInserts = false;
		return result;
	}

	@Override
	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		this.te.setChanged();
		this.te.markOutputInventoryChanged();
	}
	
	public float getAndConsumeExperience()
	{
		float amount = this.storedExperience;
		this.storedExperience = 0;
		return amount;
	}

	@Override
	public void serialize(ValueOutput output)
	{
		super.serialize(output);
		output.putFloat(EXPERIENCE, this.storedExperience);
	}

	@Override
	public void deserialize(ValueInput input)
	{
		super.deserialize(input);
		this.storedExperience = input.getFloatOr(EXPERIENCE, 0F);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		ItemStack result = super.extractItem(slot, amount, simulate);
		if (!simulate && !result.isEmpty() && this.getStackInSlot(slot).isEmpty() && !te.backstock.isEmpty())
		{
			ItemStack backstockStack = te.backstock.removeFirst();
			if (!backstockStack.isEmpty())
			{
				this.setStackInSlot(slot, backstockStack);
			}
		}
		return result;
	}
	
	
	
}
