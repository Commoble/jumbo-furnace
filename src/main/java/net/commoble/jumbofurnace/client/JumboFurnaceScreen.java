package net.commoble.jumbofurnace.client;

import java.util.List;

import net.commoble.jumbofurnace.JumboFurnace;
import net.commoble.jumbofurnace.jumbo_furnace.JumboFurnaceMenu;
import net.commoble.jumbofurnace.recipes.InFlightRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class JumboFurnaceScreen extends AbstractContainerScreen<JumboFurnaceMenu>
{
	public static final Identifier GUI_TEXTURE = JumboFurnace.id("textures/gui/jumbo_furnace.png");
	public static final Identifier LIT_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/lit_progress");

	// progress bar stuff
	public static final int BURN_METER_FROM_X = 176;
	public static final int BURN_METER_FROM_Y = 0;
	public static final int BURN_METER_WIDTH = 13;
	public static final int BURN_METER_HEIGHT = 13;
	public static final int BURN_METER_TO_X = 27;
	public static final int BURN_METER_TO_Y = 73;
	
	public static final int COOK_METER_WIDTH = 24;
	public static final int COOK_METER_HEIGHT = 16;
	public static final int COOK_METER_TO_X = 79;
	public static final int COOK_METER_TO_Y = 72;
	
	public static final int RECIPE_LINE_X_START = COOK_METER_TO_X - 16;
	public static final int RECIPE_LINE_X_END = COOK_METER_TO_X + COOK_METER_WIDTH;

	public JumboFurnaceScreen(JumboFurnaceMenu screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);
		this.imageWidth = 176;
		this.imageHeight = 240;
		this.titleLabelX = 8;
		this.titleLabelY = 6;
		this.inventoryLabelX = 8;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void render(GuiGraphics graphics, int x, int y, float partialTicks)
	{
		this.renderBackground(graphics, x, y, partialTicks);
		super.render(graphics, x, y, partialTicks);
		this.renderTooltip(graphics, x, y);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
	{		
		int xStart = (this.width - this.imageWidth) / 2;
		int yStart = (this.height - this.imageHeight) / 2;
		
		// draw the background
		graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, xStart, yStart, 0,0, this.imageWidth, this.imageHeight, 256, 256);
		
		// draw progress bars
		if (this.menu.isBurning())
		{
			int burnAmount = (this.menu).getBurnLeftScaled() + 1;
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE, 14, 14, 0, 14 - burnAmount, xStart + BURN_METER_TO_X, yStart + BURN_METER_TO_Y + 14 - burnAmount, 14, burnAmount);
		}
		
		// draw recipes moving from left to right
		for (InFlightRecipe recipe : this.menu.recipes)
		{
			float progress = (float)(recipe.progress()) / (float)(recipe.recipe().cookingTime());
			int recipeX = Mth.lerpInt(progress, xStart + RECIPE_LINE_X_START, xStart + RECIPE_LINE_X_END);
			int recipeY = COOK_METER_TO_Y;
			if (progress < 0.5F) // render inputs
			{
				List<ItemStack> inputs = recipe.inputs();
				for (ItemStack input : inputs)
				{
					graphics.renderItem(input, recipeX, recipeY);
				}
			}
			else // render outputs
			{
				List<ItemStack> outputs = recipe.recipe().results();
				for (ItemStack output : outputs)
				{
					graphics.renderItem(output, recipeX, recipeY);
				}
			}
		}
		
		// draw recipe count
		graphics.drawString(Minecraft.getInstance().font, Component.literal("x" + String.valueOf(this.menu.getCurrentRecipeCount())), xStart + COOK_METER_TO_X + 10, yStart + COOK_METER_TO_Y + 20, 0xFF373737, false);
	}
}
