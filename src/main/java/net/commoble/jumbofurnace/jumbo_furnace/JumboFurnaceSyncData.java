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
				return this.te.cookProgress;
			default:
				return 0;
		}
	}

	@Override
	public void set(int index, int value)
	{
		switch (index)
		{
			case 0:
				this.te.burnTimeRemaining = value;
				break;
			case 1:
				this.te.lastItemBurnedValue = value;
				break;
			case 2:
				this.te.cookProgress = value;
				break;
			default:
				break;
		}
	}

	@Override
	public int getCount()
	{
		return 3;
	}

}
