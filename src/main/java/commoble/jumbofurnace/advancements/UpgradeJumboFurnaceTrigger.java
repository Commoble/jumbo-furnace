package commoble.jumbofurnace.advancements;

import com.google.gson.JsonObject;

import commoble.jumbofurnace.JumboFurnace;
import commoble.jumbofurnace.advancements.UpgradeJumboFurnaceTrigger.UpgradeJumboFurnaceCriterion;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate.AndPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

public class UpgradeJumboFurnaceTrigger extends AbstractCriterionTrigger<UpgradeJumboFurnaceCriterion>
{
	public static final ResourceLocation ID = new ResourceLocation(JumboFurnace.MODID, "upgrade_jumbo_furnace");
	public static final UpgradeJumboFurnaceTrigger INSTANCE = new UpgradeJumboFurnaceTrigger();

	@Override
	public ResourceLocation getId()
	{
		return ID;
	}

	@Override
	protected UpgradeJumboFurnaceCriterion deserializeTrigger(JsonObject json, AndPredicate entityPredicate, ConditionArrayParser conditionsParser)
	{
		ItemPredicate itemPredicate = ItemPredicate.deserialize(json.get("item"));
		return new UpgradeJumboFurnaceCriterion(entityPredicate, itemPredicate);
	}
	
	public void test(ServerPlayerEntity player, ItemStack stack)
	{
		this.triggerListeners(player, criterion -> criterion.test(stack));
	}
	
	public static class UpgradeJumboFurnaceCriterion extends CriterionInstance
	{
		private final ItemPredicate itemPredicate;

		public UpgradeJumboFurnaceCriterion( AndPredicate playerCondition, ItemPredicate itemPredicate)
		{
			super(ID, playerCondition);
			this.itemPredicate = itemPredicate;
		}
		
		public boolean test(ItemStack stack)
		{
			return this.itemPredicate.test(stack);
		}
	}
}
