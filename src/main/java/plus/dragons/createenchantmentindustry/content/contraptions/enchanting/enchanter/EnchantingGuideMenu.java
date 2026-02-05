package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.LANG;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;

public class EnchantingGuideMenu extends GhostItemMenu<ItemStack> {
    private static final Component NO_ENCHANTMENT = LANG.translate("gui.enchanting_guide.no_enchantment").component();
    private ImmutableList<Component> previousEnchantments;
    ImmutableList<Component> enchantments = ImmutableList.of(NO_ENCHANTMENT);
    boolean directItemStackEdit;
    @Nullable
    BlockPos blockPos = null;

	private static final List<Integer> listOfBrokenSlots = List.of(8, 17, 26, 35);

    public EnchantingGuideMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
        super(type, id, inv, extraData);
        directItemStackEdit = extraData.readBoolean();
        if(!directItemStackEdit){
            blockPos = extraData.readBlockPos();
        }
    }

    public EnchantingGuideMenu(MenuType<?> type, int id, Inventory inv, ItemStack contentHolder, @Nullable BlockPos blockPos) {
        super(type, id, inv, contentHolder);
        if(blockPos!=null){
            directItemStackEdit = false;
            this.blockPos = blockPos;
        } else {
            directItemStackEdit = true;
        }
    }

    private void updateEnchantments(ItemStack stack) {
        var map = EnchantmentHelper.getEnchantments(stack);
        if (map.isEmpty())
            enchantments = ImmutableList.of(NO_ENCHANTMENT);
        else
            enchantments = ImmutableList.copyOf(map
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey().getFullname(entry.getValue()))
                    .toArray(Component[]::new)
            );
        boolean resetIndex = previousEnchantments == null || !previousEnchantments.toString().equals(enchantments.toString());
        previousEnchantments = ImmutableList.copyOf(enchantments);
		if(FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT)){
			if (Minecraft.getInstance().screen instanceof EnchantingGuideScreen screen) {
				screen.updateScrollInput(resetIndex);
			}
		}
    }

    @Override
    protected ItemStackHandler createGhostInventory() {
        return new ItemStackHandler(1);
    }

    @Override
    protected boolean allowRepeats() {
        return true;
    }

    @Override
    protected void initAndReadInventory(ItemStack contentHolder) {
        super.initAndReadInventory(contentHolder);
        var tag = contentHolder.getOrCreateTag();
        if (tag.contains("target", Tag.TAG_COMPOUND)) {
            ItemStack target = ItemStack.of(tag.getCompound("target"));
            ghostInventory.setStackInSlot(0, target);
            updateEnchantments(target);
        }
    }

    @Override
    protected ItemStack createOnClient(FriendlyByteBuf extraData) {
        return extraData.readItem();
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(44, 70);
        this.addSlot(new EnchantedBookSlot(0, 51, 22));
    }

    @Override
    protected void saveData(ItemStack contentHolder) {
    }

    @Override
    public boolean stillValid(Player player) {
        if(!directItemStackEdit){
            return super.stillValid(player) && player.level().getBlockEntity(blockPos) instanceof BlazeEnchanterBlockEntity;
        }
        return super.stillValid(player);
    }


    class EnchantedBookSlot extends SlotItemHandler {

        public EnchantedBookSlot(int index, int xPosition, int yPosition) {
            super(ghostInventory, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack pStack) {
            return pStack.is(Items.ENCHANTED_BOOK) && !EnchantmentHelper.getEnchantments(pStack).isEmpty();
        }

        @Override
        public void setChanged() {
            super.setChanged();
            updateEnchantments(getItem());
        }

    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
		if (listOfBrokenSlots.contains(slotId) && clickTypeIn == ClickType.THROW) {
			clickTypeIn = ClickType.PICKUP;
		}

        if (slotId < 36) {
            super.clicked(slotId, dragType, clickTypeIn, player);
            return;
        }
        if (clickTypeIn == ClickType.THROW)
            return;

        ItemStack held = getCarried();
        if (clickTypeIn == ClickType.CLONE) {
            if (player.isCreative() && held.isEmpty()) {
                ItemStack stackInSlot = ghostInventory.getStackInSlot(0)
                        .copy();
                setCarried(stackInSlot);
            }
        } else if (getSlot(36).mayPlace(held) || held.isEmpty()) {
            ghostInventory.setStackInSlot(0, held.copy());
            getSlot(slotId).setChanged();
        }
    }

    @Override
	@SuppressWarnings("UnstableApiUsage")
    public ItemStack quickMoveStack(Player playerIn, int index) {
        if (index < 36) {
            ItemStack stackToInsert = playerInventory.getItem(index);
            if (getSlot(36).mayPlace(stackToInsert)) {
                ItemStack copy = stackToInsert.copy();
				ItemVariant itemVariant = ItemVariant.of(copy);
				try(Transaction t = TransferUtil.getTransaction()) {
					try (Transaction nested = t.openNested()) {
						ghostInventory.insertSlot(0, itemVariant, copy.getCount(), nested);
						getSlot(36).setChanged();
					}
				}

            }
        } else {
			try(Transaction t = TransferUtil.getTransaction()) {
				try (Transaction nested = t.openNested()) {
					ghostInventory.extractSlot(0, ItemVariant.of(ghostInventory.getStackInSlot(0)),1, nested);
					getSlot(index).setChanged();
				}
			}

        }
        return ItemStack.EMPTY;
    }
}
