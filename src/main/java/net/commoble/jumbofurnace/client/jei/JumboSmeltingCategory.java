package net.commoble.jumbofurnace.client.jei;

import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.commoble.jumbofurnace.JumboFurnace;
import net.commoble.jumbofurnace.Names;
import net.commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class JumboSmeltingCategory implements IRecipeCategory<JumboFurnaceRecipe>
{
	public static final RecipeType<JumboFurnaceRecipe> TYPE = RecipeType.create(JumboFurnace.MODID, Names.JUMBO_SMELTING, JumboFurnaceRecipe.class);
	
	private final IDrawable backgroundInputs;
	private final IDrawable xlBackgroundOutputs;
	private final IDrawable icon;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated animatedFlame;
	
	public JumboSmeltingCategory(IGuiHelper helper)
	{
		this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(JumboFurnace.get().jumboFurnaceJeiDummy.get()));
		this.backgroundInputs = helper.createDrawable(JEIProxy.CRAFTING_TEXTURE, 29, 16, 54, 54);
		this.xlBackgroundOutputs = helper.createDrawable(JEIProxy.CRAFTING_TEXTURE, 29, 16, 54, 54);
		this.animatedFlame = helper.createAnimatedRecipeFlame(300);
		this.arrow = helper.createAnimatedRecipeArrow(200);
	}

	@Override
	public int getWidth()
	{
		return backgroundInputs.getWidth() + 56 + xlBackgroundOutputs.getWidth();
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
		return this.backgroundInputs;
	}

	@Override
	public IDrawable getIcon()
	{
		return this.icon;
	}

	@Override
	public void draw(JumboFurnaceRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY)
	{
		this.xlBackgroundOutputs.draw(graphics, 110, 0);
		this.animatedFlame.draw(graphics, 75, 38);
		this.arrow.draw(graphics, 70, 18);

		float experience = recipe.experience();
		if (experience > 0)
		{
			String experienceString = I18n.get("gui.jei.category.smelting.experience", experience);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(experienceString);
			graphics.drawString(fontRenderer, experienceString, this.getWidth() - this.xlBackgroundOutputs.getWidth() - stringWidth, 0, 0xFF808080, false);
		}
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder recipeLayout, JumboFurnaceRecipe recipe, IFocusGroup focuses)
	{
		recipeLayout.setShapeless(60,0);

		List<ItemStack> results = recipe.results();
		int resultCount = results.size();
		for (int i=0; i<resultCount; i++)
		{
			int row = i / 3;
			int column = i % 3;
			int x = 111 + (column) * 18;
			int y = row*18 + 1;
			recipeLayout.addSlot(RecipeIngredientRole.OUTPUT, x, y)
				.addItemStack(results.get(i));
		}

		// input slots
		List<SizedIngredient> ingredients = recipe.ingredients();
		int ingredientCount = ingredients.size();
		for (int row = 0; row < 3; row++)
		{
			for (int column = 0; column < 3; column++)
			{
				int inputID = row * 3 + column;
				var slot = recipeLayout.addSlot(RecipeIngredientRole.INPUT, column*18 + 1, row*18 + 1);
				if (inputID < ingredientCount)
				{
					slot.addItemStacks(List.of(ingredients.get(inputID).getItems()));
				}
			}
		}
	}

}
