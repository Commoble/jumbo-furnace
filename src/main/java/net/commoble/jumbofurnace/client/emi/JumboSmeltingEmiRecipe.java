package net.commoble.jumbofurnace.client.emi;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record JumboSmeltingEmiRecipe(ResourceLocation id, List<EmiIngredient> inputs, List<EmiStack> outputs, float experience, int cookingTime) implements EmiRecipe
{
	public JumboSmeltingEmiRecipe(ResourceLocation id, JumboFurnaceRecipe recipe)
	{
		this(id,
			recipe.ingredients()
				.stream()
				.map(sizedIngredient -> EmiIngredient.of(sizedIngredient.ingredient(), sizedIngredient.count()))
				.toList(),
			recipe.results()
				.stream()
				.map(EmiStack::of)
				.toList(),
			recipe.experience(),
			recipe.cookingTime());
			
	}

	@Override
	public EmiRecipeCategory getCategory()
	{
		return EmiProxy.JUMBO_SMELTING_CATEGORY;
	}

	@Override
	public @Nullable ResourceLocation getId()
	{
		return this.id;
	}

	@Override
	public List<EmiIngredient> getInputs()
	{
		return this.inputs;
	}

	@Override
	public List<EmiStack> getOutputs()
	{
		return this.outputs;
	}

	@Override
	public int getDisplayWidth()
	{
		return 164;
	}

	@Override
	public int getDisplayHeight()
	{
		return 54;
	}

	@Override
	public void addWidgets(WidgetHolder widgets)
	{
		widgets.addTexture(EmiTexture.SHAPELESS, 56, 2);
		widgets.addFillingArrow(70, 18, 50 * this.cookingTime).tooltip((mx, my) -> {
			return List.of(ClientTooltipComponent.create(Component.translatable("emi.cooking.time", this.cookingTime / 20f).getVisualOrderText()));
		});
		widgets.addTexture(EmiTexture.EMPTY_FLAME, 74, 37);
		widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, 74, 37, 4000, false, true, true);
		
		if (this.experience > 0)
		{
			String experienceString = I18n.get("emi.cooking.experience", this.experience);
			Minecraft minecraft = Minecraft.getInstance();
			Font fontRenderer = minecraft.font;
			int stringWidth = fontRenderer.width(experienceString);
			widgets.addText(Component.translatable("emi.cooking.experience", this.experience), 109-stringWidth, 0, -1, true);
		}
		
		
		int inputCount = this.inputs.size();
		int outputCount = this.outputs.size();
		
		for (int i=0; i<9; i++)
		{
			int row = i / 3;
			int column = i % 3;
			
			int inputX = column*18;
			int slotY = row*18;
			if (i < inputCount)
			{
				widgets.addSlot(this.inputs.get(i), inputX, slotY);
			}
			else
			{
				widgets.addSlot(EmiStack.EMPTY, inputX, slotY);
			}
			
			int outputX = column*18 + 110;
			if (i < outputCount)
			{
				widgets.addSlot(this.outputs.get(i), outputX, slotY);
			}
			else
			{
				widgets.addSlot(EmiStack.EMPTY, outputX, slotY).recipeContext(this);
			}
		}
	}

}
