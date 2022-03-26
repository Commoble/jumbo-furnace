package commoble.jumbofurnace.client;

import java.util.function.Consumer;

import commoble.jumbofurnace.JumboFurnace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy
{
	public static void addClientListeners(IEventBus modBus, IEventBus forgeBus)
	{
		modBus.addListener(ClientProxy::onClientSetup);
	}
	
	public static void initOrthoFurnace(Consumer<IItemRenderProperties> consumer)
	{
		consumer.accept(new IItemRenderProperties()
		{
			@Override
			public BlockEntityWithoutLevelRenderer getItemStackRenderer()
			{
				return OrthodimensionalHyperfurnaceRenderer.INSTANCE.get();
			}
			
		});
	}
	
	private static void onClientSetup(FMLClientSetupEvent event)
	{
		MenuScreens.register(JumboFurnace.get().jumboFurnaceMenuType.get(), JumboFurnaceScreen::new);
	}
}
