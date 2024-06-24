package net.commoble.jumbofurnace.jumbo_furnace;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.commoble.jumbofurnace.JumboFurnace;
import net.commoble.jumbofurnace.recipes.InFlightRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.IItemHandler;

public class JumboFurnaceBlock extends Block implements EntityBlock
{
	public static final IntegerProperty X = IntegerProperty.create("x", 0, 2);
	public static final IntegerProperty Y = IntegerProperty.create("y", 0, 2);
	public static final IntegerProperty Z = IntegerProperty.create("z", 0, 2);
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public JumboFurnaceBlock(Properties properties)
	{
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
			.setValue(X, 0)
			.setValue(Y, 0)
			.setValue(Z, 0)
			.setValue(LIT, false)
			);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(X, Y, Z, LIT);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return this.isCore(state)
			? JumboFurnace.get().jumboFurnaceCoreBlockEntityType.get().create(pos,state)
			: JumboFurnace.get().jumboFurnaceExteriorBlockEntityType.get().create(pos,state);
	}
	
	
	
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		// if player uses shears on block, drop a whole jumbo furnace instead
		if (player.isShiftKeyDown() && stack.is(Tags.Items.TOOLS_SHEAR))
		{
			return ItemInteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
	{
		BlockPos corePos = JumboFurnaceBlock.getCorePos(state, pos);
		BlockEntity be = level.getBlockEntity(corePos);
		if (be instanceof JumboFurnaceCoreBlockEntity core)
		{
			if (player instanceof ServerPlayer serverPlayer)
			{
				serverPlayer.openMenu(JumboFurnaceMenu.getServerMenuProvider(core, pos));
			}
			
			return InteractionResult.SUCCESS;
		}
		
		return super.useWithoutItem(state, level, pos, player, hit);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof JumboFurnaceCoreBlockEntity core)
			{
				List<ItemStack> drops = new ArrayList<>();
				double x = pos.getX() + 0.5D;
				double y = pos.getY() + 0.5D;
				double z = pos.getZ() + 0.5D;
				float experience = core.output.storedExperience;
				// drop everything in the inventory slots
				for (int i=0; i<JumboFurnaceMenu.INPUT_SLOTS; i++)
				{
					drops.add(core.input.getStackInSlot(i));
					drops.add(core.fuel.getStackInSlot(i));
					drops.add(core.output.getStackInSlot(i));
				}
				drops.add(core.multiprocessUpgradeHandler.getStackInSlot(0));
				// drop the internal inventories too
				for (InFlightRecipe inflight : core.inFlightRecipes)
				{
					for (ItemStack input : inflight.inputs())
					{
						drops.add(input);
					}
				}
				for (ItemStack stack : core.backstock)
				{
					drops.add(stack);
				}
				// take all those items and spawn them in the world
				for (ItemStack drop : drops)
				{
					Containers.dropItemStack(level, x, y, z, drop);
				}
				Player player = level.getNearestPlayer(x, y, z, 16D, null);
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
				this.destroyNextBlockPos(level, state, pos);
			}

			super.onRemove(state, level, pos, newState, isMoving);
		}
	}
	
	public void destroyNextBlockPos(Level level, BlockState state, BlockPos pos)
	{
		if (state.hasProperty(X) && state.hasProperty(Y) && state.hasProperty(Z))
		{
			int xIndex = state.getValue(X);
			int yIndex = state.getValue(Y);
			int zIndex = state.getValue(Z);
			
			int nextXIndex = (xIndex + 1) % 3;
			int nextYIndex = (xIndex == 2) ? (yIndex + 1) % 3 : yIndex;
			int nextZIndex = (xIndex == 2 && yIndex == 2) ? (zIndex + 1) % 3 : zIndex;
			
			BlockPos nextPos = pos.offset(nextXIndex - xIndex, nextYIndex - yIndex, nextZIndex - zIndex);
			BlockState nextState = level.getBlockState(nextPos);
			if (nextState.getBlock() == this)
			{
				level.destroyBlock(nextPos, true);
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
		int xOff = exteriorState.hasProperty(X) ? 1 - exteriorState.getValue(X) : 0;
		int yOff = exteriorState.hasProperty(Y) ? 1 - exteriorState.getValue(Y) : 0;
		int zOff = exteriorState.hasProperty(Z) ? 1 - exteriorState.getValue(Z) : 0;
		return exteriorPos.offset(xOff, yOff, zOff);
	}

	public boolean isCore(BlockState state)
	{
		return state.hasProperty(X) && state.hasProperty(Y) && state.hasProperty(Z)
			&& state.getValue(X) == 1
			&& state.getValue(Y) == 1
			&& state.getValue(Z) == 1;
	}
	
	public List<Pair<BlockPos, BlockState>> getStatesForFurnace(BlockPos corePos)
	{
		List<Pair<BlockPos, BlockState>> pairs = new ArrayList<>(27);
		
		for (int x=0; x<3; x++)
		{
			for (int y=0; y<3; y++)
			{
				for (int z=0; z<3; z++)
				{
					BlockState state = this.defaultBlockState()
						.setValue(X, x)
						.setValue(Y, y)
						.setValue(Z, z);
					BlockPos pos = corePos.offset(x-1, y-1, z-1);
					pairs.add(Pair.of(pos, state));
				}
			}
		}
		
		return pairs;
	}
	
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand)
	{
		if (state.getValue(LIT))
		{
			double x = pos.getX() + 0.5D;
			double y = pos.getY();
			double z = pos.getZ() + 0.5D;
			if (rand.nextDouble() < 0.1D)
			{
				level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
			}

			Direction direction = this.getSmelterHoleDirection(state);
			if (direction != null)
			{
				Direction.Axis axis = direction.getAxis();
				double orthagonalOffset = rand.nextDouble() * 0.6D - 0.3D;
				double xOff = axis == Direction.Axis.X ? direction.getStepX() * 0.52D : orthagonalOffset;
				double yOff = rand.nextDouble() * 6.0D / 16.0D;
				double zOff = axis == Direction.Axis.Z ? direction.getStepZ() * 0.52D : orthagonalOffset;
				level.addParticle(ParticleTypes.SMOKE, x + xOff, y + yOff, z + zOff, 0.0D, 0.0D, 0.0D);
				level.addParticle(ParticleTypes.FLAME, x + xOff, y + yOff, z + zOff, 0.0D, 0.0D, 0.0D);
			}
		}
	}
	
	/** Returns null if this isn't one of the blocks with the smelter holes **/
	@Nullable
	public Direction getSmelterHoleDirection(BlockState state)
	{
		if (!state.hasProperty(X) || !state.hasProperty(Y) || !state.hasProperty(Z))
			return null;
		
		int y = state.getValue(Y);
		if (y != 1)
			return null;
		
		int x = state.getValue(X);
		int z = state.getValue(Z);
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
	
	@Override
	@Deprecated
	public boolean hasAnalogOutputSignal(BlockState state)
	{
		return true;
	}

	@Override
	@Deprecated
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos)
	{
		BlockPos corePos = getCorePos(state, pos);
		BlockEntity be = level.getBlockEntity(corePos);
		if (!(be instanceof JumboFurnaceCoreBlockEntity core) || !state.hasProperty(Y))
		{
			// if we are in an invalid state, return 0
			return 0;
		}
		
		int y = state.getValue(Y);

		// top layer of blocks: comparator output is input inventory
		// middle layer of blocks: comparator output is fuel inventory
		// bottom layer of blocks: comparator output is output inventory
		
		switch(y)
		{
			case 0: return calcRedstoneFromItemHandler(core.output);
			case 1: return calcRedstoneFromItemHandler(core.fuel);
			case 2: return calcRedstoneFromItemHandler(core.input);
			default: return 0;
		}
	}
	
	// same math as Container.calcRedstone
	public static int calcRedstoneFromItemHandler(@Nonnull IItemHandler handler)
	{
		int nonEmptySlots = 0;
		float totalItemValue = 0.0F;
		int slots = handler.getSlots();

		for (int slot = 0; slot < slots; ++slot)
		{
			ItemStack itemstack = handler.getStackInSlot(slot);
			if (!itemstack.isEmpty())
			{
				totalItemValue += itemstack.getCount() / (float) Math.min(handler.getSlotLimit(slot), itemstack.getMaxStackSize());
				++nonEmptySlots;
			}
		}

		float averageItemValue = totalItemValue / slots;
		return Mth.floor(averageItemValue * 14.0F) + (nonEmptySlots > 0 ? 1 : 0);
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
			int x = state.getValue(X);
			int z = state.getValue(Z);
			switch(rot)
			{
				case NONE:
					return state;
				case CLOCKWISE_90:
					return state.setValue(X, 2-z).setValue(Z, x);
				case CLOCKWISE_180:
					return state.setValue(X, 2-x).setValue(Z, 2-z);
				case COUNTERCLOCKWISE_90:
					return state.setValue(X, z).setValue(Z, 2-x);
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
					return state.setValue(Z, 2 - state.getValue(Z));
				case FRONT_BACK: // mirror across the z-axis (flip X)
					return state.setValue(X, 2 - state.getValue(X));
				default:
					return state;
			}
		}
		else
			return super.mirror(state, mirror);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
		return type == JumboFurnace.get().jumboFurnaceCoreBlockEntityType.get() && !level.isClientSide
			? (BlockEntityTicker<T>)JumboFurnaceCoreBlockEntity.SERVER_TICKER
			: null;
	}
	
	
}
