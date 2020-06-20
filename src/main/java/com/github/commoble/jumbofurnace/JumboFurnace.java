package com.github.commoble.jumbofurnace;

import java.util.ArrayList;
import java.util.List;

import com.github.commoble.jumbofurnace.client.ClientEvents;
import com.github.commoble.jumbofurnace.config.ConfigHelper;
import com.github.commoble.jumbofurnace.config.ServerConfig;
import com.github.commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import com.github.commoble.jumbofurnace.recipes.JumboFurnaceRecipeSerializer;
import com.github.commoble.jumbofurnace.recipes.RecipeSorter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent.EntityMultiPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
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
		DeferredRegister<TileEntityType<?>> tileEntities = this.makeDeferredRegister(modBus, ForgeRegistries.TILE_ENTITIES);
		DeferredRegister<ContainerType<?>> containers = this.makeDeferredRegister(modBus, ForgeRegistries.CONTAINERS);
		DeferredRegister<IRecipeSerializer<?>> recipeSerializers = this.makeDeferredRegister(modBus, ForgeRegistries.RECIPE_SERIALIZERS);
		
		blocks.register(Names.JUMBO_FURNACE, () -> new JumboFurnaceBlock(Block.Properties.from(Blocks.FURNACE)));
		
		tileEntities.register(Names.JUMBO_FURNACE_CORE,
			() -> TileEntityType.Builder.create(JumboFurnaceCoreTileEntity::new, JumboFurnaceObjects.BLOCK).build(null));
		tileEntities.register(Names.JUMBO_FURNACE_EXTERIOR,
			() -> TileEntityType.Builder.create(JumboFurnaceExteriorTileEntity::new, JumboFurnaceObjects.BLOCK).build(null));
		
		containers.register(Names.JUMBO_FURNACE, () -> new ContainerType<>(JumboFurnaceContainer::getClientContainer));
		
		recipeSerializers.register(Names.JUMBO_SMELTING, () -> new JumboFurnaceRecipeSerializer(JUMBO_SMELTING_RECIPE_TYPE));
	}
	
	private <T extends IForgeRegistryEntry<T>> DeferredRegister<T> makeDeferredRegister(IEventBus modBus, IForgeRegistry<T> registry)
	{
		DeferredRegister<T> register = new DeferredRegister<>(registry, MODID);
		register.register(modBus);
		return register;
	}
	
	private void addForgeListeners(IEventBus forgeBus)
	{
		forgeBus.addListener(this::onServerStarting);
		forgeBus.addListener(this::onEntityPlaceBlock);
	}
	
	private void onServerStarting(FMLServerStartingEvent event)
	{
		event.getServer().getResourceManager().addReloadListener(RecipeSorter.INSTANCE);
	}
	
	private void onEntityPlaceBlock(EntityPlaceEvent event)
	{
		Block block = event.getPlacedBlock().getBlock();
		if (!(event instanceof EntityMultiPlaceEvent) && block == Blocks.FURNACE)
		{
			IWorld world = event.getWorld();
			BlockPos pos = event.getPos();
			BlockState againstState = event.getPlacedAgainst();
			Entity entity = event.getEntity();
			List<ItemStack> stacks = new ArrayList<>();
			
			// returns a non-empty list if we can make furnace
			List<BlockSnapshot> snapshots = MultiBlockHelper.getJumboFurnaceStates(world, pos, againstState, entity);
			for (BlockSnapshot snapshot : snapshots)
			{
				TileEntity te = world.getTileEntity(snapshot.getPos());
				te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).ifPresent(handler -> addItemsToList(stacks, handler));
				te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.DOWN).ifPresent(handler -> addItemsToList(stacks, handler));
				te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH).ifPresent(handler -> addItemsToList(stacks, handler));
				snapshot.getWorld().setBlockState(snapshot.getPos(), snapshot.getReplacedBlock(), 3);
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
