package commoble.jumbofurnace.jumbo_furnace;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
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
		BlockPos usePos = context.getPos();
		Direction useNormal = context.getFace();
		BlockPos corePos = usePos.offset(useNormal);
		World world = context.getWorld();
		RegistryKey<World> key = world.getDimensionKey();
		BlockItemUseContext blockContext = new BlockItemUseContext(context);
		if (MultiBlockHelper.canJumboFurnacePlaceAt(world, corePos, blockContext))
		{
			if (!world.isRemote)
			{
				BlockPos againstPos = usePos.offset(useNormal.getOpposite());
				BlockState againstState = world.getBlockState(againstPos);
				@Nullable PlayerEntity player = context.getPlayer();
				MultiBlockHelper.getStatesForPlacementIfPermitted(key, world, corePos, againstState, player)
					.forEach(pair -> world.setBlockState(pair.getFirst(), pair.getSecond()));
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
