package plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import org.jetbrains.annotations.Nullable;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;

public class MendingBySpout {

    public static boolean canItemBeMended(Level world, ItemStack stack) {
        return stack.isDamaged() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, stack) > 0;
    }

    public static int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
        if (!(CeiFluids.EXPERIENCE.is(availableFluid.getFluid()) && canItemBeMended(world, stack)))
            return -1;
        return (int) Math.min(availableFluid.getAmount(), (long) Mth.ceil(stack.getDamageValue() / getXpRepairRatio()) * UNIT_PER_MB);
    }

    @Nullable
    public static ItemStack mendItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        if (!(CeiFluids.EXPERIENCE.is(availableFluid.getFluid()) && canItemBeMended(world, stack)))
            return null;
        ItemStack result = stack.split(1);
        availableFluid.shrink(requiredAmount);
        int damage = result.getDamageValue();
        damage -= Math.min((int) (requiredAmount * getXpRepairRatio() / UNIT_PER_MB), damage);
        result.setDamageValue(damage);
        return result;
    }

	private static float getXpRepairRatio(){
		// In IForgeItem:
		//default float getXpRepairRatio(ItemStack stack)
		//    {
		//        return 2f;
		//    }
		return 2f;
	}

}
