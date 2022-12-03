package commoble.jumbofurnace.jumbo_furnace;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class JumboFurnaceItem extends Item
{

	public JumboFurnaceItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		// get the nine positions around the point of activation, offset by the raytrace normal
		BlockPos againstPos = context.getClickedPos();
		Direction useNormal = context.getClickedFace();
		BlockPos placePos = againstPos.relative(useNormal);
		BlockPos corePos = placePos.relative(useNormal);
		Level world = context.getLevel();
		ResourceKey<Level> key = world.dimension();
		BlockPlaceContext blockContext = new BlockPlaceContext(context);
		if (MultiBlockHelper.canJumboFurnacePlaceAt(world, corePos, blockContext))
		{
			if (!world.isClientSide)
			{
				BlockState againstState = world.getBlockState(againstPos);
				@Nullable Player player = context.getPlayer();
				List<Pair<BlockPos, BlockState>> placementStates = MultiBlockHelper.getStatesForPlacementIfPermitted(key, world, corePos, againstState, player);
				if (!placementStates.isEmpty())
				{

					placementStates.forEach(pair -> world.setBlockAndUpdate(pair.getFirst(), pair.getSecond()));
					BlockState jumboState = placementStates.get(0).getSecond();
					SoundType soundtype = jumboState.getSoundType(world, placePos, player);
					// play the sound to null player because we're not placing on the client
					world.playSound(null, placePos, jumboState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					if (player == null || !player.getAbilities().instabuild) {
						context.getItemInHand().shrink(1);
					}
				}
			}
			
			// need to check perms on server, but have to return same action result on client
			return InteractionResult.SUCCESS;
		}
		
		return super.useOn(context);
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
