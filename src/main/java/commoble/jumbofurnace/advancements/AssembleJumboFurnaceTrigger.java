package commoble.jumbofurnace.advancements;

import com.google.common.base.Predicates;
import com.google.gson.JsonObject;

import commoble.jumbofurnace.JumboFurnace;
import commoble.jumbofurnace.advancements.AssembleJumboFurnaceTrigger.AssembleJumboFurnaceCriterion;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

public class AssembleJumboFurnaceTrigger extends AbstractCriterionTrigger<AssembleJumboFurnaceCriterion>
{
	public static final ResourceLocation ID = new ResourceLocation(JumboFurnace.MODID, "assemble_jumbo_furnace");
	public static final AssembleJumboFurnaceTrigger INSTANCE = new AssembleJumboFurnaceTrigger();
	
	@Override
	public ResourceLocation getId()
	{
		return ID;
	}

	@Override
	protected AssembleJumboFurnaceCriterion deserializeTrigger(JsonObject json, AndPredicate entityPredicate, ConditionArrayParser conditionsParser)
	{
		return new AssembleJumboFurnaceCriterion(entityPredicate);
	}
	
	public void trigger(ServerPlayerEntity player)
	{
		this.triggerListeners(player, Predicates.alwaysTrue());
	}
	
	public static class AssembleJumboFurnaceCriterion extends CriterionInstance
	{

		public AssembleJumboFurnaceCriterion( AndPredicate playerCondition)
		{
			super(ID, playerCondition);
		}
		
	}
}
