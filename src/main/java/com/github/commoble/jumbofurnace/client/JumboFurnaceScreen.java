package com.github.commoble.jumbofurnace.client;

import com.github.commoble.jumbofurnace.JumboFurnaceContainer;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class JumboFurnaceScreen extends ContainerScreen<JumboFurnaceContainer>
{
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation("jumbofurnace:textures/gui/jumbo_furnace.png");

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
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		// positions and colors from ChestScreen
		this.font.drawString(this.title.getFormattedText(), 8.0F, 6.0F, 4210752);
		this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0F, this.ySize - 96 + 2, 4210752);
	}

}
