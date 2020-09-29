package com.github.commoble.jumbofurnace.client.jei;

import com.github.commoble.jumbofurnace.JumboFurnace;
import com.github.commoble.jumbofurnace.JumboFurnaceObjects;
import com.github.commoble.jumbofurnace.Names;
import com.github.commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import com.mojang.blaze3d.matrix.MatrixStack;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class JumboSmeltingCategory implements IRecipeCategory<JumboFurnaceRecipe>
{
	public static final ResourceLocation ID = new ResourceLocation(JumboFurnace.MODID, Names.JUMBO_SMELTING);
	public static final ResourceLocation JEI_RECIPE_TEXTURE = new ResourceLocation(ModIds.JEI_ID, "textures/gui/gui_vanilla.png");
	
	private final IDrawable background;
	private final IDrawable icon;
	private final String localizedName;
	private final IDrawableAnimated arrow;
	private final IDrawableStatic staticFlame;
	private final IDrawableAnimated animatedFlame;
	private final IDrawableStatic backgroundFlame;
	
	public JumboSmeltingCategory(IGuiHelper helper)
	{
		this.icon = helper.createDrawableIngredient(new ItemStack(JumboFurnaceObjects.JEI_DUMMY));
		this.background = helper.createDrawable(JEI_RECIPE_TEXTURE, 0, 60, 116, 54);
		this.localizedName = I18n.format("gui.jumbofurnace.category.jumbo_smelting");
		this.arrow = helper.drawableBuilder(JEI_RECIPE_TEXTURE, 82, 128, 24, 17)
			.buildAnimated(JumboFurnace.SERVER_CONFIG.jumboFurnaceCookTime.get(), IDrawableAnimated.StartDirection.LEFT, false);
		this.staticFlame = helper.createDrawable(JEI_RECIPE_TEXTURE, 82, 114, 14, 14);
		this.animatedFlame = helper.createAnimatedDrawable(this.staticFlame, 300, IDrawableAnimated.StartDirection.TOP, true);
		this.backgroundFlame = helper.createDrawable(JEI_RECIPE_TEXTURE, 1, 134, 14, 14);
	}

	@Override
	public ResourceLocation getUid()
	{
		return ID;
	}

	@Override
	public Class<? extends JumboFurnaceRecipe> getRecipeClass()
	{
		return JumboFurnaceRecipe.class;
	}

	@Override
	public String getTitle()
	{
		return this.localizedName;
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
	public void setIngredients(JumboFurnaceRecipe recipe, IIngredients ingredients)
	{
		ingredients.setInputIngredients(recipe.getIngredients());
		ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
	}

	@Override
	public void draw(JumboFurnaceRecipe recipe, MatrixStack stack, double mouseX, double mouseY)
	{
		this.backgroundFlame.draw(stack, 66, 38);
		this.animatedFlame.draw(stack, 66, 38);
		this.arrow.draw(stack, 60, 18);

		float experience = recipe.experience;
		if (experience > 0)
		{
			String experienceString = I18n.format("gui.jei.category.smelting.experience", experience);
			Minecraft minecraft = Minecraft.getInstance();
			FontRenderer fontRenderer = minecraft.fontRenderer;
			int stringWidth = fontRenderer.getStringWidth(experienceString);
			fontRenderer.drawString(stack, experienceString, this.background.getWidth() - stringWidth, 0, 0xFF808080);
		}
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, JumboFurnaceRecipe recipe, IIngredients ingredients)
	{
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		// output slot
		guiItemStacks.init(0, false, 94, 18);

		// input slots
		for (int row = 0; row < 3; row++)
		{
			for (int column = 0; column < 3; column++)
			{
				int inputID = row * 3 + column + 1;
				guiItemStacks.init(inputID, true, column * 18, row * 18);
			}
		}

		guiItemStacks.set(ingredients);
	}

}
