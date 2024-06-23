package net.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.world.inventory.ContainerData;

public class JumboFurnaceSyncData implements ContainerData
{
	private final JumboFurnaceCoreBlockEntity te;
	
	public JumboFurnaceSyncData(JumboFurnaceCoreBlockEntity te)
	{
		this.te = te;
	}

	@Override
	public int get(int index)
	{
		switch (index)
		{
			case 0:
				return this.te.burnTimeRemaining;
			case 1:
				return this.te.lastItemBurnedValue;
			case 2:
				return this.te.inFlightRecipes.size();
			default:
				return 0;
		}
	}

	@Override
	public void set(int index, int value)
	{
		// noop, this is the serverside data and set is only called clientside
	}

	@Override
	public int getCount()
	{
		return 3;
	}

}
