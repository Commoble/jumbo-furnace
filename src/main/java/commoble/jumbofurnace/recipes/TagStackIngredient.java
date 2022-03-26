package commoble.jumbofurnace.recipes;

import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public class TagStackIngredient
{
	public static final IIngredientSerializer<Ingredient> SERIALIZER = new IIngredientSerializer<Ingredient>()
	{

		@Override
		public void write(FriendlyByteBuf buffer, Ingredient ingredient)
		{
			ItemStack[] items = ingredient.getItems();
			buffer.writeVarInt(items.length);	// tell the packet how long the array is
			for (ItemStack stack : items)
			{
				buffer.writeItem(stack);
			}
		}

		@Override
		public Ingredient parse(FriendlyByteBuf buffer)
		{
			return Ingredient.fromValues(Stream.generate(() -> new Ingredient.ItemValue(buffer.readItem())).limit(buffer.readVarInt()));
		}

		@Override
		public Ingredient parse(JsonObject json)
		{
			ResourceLocation tagID = new ResourceLocation(GsonHelper.getAsString(json, "tag")); // throws JsonSyntaxException if no tag field
			int count = GsonHelper.getAsInt(json, "count", 1);
			Tag<Item> tag = SerializationTags.getInstance().getTagOrThrow(Registry.ITEM_REGISTRY, tagID, id->
			{
				throw new JsonSyntaxException("Unknown item tag '" + id + "'"); // will get caught and logged during data loading
			});
			return Ingredient.fromValues(tag.getValues().stream().map(item -> new Ingredient.ItemValue(new ItemStack(item, count))));
		}

	};

}