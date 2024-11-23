package net.commoble.jumbofurnace.jumbo_furnace;

import javax.annotation.Nullable;

import net.commoble.jumbofurnace.JumboFurnace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public class JumboFurnaceExteriorBlockEntity extends BlockEntity
{
	public static JumboFurnaceExteriorBlockEntity create(BlockPos pos, BlockState state)
	{
		return new JumboFurnaceExteriorBlockEntity(JumboFurnace.get().jumboFurnaceExteriorBlockEntityType.get(), pos, state);
	}

	protected JumboFurnaceExteriorBlockEntity(BlockEntityType<? extends JumboFurnaceExteriorBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	// exterior blocks grant interfacing access to the internal block's itemhandlers
	@Nullable
	public IItemHandler getItemHandler(Direction side)
	{
		JumboFurnaceCoreBlockEntity core = this.getCoreTile();
		if (core != null && side != null)
		{
			if (side == Direction.UP)
			{
				return core.input;
			}
			else if (side == Direction.DOWN)
			{
				return core.output;
			}
			else
			{
				return core.fuel;
			}
		}
		return null;
	}
	
	@Nullable
	public JumboFurnaceCoreBlockEntity getCoreTile()
	{
		Level level = this.level;
		BlockPos thisPos = this.worldPosition;
		BlockState thisState = this.getBlockState();
		return thisState.getBlock() instanceof JumboFurnaceBlock
			&& level.getBlockEntity(JumboFurnaceBlock.getCorePos(thisState, thisPos)) instanceof JumboFurnaceCoreBlockEntity core
				? core
				: null;
	}
}
