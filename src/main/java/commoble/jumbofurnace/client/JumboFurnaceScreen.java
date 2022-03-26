package commoble.jumbofurnace.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceMenuType;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class JumboFurnaceScreen extends AbstractContainerScreen<JumboFurnaceMenuType>
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

	public JumboFurnaceScreen(JumboFurnaceMenuType screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);
		this.imageWidth = 176;
		this.imageHeight = 240;
	}

	@Override
	public void render(PoseStack stack, int x, int y, float partialTicks)
	{
		this.renderBackground(stack);
		super.render(stack, x, y, partialTicks);
		this.renderTooltip(stack, x, y);
	}

	@Override
	protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		
		int xStart = (this.width - this.imageWidth) / 2;
		int yStart = (this.height - this.imageHeight) / 2;
		
		// draw the background
		this.blit(stack, xStart, yStart, 0,0, this.imageWidth, this.imageHeight);
		
		// draw progress bars
		if (this.menu.isBurning())
		{
			int burnAmount = (this.menu).getBurnLeftScaled() + 1;
			this.blit(stack, xStart + BURN_METER_TO_X, yStart + BURN_METER_TO_Y + BURN_METER_HEIGHT - burnAmount, BURN_METER_FROM_X, BURN_METER_HEIGHT - burnAmount, BURN_METER_WIDTH, burnAmount);
		}

		int cookProgress = (this.menu).getCookProgressionScaled() + 1;
		this.blit(stack, xStart + COOK_METER_TO_X, yStart + COOK_METER_TO_Y, COOK_METER_FROM_X, COOK_METER_FROM_Y, cookProgress, COOK_METER_HEIGHT);
	}

	@Override
	protected void renderLabels(PoseStack stack, int mouseX, int mouseY)
	{
		// positions and colors from ChestScreen
		this.font.draw(stack, this.title.getString(), 8.0F, 6.0F, 4210752);
		this.font.draw(stack, this.playerInventoryTitle, 8.0F, this.imageHeight - 96 + 2, 4210752);
	}

}
