package plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnchantingItemHandler extends SnapshotParticipant<Unit> implements SingleSlotStorage<ItemVariant> {
    private final BlazeEnchanterBlockEntity be;
    private final Direction side;

    public EnchantingItemHandler(BlazeEnchanterBlockEntity be, Direction side) {
        this.be = be;
        this.side = side;
    }

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (!be.getHeldItemStack().isEmpty())
			return 0;
		ItemStack stack = resource.toStack();
		if(Enchanting.getValidEnchantment(stack, be.targetItem, be.hyper()) == null)
			return 0;
		int toInsert = GenericItemEmptying.canItemBeEmptied(be.getLevel(), stack)
				? 1
				: Math.min((int) maxAmount, resource.getItem().getMaxStackSize());
		stack.setCount(toInsert);
		TransportedItemStack heldItem = new TransportedItemStack(stack);
		heldItem.prevBeltPosition = 0;
		be.snapshotParticipant.updateSnapshots(transaction);
		be.setHeldItem(heldItem, side.getOpposite());
		return toInsert;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		TransportedItemStack held = be.heldItem;
		if (held == null)
			return 0;
		int toExtract = Math.min((int) maxAmount, held.stack.getCount());
		ItemStack stack = held.stack.copy();
		stack.shrink(toExtract);
		be.snapshotParticipant.updateSnapshots(transaction);
		be.heldItem.stack = stack;
		if (stack.isEmpty())
			be.heldItem = null;
		return toExtract;
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
		TransportedItemStack held = be.heldItem;
		if (held == null || held.stack == null || held.stack.isEmpty())
			return ItemStack.EMPTY;
		return held.stack;
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
