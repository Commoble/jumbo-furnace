package commoble.jumbofurnace.jumbo_furnace;

import commoble.jumbofurnace.recipes.ClaimableRecipeWrapper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class InputItemHandler extends ItemStackHandler
{
	public final JumboFurnaceCoreBlockEntity te;
	
	public InputItemHandler(JumboFurnaceCoreBlockEntity te)
	{
		super(9);
		this.te = te;
	}
	
	public ClaimableRecipeWrapper getFreshRecipeInput()
	{
		return new ClaimableRecipeWrapper(this);
	}

	@Override
	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		this.te.setChanged();
		this.te.onInputInventoryChanged();
	}
}
