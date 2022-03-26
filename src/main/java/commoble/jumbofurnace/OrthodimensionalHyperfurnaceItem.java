package commoble.jumbofurnace;

import java.util.List;
import java.util.function.Consumer;

import commoble.jumbofurnace.client.ClientProxy;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;

public class OrthodimensionalHyperfurnaceItem extends Item
{
	public static final Component USE_MESSAGE = new TranslatableComponent("item.jumbofurnace.orthodimensional_hyperfurnace.use_message");
	public static final Component TOOLTIP = new TranslatableComponent("item.jumbofurnace.orthodimensional_hyperfurnace.tooltip").withStyle(ChatFormatting.GRAY);
	
	public OrthodimensionalHyperfurnaceItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Player player = context.getPlayer();
		if (player != null && player.level.isClientSide)
		{
			player.displayClientMessage(USE_MESSAGE, true);
		}
		return super.useOn(context);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
	{
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(TOOLTIP);
	}

	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer)
	{
		super.initializeClient(consumer);
		ClientProxy.initOrthoFurnace(consumer);
	}
	
	
}
