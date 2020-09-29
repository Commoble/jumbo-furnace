package commoble.jumbofurnace.client;

import commoble.jumbofurnace.JumboFurnaceObjects;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents
{
	public static void addClientListeners(IEventBus modBus, IEventBus forgeBus)
	{
		modBus.addListener(ClientEvents::onClientSetup);
	}
	
	private static void onClientSetup(FMLClientSetupEvent event)
	{
		ScreenManager.registerFactory(JumboFurnaceObjects.CONTAINER_TYPE, JumboFurnaceScreen::new);
	}
}
