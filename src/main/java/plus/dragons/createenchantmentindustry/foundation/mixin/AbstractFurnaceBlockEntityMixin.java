package plus.dragons.createenchantmentindustry.foundation.mixin;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.UNIT_PER_MB;

import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

import plus.dragons.createenchantmentindustry.EnchantmentIndustry;

import javax.annotation.Nullable;

@Mixin(AbstractFurnaceBlockEntity.class)
abstract public class AbstractFurnaceBlockEntityMixin extends BaseContainerBlockEntity implements WorldlyContainer, RecipeHolder, StackedContentsCompatible, SidedStorageBlockEntity {
	protected AbstractFurnaceBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Shadow @Final private Object2IntOpenHashMap<ResourceLocation> recipesUsed;

	@Unique
	protected long calculateExperienceStored() {
		double total = 0.0;
		for (var entry : recipesUsed.object2IntEntrySet()) {
			total += getLevel().getRecipeManager().byKey(entry.getKey()).map(recipe -> (double)((AbstractCookingRecipe) recipe).getExperience() * entry.getIntValue()).orElse(0.0);
		}
		total *= UNIT_PER_MB;
		return (long)total;
	}

	@Inject(method = "setRecipeUsed", at = @At("TAIL"))
	private void injectSetRecipeUsed(@Nullable Recipe<?> recipe, CallbackInfo ci) {
		if (recipe != null) {
			long amount = internalTank.getFluidAmount();
			amount += (amount == 0) ? calculateExperienceStored() :
                    (long) Math.floor(((AbstractCookingRecipe) recipe).getExperience() * UNIT_PER_MB);
			internalTank.setFluid(new FluidStack(FluidVariant.of(CeiFluids.EXPERIENCE.getSource()), amount));
		}
	}

	@Unique
	protected final FluidTank internalTank = new FluidTank(java.lang.Long.MAX_VALUE, fs -> fs.getType().equals(FluidVariant.of(CeiFluids.EXPERIENCE.getSource()))) {
		@Override
		protected void onContentsChanged() {
			long total = calculateExperienceStored();
			long diff = total - getFluidAmount();

			if (diff <= 0) {
				return;
			}

			if (diff >= total) {
				recipesUsed.clear();
				return;
			}

			var recipeManager = getLevel().getRecipeManager();
			var it = recipesUsed.object2IntEntrySet();
			var entriesUsed = it.stream()
				.filter(e -> recipeManager.byKey(e.getKey()).isPresent())
				.toList();
			for (var entry : entriesUsed) {
				var usedRecipe = (AbstractCookingRecipe)recipeManager.byKey(entry.getKey()).get();
				long experience = (long)(usedRecipe.getExperience() * UNIT_PER_MB);
				int count = (int)Math.min((diff / experience), entry.getIntValue());
				if (count > 0) {
					diff -= experience;
					recipesUsed.addTo(usedRecipe.getId(), -count);
				}
			}
		}
	};

	@Unique
	public final Storage<FluidVariant> exposedExperienceTank = FilteringStorage.extractOnlyOf(internalTank);

    @Override
    public @Nullable Storage<FluidVariant> getFluidStorage(Direction side) {
		if (side != null && side.getAxis().isHorizontal())
			return exposedExperienceTank;
		return null;
    }
}
