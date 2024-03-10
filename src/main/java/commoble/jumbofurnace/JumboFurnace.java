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
import net.minecraft.advancements.CriterionTrigger;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.Event.Result;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.neoforged.neoforge.event.level.BlockEvent.EntityMultiPlaceEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(JumboFurnace.MODID)
public class JumboFurnace
{
	private static JumboFurnace instance;
	public static JumboFurnace get() { return instance; }
	
	public static final String MODID = "jumbofurnace";
	public static final TagKey<Block> JUMBOFURNACEABLE_TAG = TagKey.create(Registries.BLOCK, new ResourceLocation(MODID, "jumbofurnaceable"));
	public static final TagKey<Item> MULTIPROCESSING_UPGRADE_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(MODID, "multiprocessing_upgrade"));
	
	public final ServerConfig serverConfig;
	public final DeferredHolder<Block, JumboFurnaceBlock> jumboFurnaceBlock;
	public final DeferredHolder<Item, JumboFurnaceItem> jumboFurnaceItem;
	public final DeferredHolder<Item, Item> jumboFurnaceJeiDummy;
	public final DeferredHolder<BlockEntityType<?>, BlockEntityType<JumboFurnaceCoreBlockEntity>> jumboFurnaceCoreBlockEntityType;
	public final DeferredHolder<BlockEntityType<?>, BlockEntityType<JumboFurnaceExteriorBlockEntity>> jumboFurnaceExteriorBlockEntityType;
	public final DeferredHolder<MenuType<?>, MenuType<JumboFurnaceMenu>> jumboFurnaceMenuType;
	public final DeferredHolder<RecipeType<?>, RecipeType<JumboFurnaceRecipe>> jumboSmeltingRecipeType;
	public final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<JumboFurnaceRecipe>> jumboSmeltingRecipeSerializer;
	public final DeferredHolder<IngredientType<?>, IngredientType<TagStackIngredient>> tagStackIngredient;
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
		DeferredRegister<Block> blocks = makeDeferredRegister(modBus, Registries.BLOCK);
		DeferredRegister<Item> items = makeDeferredRegister(modBus, Registries.ITEM);
		DeferredRegister<BlockEntityType<?>> blockEntities = makeDeferredRegister(modBus, Registries.BLOCK_ENTITY_TYPE);
		DeferredRegister<MenuType<?>> menus = makeDeferredRegister(modBus, Registries.MENU);
		DeferredRegister<RecipeType<?>> recipeTypes = makeDeferredRegister(modBus, Registries.RECIPE_TYPE);
		DeferredRegister<RecipeSerializer<?>> recipeSerializers = makeDeferredRegister(modBus, Registries.RECIPE_SERIALIZER);
		DeferredRegister<IngredientType<?>> ingredientTypes = makeDeferredRegister(modBus, NeoForgeRegistries.Keys.INGREDIENT_TYPES);
		DeferredRegister<CriterionTrigger<?>> triggerTypes = makeDeferredRegister(modBus, Registries.TRIGGER_TYPE);
		
		this.jumboFurnaceBlock = blocks.register(Names.JUMBO_FURNACE, () -> new JumboFurnaceBlock(Block.Properties.ofFullCopy(Blocks.FURNACE)));
		
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
		
		this.tagStackIngredient = ingredientTypes.register(Names.TAG_STACK, () -> new IngredientType<>(TagStackIngredient.CODEC));
		
		this.assembleJumboFurnaceTrigger = triggerTypes.register(Names.ASSEMBLE_JUMBO_FURNACE, AssembleJumboFurnaceTrigger::new);
		this.upgradeJumboFurnaceTrigger = triggerTypes.register(Names.UPGRADE_JUMBO_FURNACE, UpgradeJumboFurnaceTrigger::new);
		
		modBus.addListener(this::onBuildCreativeTabs);
		modBus.addListener(this::onRegisterCapabilities);

		forgeBus.addListener(this::onAddServerReloadListeners);
		forgeBus.addListener(this::onEntityPlaceBlock);
		forgeBus.addListener(EventPriority.LOW, this::onRightClickBlockLow);
		
		if (FMLEnvironment.dist == Dist.CLIENT)
		{
			ClientProxy.addClientListeners(modBus, forgeBus);
		}
	}
	
	private static <T> DeferredRegister<T> makeDeferredRegister(IEventBus modBus, ResourceKey<Registry<T>> registryKey)
	{
		DeferredRegister<T> register = DeferredRegister.create(registryKey, MODID);
		register.register(modBus);
		return register;
	}
	
	private void onRegisterCapabilities(RegisterCapabilitiesEvent event)
	{
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, this.jumboFurnaceExteriorBlockEntityType.get(), (be, side) -> be.getItemHandler(side));
	}
	
	private void onAddServerReloadListeners(AddReloadListenerEvent event)
	{
		event.addListener(RecipeSorter.INSTANCE);
	}
	
	private void onEntityPlaceBlock(EntityPlaceEvent event)
	{
		BlockState state = event.getPlacedBlock();
		LevelAccessor levelAccess = event.getLevel();
		if (!(event instanceof EntityMultiPlaceEvent) && state.is(JumboFurnace.JUMBOFURNACEABLE_TAG) && levelAccess instanceof Level level)
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
						IItemHandler sideHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, newPos, dir);
						if (sideHandler != null)
						{
							addItemsToList(stacks, sideHandler);
						}
					}
					IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, newPos, null);
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
		if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
		{
			event.accept(this.jumboFurnaceItem.get());
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
