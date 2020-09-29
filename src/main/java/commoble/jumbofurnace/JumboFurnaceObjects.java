package commoble.jumbofurnace;

import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceBlock;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceContainer;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceCoreTileEntity;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceExteriorTileEntity;
import commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(JumboFurnace.MODID)
public class JumboFurnaceObjects
{
	@ObjectHolder(Names.JUMBO_FURNACE)
	public static final JumboFurnaceBlock BLOCK = null;
	
	@ObjectHolder(Names.JUMBO_FURNACE_EXTERIOR)
	public static final TileEntityType<JumboFurnaceExteriorTileEntity> EXTERIOR_TE_TYPE = null;
	
	@ObjectHolder(Names.JUMBO_FURNACE_CORE)
	public static final TileEntityType<JumboFurnaceCoreTileEntity> CORE_TE_TYPE = null;
	
	@ObjectHolder(Names.JUMBO_FURNACE)
	public static final ContainerType<JumboFurnaceContainer> CONTAINER_TYPE = null;
	
	@ObjectHolder(Names.JUMBO_SMELTING)
	public static final IRecipeSerializer<JumboFurnaceRecipe> RECIPE_SERIALIZER = null;
	
	@ObjectHolder(Names.JUMBO_FURNACE_JEI)
	public static final Item JEI_DUMMY = null;
}
