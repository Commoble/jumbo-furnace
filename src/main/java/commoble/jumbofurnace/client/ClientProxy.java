package commoble.jumbofurnace.client;

import commoble.jumbofurnace.JumboFurnace;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientProxy
{
	public static void addClientListeners(IEventBus modBus, IEventBus forgeBus)
	{
		modBus.addListener(ClientProxy::onRegisterMenuScreens);
	}
	
	private static void onRegisterMenuScreens(RegisterMenuScreensEvent event)
	{
		event.register(JumboFurnace.get().jumboFurnaceMenuType.get(), JumboFurnaceScreen::new);
	}
}
