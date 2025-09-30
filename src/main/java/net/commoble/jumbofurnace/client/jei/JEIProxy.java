package net.commoble.jumbofurnace.client.jei;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.commoble.jumbofurnace.JumboFurnace;
import net.commoble.jumbofurnace.client.ClientProxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

//@JeiPlugin TODO don't let JEI load this class until we have the 1.21.9+ apis
public class JEIProxy implements IModPlugin
{
	public static final ResourceLocation CRAFTING_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");
	public static final ResourceLocation FURNACE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
	
	@Nullable
	private JumboSmeltingCategory jumboSmeltingCategory;
	
	@Nullable
	private JumboFurnaceUpgradeCategory jumboFurnaceUpgradeCategory;

	public static final ResourceLocation ID = JumboFurnace.id(JumboFurnace.MODID);
	
	@Override
	public ResourceLocation getPluginUid()
	{
		return ID;
	}

	/**
	 * Register the categories handled by this plugin. These are registered before
	 * recipes so they can be checked for validity.
	 */
	@Override
	public void registerCategories(IRecipeCategoryRegistration registration)
	{
		IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
		this.jumboSmeltingCategory = new JumboSmeltingCategory(helper);
		this.jumboFurnaceUpgradeCategory = new JumboFurnaceUpgradeCategory(helper);
		registration.addRecipeCategories(this.jumboSmeltingCategory, this.jumboFurnaceUpgradeCategory);
	}

	/**
	 * Register recipe catalysts. Recipe Catalysts are ingredients that are needed
	 * in order to craft other things. Vanilla examples of Recipe Catalysts are the
	 * Crafting Table and Furnace.
	 */
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addCraftingStation(
			JumboSmeltingCategory.TYPE,
			VanillaTypes.ITEM_STACK,
			new ItemStack(JumboFurnace.get().jumboFurnaceJeiDummy.get()));

		registration.addCraftingStation(
			JumboFurnaceUpgradeCategory.TYPE,
			VanillaTypes.ITEM_STACK,
			new ItemStack(JumboFurnace.get().jumboFurnaceJeiDummy.get()));
	}

	/**
	 * Register modded recipes.
	 */
	@Override
	public void registerRecipes(IRecipeRegistration registration)
	{
		if (this.jumboSmeltingCategory == null || this.jumboFurnaceUpgradeCategory == null)
		{
			throw new NullPointerException("Jumbo Furnace's Jumbo Smelting JEI categories failed to register! Notify the developer for assistance https://github.com/Commoble/jumbo-furnace/issues");
		}
		registration.addRecipes(JumboSmeltingCategory.TYPE, ClientProxy.getAllSortedFurnaceRecipes());
		registration.addRecipes(JumboFurnaceUpgradeCategory.TYPE, List.of(JumboFurnaceUpgradeCategory.JumboFurnaceUpgrade.INSTANCE));
	}
}
