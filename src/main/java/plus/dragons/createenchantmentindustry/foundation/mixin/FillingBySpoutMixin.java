package plus.dragons.createenchantmentindustry.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.spout.FillingBySpout;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.MendingBySpout;

@Mixin(value = FillingBySpout.class)
public class FillingBySpoutMixin {
    @Inject(method = "canItemBeFilled", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/transfer/GenericItemFilling;canItemBeFilled(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Z"), cancellable = true)
    private static void canItemBeMended(Level world, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (MendingBySpout.canItemBeMended(world, stack))
            cir.setReturnValue(true);
    }

    @Inject(method = "getRequiredAmountForItem",
			at = @At(value = "INVOKE",
					target = "Lcom/simibubi/create/content/fluids/transfer/GenericItemFilling;getRequiredAmountForItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lio/github/fabricators_of_create/porting_lib/fluids/FluidStack;)J"),
			cancellable = true)
    private static void getRequiredXpAmountForItem(Level world, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<Long> cir) {
        int amount = MendingBySpout.getRequiredAmountForItem(world, stack, availableFluid);
        if (amount > 0)
            cir.setReturnValue((long) amount);
    }

    @Inject(method = "fillItem",
			at = @At(value = "INVOKE",
					target = "Lcom/simibubi/create/content/fluids/transfer/GenericItemFilling;fillItem(Lnet/minecraft/world/level/Level;JLnet/minecraft/world/item/ItemStack;Lio/github/fabricators_of_create/porting_lib/fluids/FluidStack;)Lnet/minecraft/world/item/ItemStack;"),
			cancellable = true)
    private static void mendItem(Level world, long requiredAmount, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = MendingBySpout.mendItem(world, (int) requiredAmount, stack, availableFluid);
        if (result != null)
            cir.setReturnValue(result);
    }
}
