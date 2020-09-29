package commoble.jumbofurnace.client.jei;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import commoble.jumbofurnace.JumboFurnace;
import commoble.jumbofurnace.JumboFurnaceObjects;
import commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import commoble.jumbofurnace.recipes.RecipeSorter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class JEIProxy implements IModPlugin
{
	
	@Nullable
	private JumboSmeltingCategory jumboSmeltingCategory;

	public static final ResourceLocation ID = new ResourceLocation(JumboFurnace.MODID, JumboFurnace.MODID);
	
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
		this.jumboSmeltingCategory = new JumboSmeltingCategory(registration.getJeiHelpers().getGuiHelper());
		registration.addRecipeCategories(this.jumboSmeltingCategory);
	}

	/**
	 * Register recipe catalysts. Recipe Catalysts are ingredients that are needed
	 * in order to craft other things. Vanilla examples of Recipe Catalysts are the
	 * Crafting Table and Furnace.
	 */
	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{
		registration.addRecipeCatalyst(new ItemStack(JumboFurnaceObjects.JEI_DUMMY), JumboSmeltingCategory.ID);
	}

	/**
	 * Register modded recipes.
	 */
	@Override
	public void registerRecipes(IRecipeRegistration registration)
	{
		if (this.jumboSmeltingCategory == null)
		{
			throw new NullPointerException("Jumbo Furnace's Jumbo Smelting JEI category failed to register! Notify the developer for assistance https://github.com/Commoble/jumbo-furnace/issues");
		}
		registration.addRecipes(this.getRecipes(), JumboSmeltingCategory.ID);
	}
	
	public List<JumboFurnaceRecipe> getRecipes()
	{
		@SuppressWarnings("resource")
		ClientWorld world = Minecraft.getInstance().world;
		if (world != null)
		{
			RecipeManager manager = world.getRecipeManager();
			return RecipeSorter.INSTANCE.getSortedFurnaceRecipes(manager);
		}
		else
		{
			return ImmutableList.of();
		}
	}

}
