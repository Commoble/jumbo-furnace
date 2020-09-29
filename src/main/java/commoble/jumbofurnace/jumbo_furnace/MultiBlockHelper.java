package commoble.jumbofurnace.jumbo_furnace;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import commoble.jumbofurnace.JumboFurnaceObjects;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent.EntityMultiPlaceEvent;

public class MultiBlockHelper
{
	public static final List<Pair<BlockPos, BlockState>> NO_SNAPSHOTS = ImmutableList.of();
	/**
	 * Scans the area around the placement position to see if a new jumbo furnace can be formed
	 * If it can, returns a list of pairs of
	 * 	-- a block snapshot of the existing block
	 * 	-- the jumbo furnace state that that block should be set to
	 * @param world
	 * @param placePos
	 * @return
	 */
	public static List<Pair<BlockPos, BlockState>> getJumboFurnaceStates(RegistryKey<World> key, IWorld world, BlockPos placePos, BlockState againstState, Entity entity)
	{
		return get3x3CubeAround(placePos)
			.filter(pos -> canJumboFurnaceFormAt(world, pos, placePos))
			.map(pos -> getStatesForPlacementIfPermitted(key, world, pos, againstState, entity))
			.filter(list -> !list.isEmpty())
			.findFirst()
			.orElse(ImmutableList.of());
	}
	
	/**
	 * Returns whether a jumbo furnace can form around the given core position.
	 * @param world
	 * @param corePos The core position a jumbo furnace would form around
	 * @param placePos The position that a furnace block is about to be placed at
	 * @return
	 */
	public static boolean canJumboFurnaceFormAt(IWorld world, BlockPos corePos, BlockPos placePos)
	{
		return get3x3CubeAround(corePos)
			.allMatch(pos -> pos.equals(placePos) || world.getBlockState(pos).getBlock() == Blocks.FURNACE);
	}
	
	public static Stream<BlockPos> get3x3CubeAround(BlockPos pos)
	{
		return BlockPos.getAllInBox(pos.add(-1, -1, -1), pos.add(1,1,1));
	}
	
	public static List<Pair<BlockPos, BlockState>> getStatesForPlacementIfPermitted(RegistryKey<World> key, IWorld world, BlockPos corePos, BlockState againstState, Entity placer)
	{
		List<Pair<BlockPos, BlockState>> pairs = JumboFurnaceObjects.BLOCK.getStatesForFurnace(world, corePos);
		return doesPlayerHavePermissionToMakeJumboFurnace(key, world, pairs, againstState, placer)
			? pairs
			: NO_SNAPSHOTS;
	}
	
	public static boolean doesPlayerHavePermissionToMakeJumboFurnace(RegistryKey<World> key, IWorld world, List<Pair<BlockPos, BlockState>> pairs, BlockState placedAgainst, Entity entity)
	{
		List<BlockSnapshot> snapshots = pairs.stream()
			.map(pair -> BlockSnapshot.create(key, world, pair.getFirst()))
			.collect(Collectors.toList());
		EntityMultiPlaceEvent event = new EntityMultiPlaceEvent(snapshots, placedAgainst, entity);
		MinecraftForge.EVENT_BUS.post(event);
		return !event.isCanceled();
	}
}
