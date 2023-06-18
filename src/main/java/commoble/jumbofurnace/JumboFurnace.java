package commoble.jumbofurnace;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import commoble.jumbofurnace.advancements.AssembleJumboFurnaceTrigger;
import commoble.jumbofurnace.advancements.UpgradeJumboFurnaceTrigger;
import commoble.jumbofurnace.client.ClientProxy;
import commoble.jumbofurnace.config.ConfigHelper;
import commoble.jumbofurnace.config.ServerConfig;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceBlock;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceCoreBlockEntity;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceExteriorBlockEntity;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceItem;
import commoble.jumbofurnace.jumbo_furnace.JumboFurnaceMenu;
import commoble.jumbofurnace.jumbo_furnace.MultiBlockHelper;
import commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import commoble.jumbofurnace.recipes.JumboFurnaceRecipeSerializer;
import commoble.jumbofurnace.recipes.RecipeSorter;
import commoble.jumbofurnace.recipes.TagStackIngredient;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent.EntityMultiPlaceEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(JumboFurnace.MODID)
public class JumboFurnace
{
	private static JumboFurnace instance;
	public static JumboFurnace get() { return instance; }
	
	public static final String MODID = "jumbofurnace";
	public static final TagKey<Block> JUMBOFURNACEABLE_TAG = TagKey.create(Registries.BLOCK, new ResourceLocation(MODID, "jumbofurnaceable"));
	public static final TagKey<Item> MULTIPROCESSING_UPGRADE_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(MODID, "multiprocessing_upgrade"));
	
	public final ServerConfig serverConfig;
	public final RegistryObject<JumboFurnaceBlock> jumboFurnaceBlock;
	public final RegistryObject<JumboFurnaceItem> jumboFurnaceItem;
	public final RegistryObject<Item> jumboFurnaceJeiDummy;
	public final RegistryObject<BlockEntityType<JumboFurnaceCoreBlockEntity>> jumboFurnaceCoreBlockEntityType;
	public final RegistryObject<BlockEntityType<JumboFurnaceExteriorBlockEntity>> jumboFurnaceExteriorBlockEntityType;
	public final RegistryObject<MenuType<JumboFurnaceMenu>> jumboFurnaceMenuType;
	public final RegistryObject<RecipeType<JumboFurnaceRecipe>> jumboSmeltingRecipeType;
	public final RegistryObject<RecipeSerializer<JumboFurnaceRecipe>> jumboSmeltingRecipeSerializer;
	
	public JumboFurnace()
	{
		if (instance != null)
			throw new IllegalStateException("Jumbo Furnace initialized twice!");
		instance = this;
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		
		this.serverConfig = ConfigHelper.register(ModConfig.Type.SERVER, ServerConfig::create);
		
		// register forge objects
		DeferredRegister<Block> blocks = this.makeDeferredRegister(modBus, Registries.BLOCK);
		DeferredRegister<Item> items = this.makeDeferredRegister(modBus, Registries.ITEM);
		DeferredRegister<BlockEntityType<?>> blockEntities = this.makeDeferredRegister(modBus, Registries.BLOCK_ENTITY_TYPE);
		DeferredRegister<MenuType<?>> menus = this.makeDeferredRegister(modBus, Registries.MENU);
		DeferredRegister<RecipeType<?>> recipeTypes = this.makeDeferredRegister(modBus, Registries.RECIPE_TYPE);
		DeferredRegister<RecipeSerializer<?>> recipeSerializers = this.makeDeferredRegister(modBus, Registries.RECIPE_SERIALIZER);
		
		this.jumboFurnaceBlock = blocks.register(Names.JUMBO_FURNACE, () -> new JumboFurnaceBlock(Block.Properties.copy(Blocks.FURNACE)));
		
		this.jumboFurnaceItem = items.register(Names.JUMBO_FURNACE, () -> new JumboFurnaceItem(new Item.Properties()));
		
		this.jumboFurnaceJeiDummy = items.register(Names.JUMBO_FURNACE_JEI, () -> new Item(new Item.Properties())
		{
			/**
			 * allows items to add custom lines of information to the mouseover description
			 */
			@Override
			@OnlyIn(Dist.CLIENT)
			public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
			{
				tooltip.add(Component.translatable("jumbofurnace.jumbo_furnace_info_tooltip"));
			}
		});
		
		this.jumboFurnaceCoreBlockEntityType = blockEntities.register(Names.JUMBO_FURNACE_CORE,
			() -> BlockEntityType.Builder.of(JumboFurnaceCoreBlockEntity::create, this.jumboFurnaceBlock.get()).build(null));
		this.jumboFurnaceExteriorBlockEntityType = blockEntities.register(Names.JUMBO_FURNACE_EXTERIOR,
			() -> BlockEntityType.Builder.of(JumboFurnaceExteriorBlockEntity::create, this.jumboFurnaceBlock.get()).build(null));
		
		this.jumboFurnaceMenuType = menus.register(Names.JUMBO_FURNACE, () -> new MenuType<>(JumboFurnaceMenu::getClientMenu, FeatureFlags.VANILLA_SET));
		
		this.jumboSmeltingRecipeType = recipeTypes.register(Names.JUMBO_SMELTING, () -> RecipeType.simple(new ResourceLocation(MODID, Names.JUMBO_SMELTING)));
		
		this.jumboSmeltingRecipeSerializer = recipeSerializers.register(Names.JUMBO_SMELTING, () -> new JumboFurnaceRecipeSerializer(this.jumboSmeltingRecipeType.get()));
		
		modBus.addListener(this::onBuildCreativeTabs);
		modBus.addListener(this::onCommonSetup);

		forgeBus.addListener(this::onAddServerReloadListeners);
		forgeBus.addListener(this::onEntityPlaceBlock);
		forgeBus.addListener(EventPriority.LOW, this::onRightClickBlockLow);
		
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ClientProxy.addClientListeners(modBus, forgeBus);
		}
	}
	
	private <T> DeferredRegister<T> makeDeferredRegister(IEventBus modBus, ResourceKey<Registry<T>> registryKey)
	{
		DeferredRegister<T> register = DeferredRegister.create(registryKey, MODID);
		register.register(modBus);
		return register;
	}
	
	private void onAddServerReloadListeners(AddReloadListenerEvent event)
	{
		event.addListener(RecipeSorter.INSTANCE);
	}
	
	private void onEntityPlaceBlock(EntityPlaceEvent event)
	{
		BlockState state = event.getPlacedBlock();
		LevelAccessor levelAccess = event.getLevel();
		if (!(event instanceof EntityMultiPlaceEvent) && state.is(JumboFurnace.JUMBOFURNACEABLE_TAG) && levelAccess instanceof Level)
		{
			BlockPos pos = event.getPos();
			BlockState againstState = event.getPlacedAgainst();
			Entity entity = event.getEntity();
			List<ItemStack> stacks = new ArrayList<>();
			
			// returns a non-empty list if we can make furnace
			List<Pair<BlockPos, BlockState>> pairs = MultiBlockHelper.getJumboFurnaceStates(((Level)levelAccess).dimension(), levelAccess, pos, againstState, entity);
			if (pairs.size() > 0)
			{
				for (Pair<BlockPos, BlockState> pair : pairs)
				{
					BlockPos newPos = pair.getFirst();
					BlockState newState = pair.getSecond();
					BlockEntity te = levelAccess.getBlockEntity(newPos);
					// attempt to remove items from existing itemhandlers if possible
					if (te != null)
					{
						for (Direction dir : Direction.values())
						{
							te.getCapability(ForgeCapabilities.ITEM_HANDLER, dir).ifPresent(handler -> addItemsToList(stacks, handler));
						}
						te.getCapability(ForgeCapabilities.ITEM_HANDLER, null).ifPresent(handler -> addItemsToList(stacks, handler));
					}
					levelAccess.setBlock(newPos, newState, 3);
				}
				if (entity instanceof ServerPlayer)
				{
					AssembleJumboFurnaceTrigger.INSTANCE.trigger((ServerPlayer)entity);
				}
			}
			if (!stacks.isEmpty())
			{
				if (entity instanceof Player)
				{
					for (ItemStack stack : stacks)
					{
						((Player)entity).addItem(stack);
					}
				}
				else
				{

					for (ItemStack stack : stacks)
					{
						entity.spawnAtLocation(stack);
					}
				}
			}
		}
	}
	
	// the idea here is that we want using-shears-on-jumbo-furnace-while-sneaking to drop the jumbo furnace as jumbo furnace item
	// without causing the sub-blocks to recursively destroy themselves or drop themselves as regular furnaces
	// we can't cause behaviour to happen when use-shears-on-block-while-sneaking from the block's activation method
	// but we can "override" both the block and item's behaviour if we do something in this event and then cancel it
	// we subscribe on low priority because we want to allow standard events to deny block or item behaviour
	// but we also want events to run if we don't cancel it
	private void onRightClickBlockLow(RightClickBlock event)
	{
		// if block or item usage is denied, do nothing
		if (this.serverConfig.allowShearing().get() && event.getUseItem() != Result.DENY && event.getUseBlock() != Result.DENY)
		{
			Player player = event.getEntity();
			ItemStack stack = event.getItemStack();
			if (player.isSecondaryUseActive() && stack.is(Tags.Items.SHEARS))
			{
				Level level = event.getLevel();
				BlockPos pos = event.getPos();
				BlockState state = level.getBlockState(pos);
				if (state.getBlock() == this.jumboFurnaceBlock.get())
				{
					// we used shears on a jumbo furnace while sneaking -- event will now be cancelled/overridden
					
					// only make changes to world on server (blocks, itemstacks, entities, etc)
					if (!level.isClientSide)
					{
						BlockPos corePos = JumboFurnaceBlock.getCorePos(state, pos);
						// forge fires a RightClickBlock event before this is called, we can assume that this would fail if the player didn't have
						// permission to use items on the block
			            Block.popResource(level, pos, new ItemStack(this.jumboFurnaceItem.get()));
			            InteractionHand hand = event.getHand();
			            stack.hurtAndBreak(1, player, (playerEntity) -> {
			               playerEntity.broadcastBreakEvent(hand);
			            });
						
						MultiBlockHelper.get3x3CubeAround(corePos)
							.forEach(componentPos ->
								level.removeBlock(componentPos, true));	// use isMoving flag to prevent recursive destruction from occurring or dropping blocks
					}
					level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.SHEEP_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);
									
					event.setCanceled(true);
					event.setCancellationResult(InteractionResult.SUCCESS);
				}
			}
		}
	}
	
	private void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event)
	{
		if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
		{
			event.accept(this.jumboFurnaceItem);
		}
	}
	
	private void onCommonSetup(FMLCommonSetupEvent event)
	{
		event.enqueueWork(() -> this.afterCommonSetup());
	}
	
	// runs on main thread after common setup event
	// adding things to unsynchronized registries (i.e. most vanilla registries) can be done here
	private void afterCommonSetup()
	{
		CriteriaTriggers.register(AssembleJumboFurnaceTrigger.INSTANCE);
		CriteriaTriggers.register(UpgradeJumboFurnaceTrigger.INSTANCE);
		CraftingHelper.register(new ResourceLocation("jumbofurnace:tag_stack"), TagStackIngredient.SERIALIZER);
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
