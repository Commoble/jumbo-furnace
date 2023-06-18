package commoble.jumbofurnace.advancements;

import com.google.common.base.Predicates;
import com.google.gson.JsonObject;

import commoble.jumbofurnace.JumboFurnace;
import commoble.jumbofurnace.advancements.AssembleJumboFurnaceTrigger.AssembleJumboFurnaceCriterion;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class AssembleJumboFurnaceTrigger extends SimpleCriterionTrigger<AssembleJumboFurnaceCriterion>
{
	public static final ResourceLocation ID = new ResourceLocation(JumboFurnace.MODID, "assemble_jumbo_furnace");
	public static final AssembleJumboFurnaceTrigger INSTANCE = new AssembleJumboFurnaceTrigger();
	
	@Override
	public ResourceLocation getId()
	{
		return ID;
	}

	@Override
	protected AssembleJumboFurnaceCriterion createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext conditionsParser)
	{
		return new AssembleJumboFurnaceCriterion(predicate);
	}
	
	public void trigger(ServerPlayer player)
	{
		this.trigger(player, Predicates.alwaysTrue());
	}
	
	public static class AssembleJumboFurnaceCriterion extends AbstractCriterionTriggerInstance
	{

		public AssembleJumboFurnaceCriterion(ContextAwarePredicate predicate)
		{
			super(ID, predicate);
		}
		
	}
}
