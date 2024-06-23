package net.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
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
	
	public ItemStack insertCraftResult(int slot, ItemStack stack, boolean simulate)
	{
		this.forcingInserts = true;
		ItemStack result = this.insertItem(slot, stack, simulate);
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
	public CompoundTag serializeNBT(HolderLookup.Provider registries)
	{
		CompoundTag result = super.serializeNBT(registries);
		result.putFloat(EXPERIENCE, this.storedExperience);
		return result;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt)
	{
		super.deserializeNBT(registries, nbt);
		this.storedExperience = nbt.getFloat(EXPERIENCE);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		ItemStack result = super.extractItem(slot, amount, simulate);
		if (!simulate && !result.isEmpty() && this.getStackInSlot(slot).isEmpty())
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
