package plus.dragons.createenchantmentindustry.content.contraptions.fluids;

import java.util.function.Predicate;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import plus.dragons.createenchantmentindustry.foundation.mixin.TankSegmentAccessor;

public class FilteringFluidTankBehaviour extends SmartFluidTankBehaviour {

    protected final Predicate<FluidVariant> filter;

    public FilteringFluidTankBehaviour(BehaviourType<SmartFluidTankBehaviour> type,
                                       Predicate<FluidVariant> filter,
                                       SmartBlockEntity be,
                                       int tanks, int tankCapacity,
                                       boolean enforceVariety) {
        super(type, be, tanks, tankCapacity, enforceVariety);
        this.filter = filter;
        SmartFluidTank[] handlers = new SmartFluidTank[tanks];
        for (int i = 0; i < tanks; i++) {
            TankSegment tankSegment = new TankSegment(tankCapacity);
            this.tanks[i] = tankSegment;
            handlers[i] = ((TankSegmentAccessor)tankSegment).getTank();
        }
        this.capability = new InternalFluidHandler(handlers, enforceVariety);
    }

    public static FilteringFluidTankBehaviour single(Predicate<FluidVariant> filter, SmartBlockEntity be, int capacity) {
        return new FilteringFluidTankBehaviour(TYPE, filter, be, 1, capacity, false);
    }

    public class InternalFluidHandler extends SmartFluidTankBehaviour.InternalFluidHandler {

        public InternalFluidHandler(SmartFluidTank[] handlers, boolean enforceVariety) {
            super(handlers, enforceVariety);
        }

		@Override
		public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
			if (!filter.test(resource))
				return 0;
			return super.insert(resource, maxAmount, transaction);
		}
	}

}
