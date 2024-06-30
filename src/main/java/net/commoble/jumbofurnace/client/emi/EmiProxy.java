package net.commoble.jumbofurnace.client.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.commoble.jumbofurnace.JumboFurnace;
import net.commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

@EmiEntrypoint
public class EmiProxy implements EmiPlugin
{
	public static final EmiStack JUMBO_FURNACE_ICON = EmiStack.of(JumboFurnace.get().jumboFurnaceJeiDummy.get());
	public static final EmiRecipeCategory JUMBO_SMELTING_CATEGORY = new EmiRecipeCategory(JumboFurnace.get().jumboSmeltingRecipeSerializer.getId(), JUMBO_FURNACE_ICON);
		
	@Override
	public void register(EmiRegistry registry)
	{
		registry.addCategory(JUMBO_SMELTING_CATEGORY);
		registry.addWorkstation(JUMBO_SMELTING_CATEGORY, JUMBO_FURNACE_ICON);
		
		var recipeManager = registry.getRecipeManager();
		for (var recipe : recipeManager.getAllRecipesFor(RecipeType.SMELTING))
		{
			registry.addRecipe(new JumboSmeltingEmiRecipe(wrapId(recipe.id()), new JumboFurnaceRecipe(recipe.value())));
		}
		for (var recipe : recipeManager.getAllRecipesFor(JumboFurnace.get().jumboSmeltingRecipeType.get()))
		{
			registry.addRecipe(new JumboSmeltingEmiRecipe(recipe.id(), recipe.value()));
		}
	}
	
	static ResourceLocation wrapId(ResourceLocation original)
	{
		return ResourceLocation.fromNamespaceAndPath(original.getNamespace(), String.format("/jumbo_smelting_wrapper/%s", original.getPath()));
	}

}
