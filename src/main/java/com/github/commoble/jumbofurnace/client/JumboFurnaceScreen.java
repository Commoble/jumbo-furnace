package com.github.commoble.jumbofurnace.client;

import com.github.commoble.jumbofurnace.JumboFurnaceContainer;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class JumboFurnaceScreen extends ContainerScreen<JumboFurnaceContainer>
{
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation("jumbofurnace:textures/gui/jumbo_furnace.png");
	

	
	// progress bar stuff
	public static final int BURN_METER_FROM_X = 176;
	public static final int BURN_METER_FROM_Y = 0;
	public static final int BURN_METER_WIDTH = 13;
	public static final int BURN_METER_HEIGHT = 13;
	public static final int BURN_METER_TO_X = 27;
	public static final int BURN_METER_TO_Y = 73;
	
	public static final int COOK_METER_FROM_X = 176;
	public static final int COOK_METER_FROM_Y = 14;
	public static final int COOK_METER_WIDTH = 24;
	public static final int COOK_METER_HEIGHT = 16;
	public static final int COOK_METER_TO_X = 79;
	public static final int COOK_METER_TO_Y = 72;

	public JumboFurnaceScreen(JumboFurnaceContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);
		this.xSize = 176;
		this.ySize = 240;
	}

	@Override
	public void render(int x, int y, float partialTicks)
	{
		this.renderBackground();
		super.render(x, y, partialTicks);
		this.renderHoveredToolTip(x, y);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		int xStart = (this.width - this.xSize) / 2;
		int yStart = (this.height - this.ySize) / 2;
		
		// draw the background
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		this.blit(xStart, yStart, 0,0, this.xSize, this.ySize);
		
		// draw progress bars
		if (this.container.isBurning())
		{
			int burnAmount = (this.container).getBurnLeftScaled() + 1;
			this.blit(xStart + BURN_METER_TO_X, yStart + BURN_METER_TO_Y + BURN_METER_HEIGHT - burnAmount, BURN_METER_FROM_X, BURN_METER_HEIGHT - burnAmount, BURN_METER_WIDTH, burnAmount);
		}

		int cookProgress = (this.container).getCookProgressionScaled() + 1;
		this.blit(xStart + COOK_METER_TO_X, yStart + COOK_METER_TO_Y, COOK_METER_FROM_X, COOK_METER_FROM_Y, cookProgress, COOK_METER_HEIGHT);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		// positions and colors from ChestScreen
		this.font.drawString(this.title.getFormattedText(), 8.0F, 6.0F, 4210752);
		this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0F, this.ySize - 96 + 2, 4210752);
	}

}
