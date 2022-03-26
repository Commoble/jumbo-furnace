package commoble.jumbofurnace.advancements;

import com.google.gson.JsonObject;

import commoble.jumbofurnace.JumboFurnace;
import commoble.jumbofurnace.advancements.UpgradeJumboFurnaceTrigger.UpgradeJumboFurnaceCriterion;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;

public class UpgradeJumboFurnaceTrigger extends SimpleCriterionTrigger<UpgradeJumboFurnaceCriterion>
{
	public static final ResourceLocation ID = new ResourceLocation(JumboFurnace.MODID, "upgrade_jumbo_furnace");
	public static final UpgradeJumboFurnaceTrigger INSTANCE = new UpgradeJumboFurnaceTrigger();

	@Override
	public ResourceLocation getId()
	{
		return ID;
	}

	@Override
	protected UpgradeJumboFurnaceCriterion createInstance(JsonObject json, Composite entityPredicate, DeserializationContext conditionsParser)
	{
		ItemPredicate itemPredicate = ItemPredicate.fromJson(json.get("item"));
		return new UpgradeJumboFurnaceCriterion(entityPredicate, itemPredicate);
	}
	
	public void test(ServerPlayer player, ItemStack stack)
	{
		this.trigger(player, criterion -> criterion.test(stack));
	}
	
	public static class UpgradeJumboFurnaceCriterion extends AbstractCriterionTriggerInstance
	{
		private final ItemPredicate itemPredicate;

		public UpgradeJumboFurnaceCriterion( Composite playerCondition, ItemPredicate itemPredicate)
		{
			super(ID, playerCondition);
			this.itemPredicate = itemPredicate;
		}
		
		public boolean test(ItemStack stack)
		{
			return this.itemPredicate.matches(stack);
		}
	}
}
