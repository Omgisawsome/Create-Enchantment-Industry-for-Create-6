package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceFluid;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceOrb;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler", remap = false)
public abstract class OpenEndedPipeMixin extends FluidTank {

	// Grabs the outer OpenEndedPipe instance automatically without needing a custom Accessor
	@Final
	@Shadow(aliases = "this$0")
	private OpenEndedPipe this$0;

	@Unique
	private long cei$dropletRemainder = 0;

	public OpenEndedPipeMixin(long capacity) {
		super(capacity);
	}

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		long filled = super.insert(resource, maxAmount, transaction);
		Fluid fluid = resource.getFluid();

		if (maxAmount <= 0) return filled;

		// Ensure we compare against the actual Fluid objects
		boolean isExp = fluid.isSame(CeiFluids.EXPERIENCE.get()) ||
				fluid.isSame(CeiFluids.HYPER_EXPERIENCE.get());

		if (isExp) {
			transaction.addCloseCallback((context, result) -> {
				if (result.wasCommitted()) {
					try {
						OpenEndedPipe pipeInstance = this.this$0;

						if (pipeInstance != null) {
							Level level = pipeInstance.getWorld();
							if (level instanceof ServerLevel serverLevel) {

								long totalDroplets = maxAmount + this.cei$dropletRemainder;
								int xpAmount = (int) (totalDroplets / 81);
								this.cei$dropletRemainder = totalDroplets % 81;

								if (xpAmount > 0) {
									BlockPos pos = pipeInstance.getPos();
									Vec3 spawnPos = Vec3.atCenterOf(pos).subtract(0, 0.5, 0);

									if (fluid instanceof HyperExperienceFluid) {
										serverLevel.addFreshEntity(new HyperExperienceOrb(serverLevel, spawnPos.x, spawnPos.y, spawnPos.z, xpAmount * 10));
									} else {
										serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, spawnPos.x, spawnPos.y, spawnPos.z, xpAmount));
									}
								}
							}
						}
					} catch (Throwable t) {
						System.err.println("[CEI] Failed to handle XP spill from Open Ended Pipe:");
						t.printStackTrace();
					}
				}
			});

			// THE BLACK HOLE FIX: We tell Create that the entire volume was successfully accepted.
			// This prevents fluid from ever backing up into the pipe's internal buffer!
			return maxAmount;
		}

		return filled;
	}
}
