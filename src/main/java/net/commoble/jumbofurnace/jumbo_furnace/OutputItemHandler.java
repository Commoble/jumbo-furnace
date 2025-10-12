package net.commoble.jumbofurnace.jumbo_furnace;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.serialization.Codec;

import net.commoble.jumbofurnace.JumboFurnaceUtils;
import net.commoble.jumbofurnace.SnapshotStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class OutputItemHandler extends ItemStacksResourceHandler
{
	public static final String EXPERIENCE = "xp";
	public static final String BACKSTOCK = "backstock";
	public static final Codec<List<ItemStack>> BACKSTOCK_CODEC = ItemStack.CODEC.listOf()
		.xmap(ArrayList::new, Function.identity());
	
	public final JumboFurnaceCoreBlockEntity te;
	public boolean forcingInserts = false;
	public float storedExperience = 0F;
	public SnapshotStack<List<ItemStack>> backstock = SnapshotStack.of(new ArrayList<>(), ArrayList::new);
	
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
	public boolean isValid(int slot, ItemResource resource)
	{
		return this.forcingInserts;
	}
	
	public ItemStack insertCraftResult(ItemStack stack)
	{
		this.forcingInserts = true;
		ItemStack result = JumboFurnaceUtils.insertItemStacked(this, stack, null);
		this.forcingInserts = false;
		return result;
	}

	@Override
	protected void onContentsChanged(int slot, ItemStack oldStack)
	{
		super.onContentsChanged(slot, oldStack);
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
		output.store(BACKSTOCK, BACKSTOCK_CODEC, this.backstock.get());
	}

	@Override
	public void deserialize(ValueInput input)
	{
		super.deserialize(input);
		this.storedExperience = input.getFloatOr(EXPERIENCE, 0F);
		this.backstock.set(input.read(BACKSTOCK, BACKSTOCK_CODEC).orElseGet(ArrayList::new));
	}

	@Override
	public int extract(int slot, ItemResource resource, int amount, TransactionContext context)
	{
		int extracted = super.extract(slot, resource, amount, context);
		if (extracted > 0 && this.getAmountAsInt(slot) == 0 && !this.backstock.get().isEmpty())
		{
			ItemStack backstockStack = this.backstock.applyAndTakeSnapshot(List::removeFirst, context);
			if (!backstockStack.isEmpty())
			{
				this.set(slot, ItemResource.of(backstockStack), backstockStack.getCount());
			}
		}
		return extracted;
	}
}
