package net.commoble.jumbofurnace.recipes;

import java.util.List;

import net.commoble.jumbofurnace.JumboFurnace;
import net.commoble.jumbofurnace.jumbo_furnace.JumboFurnaceMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record InFlightRecipeSyncPacket(List<InFlightRecipe> recipes) implements CustomPacketPayload
{
	public static final Identifier ID = JumboFurnace.id("inflight_recipe_sync");
	public static final CustomPacketPayload.Type<InFlightRecipeSyncPacket> TYPE = new CustomPacketPayload.Type<>(ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, InFlightRecipeSyncPacket> STREAM_CODEC = InFlightRecipe.STREAM_CODEC
		.apply(ByteBufCodecs.list())
		.map(InFlightRecipeSyncPacket::new, InFlightRecipeSyncPacket::recipes);
	
	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}

	public void handle(IPayloadContext context)
	{
		if (context.player().containerMenu instanceof JumboFurnaceMenu menu)
		{
			menu.updateRecipes(this.recipes);
		}
	}
}
