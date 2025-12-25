package net.commoble.jumbofurnace.client.emi;

//@EmiEntrypoint
public class EmiProxy //implements EmiPlugin
{
//	public static final EmiStack JUMBO_FURNACE_ICON = EmiStack.of(JumboFurnace.get().jumboFurnaceJeiDummy.get());
//	public static final EmiRecipeCategory JUMBO_SMELTING_CATEGORY = new EmiRecipeCategory(JumboFurnace.get().jumboSmeltingRecipeSerializer.getId(), JUMBO_FURNACE_ICON);
//		
//	@Override
//	public void register(EmiRegistry registry)
//	{
//		registry.addCategory(JUMBO_SMELTING_CATEGORY);
//		registry.addWorkstation(JUMBO_SMELTING_CATEGORY, JUMBO_FURNACE_ICON);
//		
//		var recipeManager = registry.getRecipeManager();
//		for (var recipe : recipeManager.getAllRecipesFor(RecipeType.SMELTING))
//		{
//			registry.addRecipe(new JumboSmeltingEmiRecipe(wrapId(recipe.id()), new JumboFurnaceRecipe(recipe.value())));
//		}
//		for (var recipe : recipeManager.getAllRecipesFor(JumboFurnace.get().jumboSmeltingRecipeType.get()))
//		{
//			registry.addRecipe(new JumboSmeltingEmiRecipe(recipe.id(), recipe.value()));
//		}
//	}
//	
//	static Identifier wrapId(Identifier original)
//	{
//		return Identifier.fromNamespaceAndPath(original.getNamespace(), String.format("/jumbo_smelting_wrapper/%s", original.getPath()));
//	}

}
