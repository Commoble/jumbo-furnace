package commoble.jumbofurnace;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OrthodimensionalHyperfurnaceItem extends Item
{
	public static final ITextComponent USE_MESSAGE = new TranslationTextComponent("item.jumbofurnace.orthodimensional_hyperfurnace.use_message");
	public static final ITextComponent TOOLTIP = new TranslationTextComponent("item.jumbofurnace.orthodimensional_hyperfurnace.tooltip").mergeStyle(TextFormatting.GRAY);
	
	public OrthodimensionalHyperfurnaceItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		PlayerEntity player = context.getPlayer();
		if (player != null && player.world.isRemote)
		{
			player.sendStatusMessage(USE_MESSAGE, true);
		}
		return super.onItemUse(context);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(TOOLTIP);
	}
}
