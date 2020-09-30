package commoble.jumbofurnace.jumbo_furnace;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import commoble.jumbofurnace.JumboFurnaceObjects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;
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
	
	@Deprecated
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		// if player uses shears on block, drop a whole jumbo furnace instead
		ItemStack stack = player.getHeldItem(handIn);
		if (player.isSneaking() && handIn != null && Tags.Items.SHEARS.contains(stack.getItem()))
		{
			if (!world.isRemote)
			{
			}
			return ActionResultType.SUCCESS;
		}
		BlockPos corePos = JumboFurnaceBlock.getCorePos(state, pos);
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

	@Override
	@Deprecated
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof JumboFurnaceCoreTileEntity)
			{
				JumboFurnaceCoreTileEntity core = (JumboFurnaceCoreTileEntity)te;
				List<ItemStack> drops = new ArrayList<>();
				double x = pos.getX() + 0.5D;
				double y = pos.getY() + 0.5D;
				double z = pos.getZ() + 0.5D;
				float experience = 0;
				for (int i=0; i<JumboFurnaceContainer.INPUT_SLOTS; i++)
				{
					drops.add(core.input.getStackInSlot(i));
					drops.add(core.fuel.getStackInSlot(i));
					drops.add(core.output.getStackInSlot(i));
					experience += core.output.storedExperience[i];
				}
				for (ItemStack drop : drops)
				{
					InventoryHelper.spawnItemStack(world, x, y, z, drop);
				}
				PlayerEntity player = world.getClosestPlayer(x, y, z, 16D, null);
				if (player != null)
				{
					JumboFurnaceOutputSlot.spawnExpOrbs(player, experience);
				}
				
			}
			
			// we use the moving flag to check whether we should dismantle the rest of the furnace in the usual manner and drop blocks
			// things that dismantle the furnace themselves (shears) should remove blocks with moving == true
			// (block flag 64 or 1<<6 or Constants.BlockFlags.IS_MOVING)
			if (!isMoving)
			{
				this.destroyNextBlockPos(world, state, pos);
			}

			super.onReplaced(state, world, pos, newState, isMoving);
		}
	}
	
	public void destroyNextBlockPos(World world, BlockState state, BlockPos pos)
	{
		if (state.hasProperty(X) && state.hasProperty(Y) && state.hasProperty(Z))
		{
			int xIndex = state.get(X);
			int yIndex = state.get(Y);
			int zIndex = state.get(Z);
			
			int nextXIndex = (xIndex + 1) % 3;
			int nextYIndex = (xIndex == 2) ? (yIndex + 1) % 3 : yIndex;
			int nextZIndex = (xIndex == 2 && yIndex == 2) ? (zIndex + 1) % 3 : zIndex;
			
			BlockPos nextPos = pos.add(nextXIndex - xIndex, nextYIndex - yIndex, nextZIndex - zIndex);
			BlockState nextState = world.getBlockState(nextPos);
			if (nextState.getBlock() == this)
			{
				world.destroyBlock(nextPos, true);
			}
		}
	}

	/**
	 * Returns the assumed core position of a furnace cluster given one of its component blockstates.
	 * Not guaranteed to return a useful position if an invalid blockstate is used.
	 * @param exteriorState
	 * @param exteriorPos
	 * @return
	 */
	public static BlockPos getCorePos(BlockState exteriorState, BlockPos exteriorPos)
	{
		int xOff = exteriorState.hasProperty(X) ? 1 - exteriorState.get(X) : 0;
		int yOff = exteriorState.hasProperty(Y) ? 1 - exteriorState.get(Y) : 0;
		int zOff = exteriorState.hasProperty(Z) ? 1 - exteriorState.get(Z) : 0;
		return exteriorPos.add(xOff, yOff, zOff);
	}

	public boolean isCore(BlockState state)
	{
		return state.hasProperty(X) && state.hasProperty(Y) && state.hasProperty(Z)
			&& state.get(X) == 1
			&& state.get(Y) == 1
			&& state.get(Z) == 1;
	}
	
	public List<Pair<BlockPos, BlockState>> getStatesForFurnace(IWorld world, BlockPos corePos)
	{
		List<Pair<BlockPos, BlockState>> pairs = new ArrayList<>(27);
		
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
//					BlockSnapshot snapshot = BlockSnapshot.create(key, world, pos);
					pairs.add(Pair.of(pos, state));
				}
			}
		}
		
		return pairs;
	}

	/**
	 * Called periodically clientside on blocks near the player to show effects
	 * (like furnace fire particles). Note that this method is unrelated to
	 * {@link randomTick} and {@link #needsRandomTick}, and will always be called
	 * regardless of whether the block can receive random update ticks
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World worldIn, BlockPos pos, Random rand)
	{
		if (state.get(LIT))
		{
			double x = pos.getX() + 0.5D;
			double y = pos.getY();
			double z = pos.getZ() + 0.5D;
			if (rand.nextDouble() < 0.1D)
			{
				worldIn.playSound(x, y, z, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
			}

			Direction direction = this.getSmelterHoleDirection(state);
			if (direction != null)
			{
				Direction.Axis direction$axis = direction.getAxis();
				double orthagonalOffset = rand.nextDouble() * 0.6D - 0.3D;
				double xOff = direction$axis == Direction.Axis.X ? direction.getXOffset() * 0.52D : orthagonalOffset;
				double yOff = rand.nextDouble() * 6.0D / 16.0D;
				double zOff = direction$axis == Direction.Axis.Z ? direction.getZOffset() * 0.52D : orthagonalOffset;
				worldIn.addParticle(ParticleTypes.SMOKE, x + xOff, y + yOff, z + zOff, 0.0D, 0.0D, 0.0D);
				worldIn.addParticle(ParticleTypes.FLAME, x + xOff, y + yOff, z + zOff, 0.0D, 0.0D, 0.0D);
			}
		}
	}
	
	/** Returns null if this isn't one of the blocks with the smelter holes **/
	@Nullable
	public Direction getSmelterHoleDirection(BlockState state)
	{
		if (state.hasProperty(X) && state.hasProperty(Y) && state.hasProperty(Z))
		{
			int y = state.get(Y);
			if (y == 1)
			{
				int x = state.get(X);
				int z = state.get(Z);
				if (x == 1 && z == 0)
				{
					return Direction.NORTH;
				}
				else if (x == 1 && z == 2)
				{
					return Direction.SOUTH;
				}
				else if (x == 0 && z == 1)
				{
					return Direction.WEST;
				}
				else if (x == 2 && z == 1)
				{
					return Direction.EAST;
				}
				else
				{
					return null;
				}
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 */
	@Deprecated
	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		if (state.hasProperty(X) && state.hasProperty(Z))
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
	 */
	@Deprecated
	@Override
	public BlockState mirror(BlockState state, Mirror mirror)
	{
		if (state.hasProperty(X) && state.hasProperty(Z))
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
