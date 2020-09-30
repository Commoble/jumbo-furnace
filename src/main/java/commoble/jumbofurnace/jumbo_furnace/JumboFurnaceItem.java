package commoble.jumbofurnace.jumbo_furnace;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class JumboFurnaceItem extends Item
{

	public JumboFurnaceItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		// get the nine positions around the point of activation, offset by the raytrace normal
		BlockPos againstPos = context.getPos();
		Direction useNormal = context.getFace();
		BlockPos placePos = againstPos.offset(useNormal);
		BlockPos corePos = placePos.offset(useNormal);
		World world = context.getWorld();
		RegistryKey<World> key = world.getDimensionKey();
		BlockItemUseContext blockContext = new BlockItemUseContext(context);
		if (MultiBlockHelper.canJumboFurnacePlaceAt(world, corePos, blockContext))
		{
			if (!world.isRemote)
			{
				BlockState againstState = world.getBlockState(againstPos);
				@Nullable PlayerEntity player = context.getPlayer();
				List<Pair<BlockPos, BlockState>> placementStates = MultiBlockHelper.getStatesForPlacementIfPermitted(key, world, corePos, againstState, player);
				if (!placementStates.isEmpty())
				{

					placementStates.forEach(pair -> world.setBlockState(pair.getFirst(), pair.getSecond()));
					BlockState jumboState = placementStates.get(0).getSecond();
					SoundType soundtype = jumboState.getSoundType(world, placePos, player);
					// play the sound to null player because we're not placing on the client
					world.playSound(null, placePos, jumboState.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					if (player == null || !player.abilities.isCreativeMode) {
						context.getItem().shrink(1);
					}
				}
			}
			
			// need to check perms on server, but have to return same action result on client
			return ActionResultType.SUCCESS;
		}
		
		return super.onItemUse(context);
	}
	
	public static class Settable
	{
		private boolean set = false;
		
		public boolean get()
		{
			return this.set;
		}
		
		public void set()
		{
			this.set = true; 
		}
	}
}
