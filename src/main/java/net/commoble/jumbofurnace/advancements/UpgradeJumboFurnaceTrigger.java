package net.commoble.jumbofurnace.advancements;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.commoble.jumbofurnace.advancements.UpgradeJumboFurnaceTrigger.Criterion;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

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
	
	public static record Criterion(Optional<Holder<LootItemCondition>> player, ItemPredicate itemPredicate) implements SimpleCriterionTrigger.SimpleInstance
	{
		public static final Codec<Criterion> CODEC = RecordCodecBuilder.create(builder -> builder.group(
				LootItemCondition.CODEC.optionalFieldOf("player").forGetter(Criterion::player),
				ItemPredicate.CODEC.fieldOf("item").forGetter(Criterion::itemPredicate)
			).apply(builder, Criterion::new));
		
		public boolean test(ItemStack stack)
		{
			return this.itemPredicate.test(stack);
		}
	}
}
