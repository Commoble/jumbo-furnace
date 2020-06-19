package com.github.commoble.jumbofurnace;

import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent.EntityMultiPlaceEvent;

public class MultiBlockHelper
{
	public static final List<BlockSnapshot> NO_SNAPSHOTS = ImmutableList.of();
	/**
	 * Scans the area around the placement position to see if a new jumbo furnace can be formed
	 * If it can, returns a list of the states to set;
	 * if it can't returns an empty list
	 * @param world
	 * @param placePos
	 * @return
	 */
	public static List<BlockSnapshot> getJumboFurnaceStates(IWorld world, BlockPos placePos, BlockState againstState, Entity entity)
	{
		return get3x3CubeAround(placePos)
			.filter(pos -> canJumboFurnaceFormAt(world, pos, placePos))
			.map(pos -> getSnapshotsIfPermitted(world, pos, againstState, entity))
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
	
	public static List<BlockSnapshot> getSnapshotsIfPermitted(IWorld world, BlockPos corePos, BlockState againstState, Entity placer)
	{
		List<BlockSnapshot> snapshots = JumboFurnaceObjects.BLOCK.getStatesForFurnace(world, corePos);
		return doesPlayerHavePermissionToMakeJumboFurnace(snapshots, againstState, placer)
			? snapshots
			: NO_SNAPSHOTS;
	}
	
	public static boolean doesPlayerHavePermissionToMakeJumboFurnace(List<BlockSnapshot> snapshots, BlockState placedAgainst, Entity entity)
	{
		EntityMultiPlaceEvent event = new EntityMultiPlaceEvent(snapshots, placedAgainst, entity);
		MinecraftForge.EVENT_BUS.post(event);
		return !event.isCanceled();
	}
}
