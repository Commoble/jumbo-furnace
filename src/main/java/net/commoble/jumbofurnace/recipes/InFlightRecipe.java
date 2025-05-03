package net.commoble.jumbofurnace.recipes;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class InFlightRecipe
{
	public static final Codec<InFlightRecipe> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			JumboFurnaceRecipe.CODEC.fieldOf("recipe").forGetter(InFlightRecipe::recipe),
			ItemStack.CODEC.listOf().fieldOf("inputs").forGetter(InFlightRecipe::inputs),
			Codec.INT.fieldOf("progress").forGetter(InFlightRecipe::progress)
		).apply(builder, InFlightRecipe::new));
	
	public static final StreamCodec<RegistryFriendlyByteBuf, InFlightRecipe> STREAM_CODEC = StreamCodec.composite(
		JumboFurnaceRecipe.STREAM_CODEC, InFlightRecipe::recipe,
		ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), InFlightRecipe::inputs,
		ByteBufCodecs.INT, InFlightRecipe::progress,
		InFlightRecipe::new);
	
	private final JumboFurnaceRecipe recipe;
	private final List<ItemStack> inputs;
	private int progress = 0;
	
	public InFlightRecipe(JumboFurnaceRecipe recipe, List<ItemStack> inputs)
	{
		this(recipe, inputs, 0);
	}
	
	public InFlightRecipe(JumboFurnaceRecipe recipe, List<ItemStack> inputs, int progress)
	{
		this.recipe = recipe;
		this.inputs = inputs;
		this.progress = progress;
	}
	
	public JumboFurnaceRecipe recipe()
	{
		return this.recipe;
	}
	
	public List<ItemStack> inputs()
	{
		return this.inputs;
	}
	
	public int progress()
	{
		return this.progress;
	}
	
	public boolean incrementProgress()
	{
		this.progress++;
		return this.progress >= recipe.cookingTime();
	}
}
