package net.commoble.jumbofurnace.client;

import net.commoble.jumbofurnace.JumboFurnace;
import net.commoble.jumbofurnace.recipes.RecipeSorter;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

public class ClientProxy
{
	public static RecipeMap clientRecipes = RecipeMap.EMPTY;
	public static RecipeSorter recipeSorter = new RecipeSorter();
	
	public static void addClientListeners(IEventBus modBus, IEventBus gameBus)
	{
		modBus.addListener(ClientProxy::onRegisterMenuScreens);
		gameBus.addListener(ClientProxy::onDatapackSync);
		gameBus.addListener(ClientProxy::onRecipesReceived);
		gameBus.addListener(ClientProxy::onLoggingOut);
	}
	
	private static void onRegisterMenuScreens(RegisterMenuScreensEvent event)
	{
		event.register(JumboFurnace.get().jumboFurnaceMenuType.get(), JumboFurnaceScreen::new);
	}
	
	private static void onDatapackSync(OnDatapackSyncEvent event)
	{
		event.sendRecipes(JumboFurnace.get().jumboSmeltingRecipeType.get());
	}
	
	private static void onRecipesReceived(RecipesReceivedEvent event)
	{
		clientRecipes = event.getRecipeMap();
		recipeSorter = new RecipeSorter();
	}
	
	private static void onLoggingOut(LoggingOut event)
	{
		clientRecipes = RecipeMap.EMPTY;
		recipeSorter = new RecipeSorter();
	}
}
