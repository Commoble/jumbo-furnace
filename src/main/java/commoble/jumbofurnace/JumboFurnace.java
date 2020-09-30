package commoble.jumbofurnace;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import commoble.jumbofurnace.client.ClientEvents;
import commoble.jumbofurnace.config.ConfigHelper;
import commoble.jumbofurnace.config.ServerConfig;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceBlock;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceContainer;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceCoreTileEntity;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceExteriorTileEntity;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceItem;
import commoble.jumbofurnace.jumbo_furnace.MultiBlockHelper;
import commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import commoble.jumbofurnace.recipes.JumboFurnaceRecipeSerializer;
import commoble.jumbofurnace.recipes.RecipeSorter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.world.BlockEvent.EntityMultiPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod(JumboFurnace.MODID)
public class JumboFurnace
{
	public static final String MODID = "jumbofurnace";
	public static final IRecipeType<JumboFurnaceRecipe> JUMBO_SMELTING_RECIPE_TYPE = IRecipeType.register("jumbofurnace:jumbo_smelting");
	public static final ITag<Block> JUMBOFURNACEABLE_TAG = BlockTags.makeWrapperTag(MODID + ":" + "jumbofurnaceable");
	
	public static ServerConfig SERVER_CONFIG;
	
	public JumboFurnace()
	{
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		SERVER_CONFIG = ConfigHelper.register(ModConfig.Type.SERVER, ServerConfig::new);
		
		this.addModListeners(modBus);
		this.addForgeListeners(forgeBus);
		
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ClientEvents.addClientListeners(modBus, forgeBus);
		}
	}
	
	private void addModListeners(IEventBus modBus)
	{
		// register forge objects
		DeferredRegister<Block> blocks = this.makeDeferredRegister(modBus, ForgeRegistries.BLOCKS);
		DeferredRegister<Item> items = this.makeDeferredRegister(modBus, ForgeRegistries.ITEMS);
		DeferredRegister<TileEntityType<?>> tileEntities = this.makeDeferredRegister(modBus, ForgeRegistries.TILE_ENTITIES);
		DeferredRegister<ContainerType<?>> containers = this.makeDeferredRegister(modBus, ForgeRegistries.CONTAINERS);
		DeferredRegister<IRecipeSerializer<?>> recipeSerializers = this.makeDeferredRegister(modBus, ForgeRegistries.RECIPE_SERIALIZERS);
		
		blocks.register(Names.JUMBO_FURNACE, () -> new JumboFurnaceBlock(Block.Properties.from(Blocks.FURNACE)));
		
		items.register(Names.JUMBO_FURNACE, () -> new JumboFurnaceItem(new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
		
		items.register(Names.JUMBO_FURNACE_JEI, () -> new Item(new Item.Properties())
		{
			/**
			 * allows items to add custom lines of information to the mouseover description
			 */
			@Override
			@OnlyIn(Dist.CLIENT)
			public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
			{
				tooltip.add(new TranslationTextComponent("jumbofurnace.jumbo_furnace_info_tooltip"));
			}
		});
		
		tileEntities.register(Names.JUMBO_FURNACE_CORE,
			() -> TileEntityType.Builder.create(JumboFurnaceCoreTileEntity::new, JumboFurnaceObjects.BLOCK).build(null));
		tileEntities.register(Names.JUMBO_FURNACE_EXTERIOR,
			() -> TileEntityType.Builder.create(JumboFurnaceExteriorTileEntity::new, JumboFurnaceObjects.BLOCK).build(null));
		
		containers.register(Names.JUMBO_FURNACE, () -> new ContainerType<>(JumboFurnaceContainer::getClientContainer));
		
		recipeSerializers.register(Names.JUMBO_SMELTING, () -> new JumboFurnaceRecipeSerializer(JUMBO_SMELTING_RECIPE_TYPE));
	}
	
	private <T extends IForgeRegistryEntry<T>> DeferredRegister<T> makeDeferredRegister(IEventBus modBus, IForgeRegistry<T> registry)
	{
		DeferredRegister<T> register = DeferredRegister.create(registry, MODID);
		register.register(modBus);
		return register;
	}
	
	private void addForgeListeners(IEventBus forgeBus)
	{
		forgeBus.addListener(this::onAddServerReloadListeners);
		forgeBus.addListener(this::onEntityPlaceBlock);
	}
	
	private void onAddServerReloadListeners(AddReloadListenerEvent event)
	{
		event.addListener(RecipeSorter.INSTANCE);
	}
	
	private void onEntityPlaceBlock(EntityPlaceEvent event)
	{
		Block block = event.getPlacedBlock().getBlock();
		IWorld world = event.getWorld();
		if (!(event instanceof EntityMultiPlaceEvent) && JumboFurnace.JUMBOFURNACEABLE_TAG.contains(block) && world instanceof World)
		{
			BlockPos pos = event.getPos();
			BlockState againstState = event.getPlacedAgainst();
			Entity entity = event.getEntity();
			List<ItemStack> stacks = new ArrayList<>();
			
			// returns a non-empty list if we can make furnace
			List<Pair<BlockPos, BlockState>> pairs = MultiBlockHelper.getJumboFurnaceStates(((World)world).getDimensionKey(), world, pos, againstState, entity);
			for (Pair<BlockPos, BlockState> pair : pairs)
			{
				BlockPos newPos = pair.getFirst();
				BlockState newState = pair.getSecond();
				TileEntity te = world.getTileEntity(newPos);
				// attempt to remove items from existing itemhandlers if possible
				if (te != null)
				{
					for (Direction dir : Direction.values())
					{
						te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).ifPresent(handler -> addItemsToList(stacks, handler));
					}
					te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(handler -> addItemsToList(stacks, handler));
				}
				world.setBlockState(newPos, newState, 3);
			}
			if (!stacks.isEmpty())
			{
				if (entity instanceof PlayerEntity)
				{
					for (ItemStack stack : stacks)
					{
						((PlayerEntity)entity).addItemStackToInventory(stack);
					}
				}
				else
				{

					for (ItemStack stack : stacks)
					{
						entity.entityDropItem(stack);
					}
				}
			}
		}
	}
	
	private static void addItemsToList(List<ItemStack> stacks, IItemHandler handler)
	{
		int slots = handler.getSlots();
		for (int slot=0; slot<slots; slot++)
		{
			ItemStack stack = handler.extractItem(slot, 64, false);
			if (!stack.isEmpty())
			{
				stacks.add(stack);
			}
		}
	}
}
