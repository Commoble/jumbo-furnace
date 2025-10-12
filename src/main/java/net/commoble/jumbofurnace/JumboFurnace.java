package net.commoble.jumbofurnace;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import net.commoble.jumbofurnace.advancements.AssembleJumboFurnaceTrigger;
import net.commoble.jumbofurnace.advancements.UpgradeJumboFurnaceTrigger;
import net.commoble.jumbofurnace.client.ClientProxy;
import net.commoble.jumbofurnace.config.ConfigHelper;
import net.commoble.jumbofurnace.config.ServerConfig;
import net.commoble.jumbofurnace.jumbo_furnace.JumboFurnaceBlock;
import net.commoble.jumbofurnace.jumbo_furnace.JumboFurnaceCoreBlockEntity;
import net.commoble.jumbofurnace.jumbo_furnace.JumboFurnaceExteriorBlockEntity;
import net.commoble.jumbofurnace.jumbo_furnace.JumboFurnaceItem;
import net.commoble.jumbofurnace.jumbo_furnace.JumboFurnaceMenu;
import net.commoble.jumbofurnace.jumbo_furnace.MultiBlockHelper;
import net.commoble.jumbofurnace.recipes.InFlightRecipeSyncPacket;
import net.commoble.jumbofurnace.recipes.JumboFurnaceRecipe;
import net.commoble.jumbofurnace.recipes.RecipeSorter;
import net.commoble.jumbofurnace.recipes.SimpleRecipeSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.level.BlockEvent.EntityMultiPlaceEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@Mod(JumboFurnace.MODID)
public class JumboFurnace
{
	private static JumboFurnace instance;
	public static JumboFurnace get() { return instance; }
	
	public static final String MODID = "jumbofurnace";
	public static final TagKey<Block> JUMBOFURNACEABLE_TAG = TagKey.create(Registries.BLOCK, id("jumbofurnaceable"));
	public static final TagKey<Item> MULTIPROCESSING_UPGRADE_TAG = TagKey.create(Registries.ITEM, id("multiprocessing_upgrade"));
	
	public final ServerConfig serverConfig;
	public final DeferredHolder<Block, JumboFurnaceBlock> jumboFurnaceBlock;
	public final DeferredHolder<Item, JumboFurnaceItem> jumboFurnaceItem;
	public final DeferredHolder<Item, Item> jumboFurnaceJeiDummy;
	public final DeferredHolder<BlockEntityType<?>, BlockEntityType<JumboFurnaceCoreBlockEntity>> jumboFurnaceCoreBlockEntityType;
	public final DeferredHolder<BlockEntityType<?>, BlockEntityType<JumboFurnaceExteriorBlockEntity>> jumboFurnaceExteriorBlockEntityType;
	public final DeferredHolder<MenuType<?>, MenuType<JumboFurnaceMenu>> jumboFurnaceMenuType;
	public final DeferredHolder<RecipeType<?>, RecipeType<JumboFurnaceRecipe>> jumboSmeltingRecipeType;
	public final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<JumboFurnaceRecipe>> jumboSmeltingRecipeSerializer;
	public final DeferredHolder<CriterionTrigger<?>, AssembleJumboFurnaceTrigger> assembleJumboFurnaceTrigger;
	public final DeferredHolder<CriterionTrigger<?>, UpgradeJumboFurnaceTrigger> upgradeJumboFurnaceTrigger;
	
	public JumboFurnace(IEventBus modBus)
	{
		if (instance != null)
			throw new IllegalStateException("Jumbo Furnace initialized twice!");
		instance = this;
		
		IEventBus forgeBus = NeoForge.EVENT_BUS;
		
		this.serverConfig = ConfigHelper.register(ModConfig.Type.SERVER, ServerConfig::create);
		
		// register forge objects
		DeferredRegister.Blocks blocks = defreg(DeferredRegister::createBlocks);
		DeferredRegister.Items items = defreg(DeferredRegister::createItems);
		DeferredRegister<BlockEntityType<?>> blockEntities = defreg(Registries.BLOCK_ENTITY_TYPE);
		DeferredRegister<MenuType<?>> menus = defreg(Registries.MENU);
		DeferredRegister<RecipeType<?>> recipeTypes = defreg(Registries.RECIPE_TYPE);
		DeferredRegister<RecipeSerializer<?>> recipeSerializers = defreg(Registries.RECIPE_SERIALIZER);
		DeferredRegister<CriterionTrigger<?>> triggerTypes = defreg(Registries.TRIGGER_TYPE);
		
		this.jumboFurnaceBlock = blocks.registerBlock(Names.JUMBO_FURNACE, JumboFurnaceBlock::new, Block.Properties.ofFullCopy(Blocks.FURNACE));
		
		this.jumboFurnaceItem = items.registerItem(Names.JUMBO_FURNACE, JumboFurnaceItem::new);
		
		this.jumboFurnaceJeiDummy = items.registerItem(Names.JUMBO_FURNACE_JEI, props -> new Item(props)
		{
			/**
			 * allows items to add custom lines of information to the mouseover description
			 */
			@Override
			public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn)
			{
				tooltip.accept(Component.translatable("jumbofurnace.jumbo_furnace_info_tooltip")
					.withStyle(ChatFormatting.GRAY));
			}
		});
		
		this.jumboFurnaceCoreBlockEntityType = blockEntities.register(Names.JUMBO_FURNACE_CORE,
			() -> new BlockEntityType<>(JumboFurnaceCoreBlockEntity::create, this.jumboFurnaceBlock.get()));
		this.jumboFurnaceExteriorBlockEntityType = blockEntities.register(Names.JUMBO_FURNACE_EXTERIOR,
			() -> new BlockEntityType<>(JumboFurnaceExteriorBlockEntity::create, this.jumboFurnaceBlock.get()));
		
		this.jumboFurnaceMenuType = menus.register(Names.JUMBO_FURNACE, () -> new MenuType<>(JumboFurnaceMenu::getClientMenu, FeatureFlags.VANILLA_SET));
		
		this.jumboSmeltingRecipeType = recipeTypes.register(Names.JUMBO_SMELTING, () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MODID, Names.JUMBO_SMELTING)));
		
		this.jumboSmeltingRecipeSerializer = recipeSerializers.register(Names.JUMBO_SMELTING, () -> new SimpleRecipeSerializer<>(JumboFurnaceRecipe.CODEC, JumboFurnaceRecipe.STREAM_CODEC));
		
		this.assembleJumboFurnaceTrigger = triggerTypes.register(Names.ASSEMBLE_JUMBO_FURNACE, AssembleJumboFurnaceTrigger::new);
		this.upgradeJumboFurnaceTrigger = triggerTypes.register(Names.UPGRADE_JUMBO_FURNACE, UpgradeJumboFurnaceTrigger::new);
		
		modBus.addListener(this::onBuildCreativeTabs);
		modBus.addListener(this::onRegisterCapabilities);
		modBus.addListener(this::onRegisterPayloads);

		forgeBus.addListener(this::onAddServerReloadListeners);
		forgeBus.addListener(this::onEntityPlaceBlock);
		forgeBus.addListener(EventPriority.LOW, this::onRightClickBlockLow);
		
		if (FMLEnvironment.getDist().isClient())
		{
			ClientProxy.addClientListeners(modBus, forgeBus);
		}
	}
	
	private static <T> DeferredRegister<T> defreg(ResourceKey<Registry<T>> registryKey)
	{
		IEventBus modBus = ModList.get().getModContainerById(MODID).get().getEventBus();
		DeferredRegister<T> register = DeferredRegister.create(registryKey, MODID);
		register.register(modBus);
		return register;
	}
	
	private static <R extends DeferredRegister<?>> R defreg(Function<String,R> defregFactory)
	{
		IEventBus modBus = ModList.get().getModContainerById(MODID).get().getEventBus();
		R register = defregFactory.apply(MODID);
		register.register(modBus);
		return register;
	}
	
	private void onRegisterCapabilities(RegisterCapabilitiesEvent event)
	{
		event.registerBlockEntity(Capabilities.Item.BLOCK, this.jumboFurnaceExteriorBlockEntityType.get(), (be, side) -> be.getItemHandler(side));
	}
	
	private void onAddServerReloadListeners(AddServerReloadListenersEvent event)
	{
		event.addListener(JumboFurnace.id("recipe_sorter"), RecipeSorter.SERVER_INSTANCE);
	}
	
	private void onRegisterPayloads(RegisterPayloadHandlersEvent event)
	{
		var registrar = event.registrar("1");
		registrar.playToClient(InFlightRecipeSyncPacket.TYPE, InFlightRecipeSyncPacket.STREAM_CODEC, InFlightRecipeSyncPacket::handle);
	}
	
	private void onEntityPlaceBlock(EntityPlaceEvent event)
	{
		BlockState state = event.getPlacedBlock();
		LevelAccessor levelAccess = event.getLevel();
		if (!Transaction.hasActiveTransaction() && !(event instanceof EntityMultiPlaceEvent) && state.is(JumboFurnace.JUMBOFURNACEABLE_TAG) && levelAccess instanceof ServerLevel level)
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
					// attempt to remove items from existing itemhandlers if possible

					for (Direction dir : Direction.values())
					{
						@Nullable ResourceHandler<ItemResource> sideHandler = level.getCapability(Capabilities.Item.BLOCK, newPos, dir);
						if (sideHandler != null)
						{
							addItemsToList(stacks, sideHandler);
						}
					}
					@Nullable ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, newPos, null);
					if (handler != null)
					{
						addItemsToList(stacks, handler);
					}
					levelAccess.setBlock(newPos, newState, 3);
				}
				if (entity instanceof ServerPlayer)
				{
					this.assembleJumboFurnaceTrigger.get().trigger((ServerPlayer)entity);
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
						entity.spawnAtLocation(level, stack);
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
		if (this.serverConfig.allowShearing().get() && event.getUseItem() != TriState.FALSE && event.getUseBlock() != TriState.FALSE)
		{
			Player player = event.getEntity();
			ItemStack stack = event.getItemStack();
			if (player.isSecondaryUseActive() && stack.is(Tags.Items.TOOLS_SHEAR))
			{
				Level level = event.getLevel();
				BlockPos pos = event.getPos();
				BlockState state = level.getBlockState(pos);
				if (state.getBlock() == this.jumboFurnaceBlock.get())
				{
					// we used shears on a jumbo furnace while sneaking -- event will now be cancelled/overridden
					
					// only make changes to world on server (blocks, itemstacks, entities, etc)
					if (!level.isClientSide())
					{
						BlockPos corePos = JumboFurnaceBlock.getCorePos(state, pos);
						// forge fires a RightClickBlock event before this is called, we can assume that this would fail if the player didn't have
						// permission to use items on the block
			            Block.popResource(level, pos, new ItemStack(this.jumboFurnaceItem.get()));
			            InteractionHand hand = event.getHand();
			            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
						
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
		if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
		{
			event.accept(this.jumboFurnaceItem.get());
		}
	}
	
	private static void addItemsToList(List<ItemStack> stacks, ResourceHandler<ItemResource> handler)
	{
		int slots = handler.size();
		for (int slot=0; slot<slots; slot++)
		{
			ItemStack stack = JumboFurnaceUtils.extractImmediate(slot, handler, handler.getAmountAsInt(slot));
			if (!stack.isEmpty())
			{
				stacks.add(stack);
			}
		}
	}
	
	public static ResourceLocation id(String path)
	{
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}
}
