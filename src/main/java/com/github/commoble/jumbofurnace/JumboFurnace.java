package com.github.commoble.jumbofurnace;

import com.github.commoble.jumbofurnace.client.ClientEvents;
import com.github.commoble.jumbofurnace.recipes.JumboFurnaceRecipeSerializer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.EntityMultiPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod(JumboFurnace.MODID)
public class JumboFurnace
{
	public static final String MODID = "jumbofurnace";
	
	public JumboFurnace()
	{
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
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
		
		recipeSerializers.register(Names.JUMBO_FURNACE, () -> new JumboFurnaceRecipeSerializer());
	}
	
	private <T extends IForgeRegistryEntry<T>> DeferredRegister<T> makeDeferredRegister(IEventBus modBus, IForgeRegistry<T> registry)
	{
		DeferredRegister<T> register = new DeferredRegister<>(registry, MODID);
		register.register(modBus);
		return register;
	}
	
	private void addForgeListeners(IEventBus forgeBus)
	{
		forgeBus.addListener(this::onEntityPlaceBlock);
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
			MultiBlockHelper.getJumboFurnaceStates(world, pos, againstState, entity) // returns a non-empty list if we can make furnace
				.forEach(snapshot -> snapshot.getWorld().setBlockState(snapshot.getPos(), snapshot.getReplacedBlock(), 3));
		}
	}
}
