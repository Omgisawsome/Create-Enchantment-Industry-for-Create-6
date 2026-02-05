package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.printer;

import com.simibubi.create.AllItems;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PrinterTargetItemHandler extends SnapshotParticipant<Unit> implements SingleSlotStorage<ItemVariant> {
    PrinterBlockEntity be;

    public PrinterTargetItemHandler(PrinterBlockEntity be) {
        this.be = be;
    }

    private boolean isItemValid(ItemVariant itemVariant) {
        return itemVariant.getItem().equals(Items.ENCHANTED_BOOK) || itemVariant.getItem().equals(Items.WRITTEN_BOOK) ||
				itemVariant.getItem().equals(Items.NAME_TAG) || itemVariant.getItem().equals(AllItems.SCHEDULE.get());
    }


	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!be.getCopyTarget().isEmpty())
			return 0;

		if(!isItemValid(resource))
			return 0;

		be.snapshotParticipant.updateSnapshots(transaction);
		be.setCopyTarget(resource.toStack());
		return 1;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if(!resource.isOf(be.getCopyTarget().getItem()))
			return 0;

		be.snapshotParticipant.updateSnapshots(transaction);
		be.setCopyTarget(ItemStack.EMPTY);
		return 1;
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public ItemVariant getResource() {
		return ItemVariant.of(getStack());
	}

	@Override
	public long getAmount() {
		ItemStack stack = getStack();
		return stack.isEmpty() ? 0 : stack.getCount();
	}

	@Override
	public long getCapacity() {
		return getStack().getMaxStackSize();
	}

	public ItemStack getStack() {
		return be.getCopyTarget();
	}


	@Override
	protected Unit createSnapshot() {
		return Unit.INSTANCE;
	}

	@Override
	protected void readSnapshot(Unit snapshot) {}

	@Override
	protected void onFinalCommit() {
		super.onFinalCommit();
		be.notifyUpdate();
	}
}
