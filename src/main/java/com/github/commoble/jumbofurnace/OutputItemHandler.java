package com.github.commoble.jumbofurnace;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

public class OutputItemHandler extends ItemStackHandler
{
	public static final String EXPERIENCE = "xp";
	
	public final JumboFurnaceCoreTileEntity te;
	public boolean forcingInserts = false;
	public final float[] storedExperience;
	
	public OutputItemHandler(JumboFurnaceCoreTileEntity te)
	{
		super(JumboFurnaceContainer.INPUT_SLOTS);
		this.te = te;
		this.storedExperience = new float[9];
	}
	
	public void addExperience(int slot, float experience)
	{
		this.storedExperience[slot] += experience;
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
		this.te.markDirty();
		this.te.onOutputInventoryChanged();
	}
	
	public float getAndConsumeExperience(int index)
	{
		float amount = this.storedExperience[index];
		this.storedExperience[index] = 0;
		return amount;
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT result = super.serializeNBT();
		ListNBT list = new ListNBT();
		for (float experience : this.storedExperience)
		{
			list.add(FloatNBT.valueOf(experience));
		}
		result.put(EXPERIENCE, list);
		return result;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		super.deserializeNBT(nbt);
		ListNBT list = nbt.getList(EXPERIENCE, Constants.NBT.TAG_FLOAT);
		int listSize = list.size();
		int slotCount = this.storedExperience.length;
		int slotsToRead = Math.min(listSize, slotCount);
		for (int i=0; i<slotsToRead; i++)
		{
			this.storedExperience[i] = list.getFloat(i);
		}
	}
	
}
