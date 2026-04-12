package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.fluids.OpenEndedPipe;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceFluid;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceOrb;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

@Mixin(targets = "com.simibubi.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler", remap = false)
public class OpenEndedPipeMixin {

	@Final
	@Shadow(aliases = "this$0")
	private OpenEndedPipe this$0;

	@Unique
	private long cei$dropletRemainder = 0;

	@Inject(method = "insert", at = @At("HEAD"), cancellable = true, remap = false)
	private void cei$interceptXPInsert(FluidVariant resource, long maxAmount, TransactionContext transaction, CallbackInfoReturnable<Long> cir) {
		if (maxAmount <= 0) return;

		Fluid fluid = resource.getFluid();

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
									BlockPos pipePos = pipeInstance.getPos();
									BlockPos outputPos = pipeInstance.getOutputPos();

									Vec3 spawnPos = Vec3.atCenterOf(outputPos);

									Vec3 speed = new Vec3(
											outputPos.getX() - pipePos.getX(),
											outputPos.getY() - pipePos.getY(),
											outputPos.getZ() - pipePos.getZ()
									).scale(0.2);

									ExperienceOrb orb;
									if (fluid instanceof HyperExperienceFluid) {
										orb = new HyperExperienceOrb(serverLevel, spawnPos.x, spawnPos.y - 0.25, spawnPos.z, xpAmount * 10);
									} else {
										orb = new ExperienceOrb(serverLevel, spawnPos.x, spawnPos.y - 0.25, spawnPos.z, xpAmount);
									}

									// Apply the velocity push
									orb.setDeltaMovement(speed);
									serverLevel.addFreshEntity(orb);
								}
							}
						}
					} catch (Throwable t) {
						System.err.println("[CEI] Failed to handle XP spill from Open Ended Pipe:");
						t.printStackTrace();
					}
				}
			});

			cir.setReturnValue(maxAmount);
		}
	}
}
