package net.commoble.jumbofurnace.recipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.jumbofurnace.JumboFurnace;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

public class TagStackIngredient extends Ingredient
{
	public static final Codec<TagStackIngredient> CODEC = RecordCodecBuilder.create(builder -> builder.group(
		TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagStackIngredient::tag),
		Codec.INT.optionalFieldOf("count", 1).forGetter(TagStackIngredient::count)
	).apply(builder, TagStackIngredient::new));
	
	private final TagKey<Item> tag;	public TagKey<Item> tag() { return this.tag; }
	private final int count;	public int count() { return this.count; }
	
	public TagStackIngredient(TagKey<Item> tag, int count)
	{
		super(Stream.of(new TagCountValue(tag, count)), JumboFurnace.get().tagStackIngredient);
		this.tag = tag;
		this.count = count;
	}
	
	public static record TagCountValue(TagKey<Item> tag, int count) implements Ingredient.Value
	{		
		@Override
		public Collection<ItemStack> getItems()
		{
			var list = new ArrayList<ItemStack>();
			for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag))
			{
				list.add(new ItemStack(holder.value(), this.count));
			}

            if (list.size() == 0) {
                list.add(new ItemStack(Blocks.BARRIER).setHoverName(Component.literal("Empty Tag: " + this.tag.location())));
            }
            return list;
		}
	}


}