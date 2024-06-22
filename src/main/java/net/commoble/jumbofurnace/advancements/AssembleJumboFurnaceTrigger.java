package net.commoble.jumbofurnace.advancements;

import java.util.Optional;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;

import net.commoble.jumbofurnace.advancements.AssembleJumboFurnaceTrigger.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class AssembleJumboFurnaceTrigger extends SimpleCriterionTrigger<Criterion>
{
	@Override
	public Codec<Criterion> codec()
	{
		return Criterion.CODEC;
	}
	
	public void trigger(ServerPlayer player)
	{
		this.trigger(player, Predicates.alwaysTrue());
	}

	public static record Criterion(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance
	{
		public static final Codec<Criterion> CODEC = EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player")
			.xmap(Criterion::new, Criterion::player)
			.codec();
	}

}
