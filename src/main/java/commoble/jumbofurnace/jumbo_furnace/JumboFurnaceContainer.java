package com.github.commoble.jumbofurnace.jumbo_furnace;

import com.github.commoble.jumbofurnace.JumboFurnace;
import com.github.commoble.jumbofurnace.JumboFurnaceObjects;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class JumboFurnaceContainer extends Container
{
	public static final ITextComponent TITLE = new TranslationTextComponent("container.jumbofurnace.jumbo_furnace");
	
	// slot positions
	public static final int SLOT_SPACING = 18;
	public static final int INPUT_START_X = 8;
	public static final int INPUT_START_Y = 18;
	public static final int FUEL_START_X = INPUT_START_X;
	public static final int FUEL_START_Y = 90;
	public static final int OUTPUT_START_X = 116;
	public static final int OUTPUT_START_Y = 53;
	public static final int BACKPACK_START_X = INPUT_START_X;
	public static final int BACKPACK_START_Y = 158;
	public static final int HOTBAR_START_X = INPUT_START_X;
	public static final int HOTBAR_START_Y = 216;
	
	// slot counts
	public static final int SLOT_ROWS = 3;
	public static final int SLOT_COLUMNS = 3;
	public static final int BACKPACK_ROWS = 3;
	public static final int PLAYER_COLUMNS = 9;
	public static final int INPUT_SLOTS = SLOT_ROWS * SLOT_COLUMNS;
	public static final int BACKPACK_SLOTS = BACKPACK_ROWS * PLAYER_COLUMNS;
	public static final int HOTBAR_SLOTS = PLAYER_COLUMNS;
	
	// slot indices
	public static final int FIRST_INPUT_SLOT = 0;
	public static final int FIRST_FUEL_SLOT = FIRST_INPUT_SLOT + INPUT_SLOTS;
	public static final int FIRST_OUTPUT_SLOT = FIRST_FUEL_SLOT + INPUT_SLOTS;
	public static final int FIRST_HOTBAR_SLOT = FIRST_OUTPUT_SLOT + INPUT_SLOTS;
	public static final int FIRST_BACKPACK_SLOT = FIRST_HOTBAR_SLOT + HOTBAR_SLOTS;
	public static final int FIRST_PLAYER_SLOT = FIRST_HOTBAR_SLOT;

	public static final int END_INPUT_SLOTS = FIRST_INPUT_SLOT + INPUT_SLOTS;
	public static final int END_FUEL_SLOTS = FIRST_FUEL_SLOT + INPUT_SLOTS;
	public static final int END_PLAYER_SLOTS = FIRST_BACKPACK_SLOT + BACKPACK_SLOTS;
	
	/** Used by the Server to determine whether the player is close enough to use the Container **/
	private final IWorldPosCallable usabilityTest;
	private final PlayerEntity player;
	private final IIntArray furnaceData;

	/** Container factory for opening the container clientside **/
	public static JumboFurnaceContainer getClientContainer(int id, PlayerInventory playerInventory)
	{
		// init client inventory with dummy slots
		return new JumboFurnaceContainer(id, playerInventory, BlockPos.ZERO, new ItemStackHandler(9), new ItemStackHandler(9), new UninsertableItemStackHandler(9), new IntArray(4));
	}
	
	/**
	 * Get the server container provider for NetworkHooks.openGui
	 * @param te The TileEntity of the furnace core
	 * @param activationPos The position of the block that the player actually activated to open the container (may be different than te.getPos)
	 * @return
	 */
	public static IContainerProvider getServerContainerProvider(JumboFurnaceCoreTileEntity te, BlockPos activationPos)
	{
		return (id, playerInventory, serverPlayer) -> new JumboFurnaceContainer(id, playerInventory, activationPos, te.input, te.fuel, te.output, new JumboFurnaceSyncData(te));
	}
	
	protected JumboFurnaceContainer(int id, PlayerInventory playerInventory, BlockPos pos, IItemHandler inputs, IItemHandler fuel, IItemHandler outputs, IIntArray furnaceData)
	{
		super(JumboFurnaceObjects.CONTAINER_TYPE, id);
		
		this.player = playerInventory.player;
		this.usabilityTest = IWorldPosCallable.of(this.player.world, pos);
		this.furnaceData = furnaceData;
		
		// add input slots
		for (int row=0; row < SLOT_ROWS; row++)
		{
			int y = INPUT_START_Y + SLOT_SPACING*row;
			for (int column=0; column < SLOT_COLUMNS; column++)
			{
				int x = INPUT_START_X + SLOT_SPACING*column;
				int index = row * SLOT_COLUMNS + column;
				this.addSlot(new SlotItemHandler(inputs, index, x, y));
			}
		}
		
		// add fuel slots
		for (int row=0; row < SLOT_ROWS; row++)
		{
			int y = FUEL_START_Y + SLOT_SPACING*row;
			for (int column=0; column < SLOT_COLUMNS; column++)
			{
				int x = FUEL_START_X + SLOT_SPACING*column;
				int index = row * SLOT_COLUMNS + column;
				this.addSlot(new JumboFurnaceFuelSlot(fuel, index, x, y));
			}
		}
		
		// add output slots
		for (int row=0; row < SLOT_ROWS; row++)
		{
			int y = OUTPUT_START_Y + SLOT_SPACING*row;
			for (int column=0; column < SLOT_COLUMNS; column++)
			{
				int x = OUTPUT_START_X + SLOT_SPACING*column;
				int index = row * SLOT_COLUMNS + column;
				this.addSlot(new JumboFurnaceOutputSlot(this.player, outputs, index, x, y));
			}
		}
		
		// add hotbar slots
		for (int hotbarSlot = 0; hotbarSlot < PLAYER_COLUMNS; hotbarSlot++)
		{
			int x = HOTBAR_START_X + SLOT_SPACING * hotbarSlot;
			this.addSlot(new Slot(playerInventory, hotbarSlot, x, HOTBAR_START_Y));
		}
		
		// add backpack slots
		for (int row=0; row < BACKPACK_ROWS; row++)
		{
			int y = BACKPACK_START_Y + SLOT_SPACING * row;
			for (int column=0; column < PLAYER_COLUMNS; column++)
			{
				int x = BACKPACK_START_X + SLOT_SPACING*column;
				int index = row * PLAYER_COLUMNS + column + HOTBAR_SLOTS;
				this.addSlot(new Slot(playerInventory, index, x, y));
			}
		}

		this.trackIntArray(furnaceData);
	}

	@Override
	public boolean canInteractWith(PlayerEntity arg0)
	{
		return isWithinUsableDistance(this.usabilityTest, arg0, JumboFurnaceObjects.BLOCK);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int index)
	{
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		
		if (slot != null && slot.getHasStack())
		{
			ItemStack stackInSlot = slot.getStack();
			slotStackCopy = stackInSlot.copy();
			
			// if this is an input/fuel/output slot, try to put the item in the player slots
			if (index < FIRST_PLAYER_SLOT)
			{
				if (!this.mergeItemStack(stackInSlot, FIRST_PLAYER_SLOT, END_PLAYER_SLOTS, true))
				{
					return ItemStack.EMPTY;
				}
			}
			// otherwise, this is a player slot
			else
			{
				// if we can burn the item, try to put it in the fuel slots first
				if (ForgeHooks.getBurnTime(stackInSlot) > 0 && !this.mergeItemStack(stackInSlot, FIRST_FUEL_SLOT, END_FUEL_SLOTS, false))
				{
					return ItemStack.EMPTY;
				}
				// otherwise, try to put it in the input slots
				if (!this.mergeItemStack(stackInSlot, FIRST_INPUT_SLOT, END_INPUT_SLOTS, false))
				{
					return ItemStack.EMPTY;
				}
			}
			
			if (stackInSlot.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
			
			if (stackInSlot.getCount() == slotStackCopy.getCount())
			{
				return ItemStack.EMPTY;
			}
			
			slot.onTake(player, stackInSlot);
			
		}
		
		return slotStackCopy;
	}
	
	public int getBurnTimeRemaining()
	{
		return this.furnaceData.get(0);
	}
	
	public int getItemBurnedValue()
	{
		return this.furnaceData.get(1);
	}
	
	public int getCookProgress()
	{
		return this.furnaceData.get(2);
	}

	public int getCookProgressionScaled()
	{
		int cookProgress = this.getCookProgress();
		int cookTimeForRecipe = JumboFurnace.SERVER_CONFIG.jumboFurnaceCookTime.get();
		return cookTimeForRecipe != 0 && cookProgress != 0 ? cookProgress * 24 / cookTimeForRecipe : 0;
	}

	public int getBurnLeftScaled()
	{
		int totalBurnTime = this.getItemBurnedValue();
		if (totalBurnTime == 0)
		{
			totalBurnTime = 200;
		}

		return this.getBurnTimeRemaining() * 13 / totalBurnTime;
	}

	public boolean isBurning()
	{
		return this.getBurnTimeRemaining() > 0;
	}

}
