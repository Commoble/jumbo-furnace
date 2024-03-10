package commoble.jumbofurnace.client.jei;

import java.util.List;

import com.google.common.collect.Streams;

import commoble.jumbofurnace.JumboFurnace;
import commoble.jumbofurnace.client.jei.JumboFurnaceUpgradeCategory.JumboFurnaceUpgrade;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public class JumboFurnaceUpgradeCategory implements IRecipeCategory<JumboFurnaceUpgrade>
{
	public static enum JumboFurnaceUpgrade { INSTANCE }
	
	public static final RecipeType<JumboFurnaceUpgrade> TYPE = RecipeType.create(JumboFurnace.MODID, "jumbo_furnace_upgrade", JumboFurnaceUpgrade.class);

	private final IDrawable icon;
	private final IDrawable background;
	
	public JumboFurnaceUpgradeCategory(IGuiHelper helper)
	{
		this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(JumboFurnace.get().jumboFurnaceJeiDummy.get()));
		this.background = helper.drawableBuilder(JEIProxy.JEI_RECIPE_TEXTURE, 90, 74, 26, 26)
			.addPadding(0, 80, 45, 45)
			.build();
	}
	
	@Override
	public RecipeType<JumboFurnaceUpgrade> getRecipeType()
	{
		return TYPE;
	}

	@Override
	public Component getTitle()
	{
		return Component.translatable("gui.jumbofurnace.category.jumbo_furnace_upgrade");
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
	public void draw(JumboFurnaceUpgrade recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Font fontRenderer = minecraft.font;
		FormattedText text = Component.translatable("jumbofurnace.jumbo_furnace_upgrade_info");
		List<FormattedCharSequence> lines = fontRenderer.split(text, this.background.getWidth());
		int lineCount = lines.size();
		for (int i=0; i<lineCount; i++)
		{
			graphics.drawString(fontRenderer, lines.get(i), 0, 27 + i*9, 0xFF808080);
		}
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, JumboFurnaceUpgrade recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.CATALYST, 50, 5)
			.addItemStacks(Streams.stream(BuiltInRegistries.ITEM.getTagOrEmpty(JumboFurnace.MULTIPROCESSING_UPGRADE_TAG))
				.map(ItemStack::new)
				.toList());
	}
}
