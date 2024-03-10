package commoble.jumbofurnace.client.jei;

import commoble.jumbofurnace.JumboFurnace;
import commoble.jumbofurnace.Names;
import commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class JumboSmeltingCategory implements IRecipeCategory<JumboFurnaceRecipe>
{
	public static final RecipeType<JumboFurnaceRecipe> TYPE = RecipeType.create(JumboFurnace.MODID, Names.JUMBO_SMELTING, JumboFurnaceRecipe.class);
	
	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawableAnimated arrow;
	private final IDrawableStatic staticFlame;
	private final IDrawableAnimated animatedFlame;
	private final IDrawableStatic backgroundFlame;
	
	public JumboSmeltingCategory(IGuiHelper helper)
	{
		this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(JumboFurnace.get().jumboFurnaceJeiDummy.get()));
		this.background = helper.createDrawable(JEIProxy.JEI_RECIPE_TEXTURE, 0, 60, 116, 54);
		this.arrow = helper.drawableBuilder(JEIProxy.JEI_RECIPE_TEXTURE, 82, 128, 24, 17)
			.buildAnimated(JumboFurnace.get().serverConfig.jumboFurnaceCookTime().get(), IDrawableAnimated.StartDirection.LEFT, false);
		this.staticFlame = helper.createDrawable(JEIProxy.JEI_RECIPE_TEXTURE, 82, 114, 14, 14);
		this.animatedFlame = helper.createAnimatedDrawable(this.staticFlame, 300, IDrawableAnimated.StartDirection.TOP, true);
		this.backgroundFlame = helper.createDrawable(JEIProxy.JEI_RECIPE_TEXTURE, 1, 134, 14, 14);
	}

	@Override
	public RecipeType<JumboFurnaceRecipe> getRecipeType()
	{
		return TYPE;
	}

	@Override
	public Component getTitle()
	{
		return Component.translatable("gui.jumbofurnace.category.jumbo_smelting");
	}

	@Override
	public IDrawable getBackground()
	{
		return this.background;
	}

	@Override
	public IDrawable getIcon()
	{
		return this.icon;
	}

	@Override
	public void draw(JumboFurnaceRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY)
	{
		this.backgroundFlame.draw(graphics, 66, 38);
		this.animatedFlame.draw(graphics, 66, 38);
		this.arrow.draw(graphics, 60, 18);

		float experience = recipe.experience();
		if (experience > 0)
		{
			String experienceString = I18n.get("gui.jei.category.smelting.experience", experience);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(experienceString);
			graphics.drawString(fontRenderer, experienceString, this.background.getWidth() - stringWidth, 0, 0xFF808080, false);
		}
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder recipeLayout, JumboFurnaceRecipe recipe, IFocusGroup focuses)
	{
		recipeLayout.setShapeless(60,0);
		
		// output slot
		recipeLayout.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
			.addItemStack(recipe.result());

		// input slots
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		int ingredientCount = ingredients.size();
		for (int row = 0; row < 3; row++)
		{
			for (int column = 0; column < 3; column++)
			{
				int inputID = row * 3 + column;
				var slot = recipeLayout.addSlot(RecipeIngredientRole.INPUT, column*18 + 1, row*18 + 1);
				if (inputID < ingredientCount)
				{
					slot.addIngredients(ingredients.get(inputID));
				}
			}
		}
	}

}
