package net.commoble.jumbofurnace.jumbo_furnace;

import org.jspecify.annotations.Nullable;

import net.commoble.jumbofurnace.JumboFurnace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class JumboFurnaceExteriorBlockEntity extends BlockEntity
{
	public static JumboFurnaceExteriorBlockEntity create(BlockPos pos, BlockState state)
	{
		return new JumboFurnaceExteriorBlockEntity(JumboFurnace.JUMBO_FURNACE_EXTERIOR_BLOCKENTITY_TYPE.get(), pos, state);
	}

	protected JumboFurnaceExteriorBlockEntity(BlockEntityType<? extends JumboFurnaceExteriorBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	// exterior blocks grant interfacing access to the internal block's itemhandlers
	public @Nullable ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side)
	{
		JumboFurnaceCoreBlockEntity core = this.getCoreTile();
		if (core != null)
		{
			if (side == null || side == Direction.UP)
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
		@Nullable Level level = this.level;
		if (level == null)
			return null;
		
		BlockPos thisPos = this.worldPosition;
		BlockState thisState = this.getBlockState();
		return thisState.getBlock() instanceof JumboFurnaceBlock
			&& level.getBlockEntity(JumboFurnaceBlock.getCorePos(thisState, thisPos)) instanceof JumboFurnaceCoreBlockEntity core
				? core
				: null;
	}
}
