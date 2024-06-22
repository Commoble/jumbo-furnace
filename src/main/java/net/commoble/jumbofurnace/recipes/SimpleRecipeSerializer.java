package net.commoble.jumbofurnace.recipes;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

public record SimpleRecipeSerializer<INPUT extends RecipeInput, RECIPE extends Recipe<INPUT>>(MapCodec<RECIPE> codec, StreamCodec<RegistryFriendlyByteBuf, RECIPE> streamCodec)  implements RecipeSerializer<RECIPE>
{}
