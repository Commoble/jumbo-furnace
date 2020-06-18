package com.github.commoble.jumbofurnace;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.util.BlockSnapshot;

public class JumboFurnaceBlock extends Block
{
	public static final IntegerProperty X = IntegerProperty.create("x", 0, 2);
	public static final IntegerProperty Y = IntegerProperty.create("y", 0, 2);
	public static final IntegerProperty Z = IntegerProperty.create("z", 0, 2);
	public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

	public JumboFurnaceBlock(Properties properties)
	{
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState()
			.with(X, 0)
			.with(Y, 0)
			.with(Z, 0)
			.with(LIT, false)
			);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(X, Y, Z, LIT);
	}
	
	public void buildJumboFurnace(IWorld world, BlockPos corePos)
	{
		
	}
	
	public List<BlockSnapshot> getStatesForFurnace(IWorld world, BlockPos corePos)
	{
		List<BlockSnapshot> snapshots = new ArrayList<>(27);
		
		for (int x=0; x<3; x++)
		{
			for (int y=0; y<3; y++)
			{
				for (int z=0; z<3; z++)
				{
					BlockState state = this.getDefaultState()
						.with(X, x)
						.with(Y, y)
						.with(Z, z);
					BlockPos pos = corePos.add(x-1, y-1, z-1);
					BlockSnapshot snapshot = new BlockSnapshot(world, pos, state);
					snapshots.add(snapshot);
				}
			}
		}
		
		return snapshots;
	}
	
}
