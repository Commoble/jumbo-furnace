package commoble.jumbofurnace;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;

public class JumboFurnaceUtils
{
	/** 
	 * @param stack Itemstack to be used as fuel for a jumbo furnace.
	 * @return If the itemstack provides a burn time specific to jumbo smelting, returns that.
	 * Otherwise, returns the burn time for vanilla furnace smelting.
	 */
	public static int getJumboSmeltingBurnTime(ItemStack stack)
	{
		int jumboSmeltingBurnTime = ForgeHooks.getBurnTime(stack, JumboFurnace.get().jumboSmeltingRecipeType.get());
		return jumboSmeltingBurnTime >= 0
			? jumboSmeltingBurnTime
			: ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
	}
}
