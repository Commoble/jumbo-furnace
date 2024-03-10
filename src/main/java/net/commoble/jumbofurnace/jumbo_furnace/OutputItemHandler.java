package net.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class OutputItemHandler extends ItemStackHandler
{
	public static final String EXPERIENCE = "xp";
	
	public final JumboFurnaceCoreBlockEntity te;
	public boolean forcingInserts = false;
	public final float[] storedExperience;
	
	public OutputItemHandler(JumboFurnaceCoreBlockEntity te)
	{
		super(JumboFurnaceMenu.INPUT_SLOTS);
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
		this.te.setChanged();
		this.te.onOutputInventoryChanged();
	}
	
	public float getAndConsumeExperience(int index)
	{
		float amount = this.storedExperience[index];
		this.storedExperience[index] = 0;
		return amount;
	}

	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag result = super.serializeNBT();
		ListTag list = new ListTag();
		for (float experience : this.storedExperience)
		{
			list.add(FloatTag.valueOf(experience));
		}
		result.put(EXPERIENCE, list);
		return result;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		super.deserializeNBT(nbt);
		ListTag list = nbt.getList(EXPERIENCE, Tag.TAG_FLOAT);
		int listSize = list.size();
		int slotCount = this.storedExperience.length;
		int slotsToRead = Math.min(listSize, slotCount);
		for (int i=0; i<slotsToRead; i++)
		{
			this.storedExperience[i] = list.getFloat(i);
		}
	}
	
}
