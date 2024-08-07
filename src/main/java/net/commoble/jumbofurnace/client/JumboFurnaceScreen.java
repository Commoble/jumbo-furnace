package net.commoble.jumbofurnace.client;

import net.commoble.jumbofurnace.JumboFurnace;
import net.commoble.jumbofurnace.jumbo_furnace.JumboFurnaceMenu;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class JumboFurnaceScreen extends AbstractContainerScreen<JumboFurnaceMenu>
{
	public static final ResourceLocation GUI_TEXTURE = JumboFurnace.id("textures/gui/jumbo_furnace.png");
	public static final ResourceLocation LIT_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
	private static final ResourceLocation COOK_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");

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
		graphics.blit(GUI_TEXTURE, xStart, yStart, 0,0, this.imageWidth, this.imageHeight);
		
		// draw progress bars
		if (this.menu.isBurning())
		{
			int burnAmount = (this.menu).getBurnLeftScaled() + 1;
			graphics.blitSprite(LIT_PROGRESS_SPRITE, 14, 14, 0, 14 - burnAmount, xStart + BURN_METER_TO_X, yStart + BURN_METER_TO_Y + 14 - burnAmount, 14, burnAmount);
//			graphics.blitSprite(LIT_PROGRESS_SPRITE, xStart + BURN_METER_TO_X, yStart + BURN_METER_TO_Y + BURN_METER_HEIGHT - burnAmount, BURN_METER_FROM_X, BURN_METER_HEIGHT - burnAmount, BURN_METER_WIDTH, burnAmount);
		}

		int cookMeterPixels = this.getCookMeterPixels(partialTicks);
		if (cookMeterPixels > 0)
		{
			graphics.blitSprite(COOK_PROGRESS_SPRITE, COOK_METER_WIDTH, COOK_METER_HEIGHT, 0, 0, xStart + COOK_METER_TO_X, yStart + COOK_METER_TO_Y, cookMeterPixels, COOK_METER_HEIGHT);
		}
//		int cookProgress = (this.menu).getCookProgressionScaled() + 1;
//		graphics.blit(GUI_TEXTURE, xStart + COOK_METER_TO_X, yStart + COOK_METER_TO_Y, COOK_METER_FROM_X, COOK_METER_FROM_Y, cookProgress, COOK_METER_HEIGHT);
		// debug recipes to make sure we're doing it right
		graphics.drawString(Minecraft.getInstance().font, Component.literal("x" + String.valueOf(this.menu.getCurrentRecipeCount())), xStart + COOK_METER_TO_X + 10, yStart + COOK_METER_TO_Y + 20, 0x373737, false);
	}
	
	private int getCookMeterPixels(float partialTicks)
	{
		int recipes = this.menu.getCurrentRecipeCount();
		if (recipes <= 0)
		{
			return 0;
		}
		if (this.menu.getBurnTimeRemaining() <= 0)
		{
			return COOK_METER_WIDTH / 2;
		} 
		long gameTime = (long)(Util.getMillis() / 50D) % COOK_METER_WIDTH;
		return (int) gameTime;
	}
}
