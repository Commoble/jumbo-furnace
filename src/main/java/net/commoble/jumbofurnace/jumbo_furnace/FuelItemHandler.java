package net.commoble.jumbofurnace.jumbo_furnace;

import org.jspecify.annotations.Nullable;

import net.commoble.jumbofurnace.JumboFurnaceUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class FuelItemHandler extends ItemStacksResourceHandler
{
	public final JumboFurnaceCoreBlockEntity core;
	
	public FuelItemHandler(JumboFurnaceCoreBlockEntity core)
	{
		super(9);
		this.core = core;
	}

	@Override
	public boolean isValid(int slot, ItemResource resource)
	{
		@Nullable Level level = core.getLevel();
		if (level == null)
			return false;
		
		return JumboFurnaceUtils.getJumboSmeltingBurnTime(resource.toStack(), level.fuelValues()) > 0;
	}

	@Override
	protected void onContentsChanged(int slot, @SuppressWarnings("null") ItemStack oldStack)
	{
		super.onContentsChanged(slot, oldStack);
		this.core.setChanged();
		this.core.markFuelInventoryChanged();
	}
	
	
}
