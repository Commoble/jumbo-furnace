package commoble.jumbofurnace.client;

import commoble.jumbofurnace.JumboFurnace;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy
{
	public static void addClientListeners(IEventBus modBus, IEventBus forgeBus)
	{
		modBus.addListener(ClientProxy::onClientSetup);
	}
	
	private static void onClientSetup(FMLClientSetupEvent event)
	{
		MenuScreens.register(JumboFurnace.get().jumboFurnaceMenuType.get(), JumboFurnaceScreen::new);
	}
}
