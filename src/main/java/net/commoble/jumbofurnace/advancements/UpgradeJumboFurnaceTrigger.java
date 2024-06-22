package net.commoble.jumbofurnace.advancements;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.jumbofurnace.advancements.UpgradeJumboFurnaceTrigger.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class UpgradeJumboFurnaceTrigger extends SimpleCriterionTrigger<Criterion>
{
	public void test(ServerPlayer player, ItemStack stack)
	{
		this.trigger(player, criterion -> criterion.test(stack));
	}

	@Override
	public Codec<Criterion> codec()
	{
		return Criterion.CODEC;
	}
	
	public static record Criterion(Optional<ContextAwarePredicate> player, ItemPredicate itemPredicate) implements SimpleCriterionTrigger.SimpleInstance
	{
		public static final Codec<Criterion> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Criterion::player),
				ItemPredicate.CODEC.fieldOf("item").forGetter(Criterion::itemPredicate)
			).apply(builder, Criterion::new));
		
		public boolean test(ItemStack stack)
		{
			return this.itemPredicate.test(stack);
		}
	}
}
