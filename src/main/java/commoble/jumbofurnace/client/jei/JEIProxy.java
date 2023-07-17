package commoble.jumbofurnace.client.jei;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import commoble.jumbofurnace.JumboFurnace;
import commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import commoble.jumbofurnace.recipes.RecipeSorter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

@JeiPlugin
public class JEIProxy implements IModPlugin
{
	public static final ResourceLocation JEI_RECIPE_TEXTURE = new ResourceLocation(ModIds.JEI_ID, "textures/jei/gui/gui_vanilla.png");
	
	@Nullable
	private JumboSmeltingCategory jumboSmeltingCategory;
	
	@Nullable
	private JumboFurnaceUpgradeCategory jumboFurnaceUpgradeCategory;

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
		registration.addRecipeCatalyst(new ItemStack(JumboFurnace.get().jumboFurnaceJeiDummy.get()), JumboSmeltingCategory.TYPE, JumboFurnaceUpgradeCategory.TYPE);
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
		registration.addRecipes(JumboSmeltingCategory.TYPE, this.getRecipes());
		registration.addRecipes(JumboFurnaceUpgradeCategory.TYPE, List.of(JumboFurnaceUpgradeCategory.JumboFurnaceUpgrade.INSTANCE));
	}
	
	public List<JumboFurnaceRecipe> getRecipes()
	{
		@SuppressWarnings("resource")
		ClientLevel world = Minecraft.getInstance().level;
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
