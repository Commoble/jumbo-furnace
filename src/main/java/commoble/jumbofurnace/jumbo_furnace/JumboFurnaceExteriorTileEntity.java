package commoble.jumbofurnace.jumbo_furnace;

import javax.annotation.Nullable;

import commoble.jumbofurnace.JumboFurnaceObjects;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public class JumboFurnaceExteriorTileEntity extends TileEntity
{

	public JumboFurnaceExteriorTileEntity()
	{
		super(JumboFurnaceObjects.EXTERIOR_TE_TYPE);
	}

	// exterior blocks grant interfacing access to the internal block's itemhandlers
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			JumboFurnaceCoreTileEntity core = this.getCoreTile();
			if (core != null)
			{
				if (side == Direction.UP)
				{
					return core.inputOptional.cast();
				}
				else if (side == Direction.DOWN)
				{
					return core.outputOptional.cast();
				}
				else
				{
					return core.fuelOptional.cast();
				}
			}
		}


		return super.getCapability(cap, side);
	}
	
	@Nullable
	public JumboFurnaceCoreTileEntity getCoreTile()
	{
		World world = this.world;
		BlockPos pos = this.pos;
		BlockState state = this.getBlockState();
		BlockPos corePos = JumboFurnaceObjects.BLOCK.getCorePos(state, pos);
		TileEntity te = world.getTileEntity(corePos);
		return te instanceof JumboFurnaceCoreTileEntity ? (JumboFurnaceCoreTileEntity)te : null;
	}

}
