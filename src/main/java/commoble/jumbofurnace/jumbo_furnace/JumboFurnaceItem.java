package commoble.jumbofurnace.jumbo_furnace;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.BlockSnapshot;

public class JumboFurnaceItem extends Item
{

	public JumboFurnaceItem(Properties properties)
	{
		super(properties);
	}
	
	
	
	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		tooltip.add(new TranslationTextComponent("jumbofurnace.jumbo_furnace_info_tooltip"));
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
		List<Pair<BlockPos, BlockState>> placementStates = new ArrayList<>(27);
		List<BlockSnapshot> snapshots = new ArrayList<>();
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
