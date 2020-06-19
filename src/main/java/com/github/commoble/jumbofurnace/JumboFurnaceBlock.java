package com.github.commoble.jumbofurnace;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.fml.network.NetworkHooks;

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

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return this.isCore(state)
			? JumboFurnaceObjects.CORE_TE_TYPE.create()
			: JumboFurnaceObjects.EXTERIOR_TE_TYPE.create();
	}

	/**
	 * Amount of light emitted
	 * 
	 * @deprecated prefer calling {@link IBlockState#getLightValue()}
	 */
	@Override
	@Deprecated
	public int getLightValue(BlockState state)
	{
		return state.get(LIT) ? super.getLightValue(state) : 0;
	}
	
	@Deprecated
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		BlockPos corePos = this.getCorePos(state, pos);
		TileEntity te = world.getTileEntity(corePos);
		if (te instanceof JumboFurnaceCoreTileEntity)
		{
			if (player instanceof ServerPlayerEntity)
			{
				ServerPlayerEntity serverPlayer = (ServerPlayerEntity)player;
				JumboFurnaceCoreTileEntity core = (JumboFurnaceCoreTileEntity)te;
				IContainerProvider provider = JumboFurnaceContainer.getServerContainerProvider(core, pos);
				INamedContainerProvider namedProvider = new SimpleNamedContainerProvider(provider, JumboFurnaceContainer.TITLE);
				NetworkHooks.openGui(serverPlayer, namedProvider);
			}
			
			return ActionResultType.SUCCESS;
		}
		
		return super.onBlockActivated(state, world, corePos, player, handIn, hit);
	}
	
	/**
	 * Returns the assumed core position of a furnace cluster given one of its component blockstates.
	 * Not guaranteed to return a useful position if an invalid blockstate is used.
	 * @param exteriorState
	 * @param exteriorPos
	 * @return
	 */
	public BlockPos getCorePos(BlockState exteriorState, BlockPos exteriorPos)
	{
		int xOff = exteriorState.has(X) ? 1 - exteriorState.get(X) : 0;
		int yOff = exteriorState.has(Y) ? 1 - exteriorState.get(Y) : 0;
		int zOff = exteriorState.has(Z) ? 1 - exteriorState.get(Z) : 0;
		return exteriorPos.add(xOff, yOff, zOff);
	}

	public boolean isCore(BlockState state)
	{
		return state.has(X) && state.has(Y) && state.has(Z)
			&& state.get(X) == 1
			&& state.get(Y) == 1
			&& state.get(Z) == 1;
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

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	@Deprecated
	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		if (state.has(X) && state.has(Z))
		{
			int x = state.get(X);
			int z = state.get(Z);
			switch(rot)
			{
				case NONE:
					return state;
				case CLOCKWISE_90:
					return state.with(X, 2-z).with(Z, x);
				case CLOCKWISE_180:
					return state.with(X, 2-x).with(Z, 2-z);
				case COUNTERCLOCKWISE_90:
					return state.with(X, z).with(Z, 2-x);
				default:
					return state;
			}
		}
		else
		{
			return super.rotate(state, rot);
		}
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever
	 *             possible. Implementing/overriding is fine.
	 */
	@Deprecated
	@Override
	public BlockState mirror(BlockState state, Mirror mirror)
	{
		if (state.has(X) && state.has(Z))
		{
			switch(mirror)
			{
				case NONE:
					return state;
				case LEFT_RIGHT: // mirror across the x-axis (flip Z)
					return state.with(Z, 2 - state.get(Z));
				case FRONT_BACK: // mirror across the z-axis (flip X)
					return state.with(X, 2 - state.get(X));
				default:
					return state;
			}
		}
		else
			return super.mirror(state, mirror);
	}
}
