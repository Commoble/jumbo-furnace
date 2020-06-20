package com.github.commoble.jumbofurnace.jumbo_furnace;

import net.minecraft.util.IIntArray;

public class JumboFurnaceSyncData implements IIntArray
{
	private final JumboFurnaceCoreTileEntity te;
	
	public JumboFurnaceSyncData(JumboFurnaceCoreTileEntity te)
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
	public int size()
	{
		return 3;
	}

}
