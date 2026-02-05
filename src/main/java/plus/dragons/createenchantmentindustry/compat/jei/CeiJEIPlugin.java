package plus.dragons.createenchantmentindustry.compat.jei;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllFluids;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.fabric.ingredients.fluid.JeiFluidIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.compat.jei.category.DisenchantingCategory;
import plus.dragons.createenchantmentindustry.compat.jei.category.RecipeCategoryBuilder;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchantRecipe;
import plus.dragons.createenchantmentindustry.entry.CeiBlocks;
import plus.dragons.createenchantmentindustry.entry.CeiFluids;
import plus.dragons.createenchantmentindustry.entry.CeiRecipeTypes;

// @JeiPlugin Annotation is for forge so check mod.json for the entrypoint
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@JeiPlugin
public class CeiJEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = EnchantmentIndustry.genRL("jei_plugin");

    protected final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();
    protected IIngredientManager ingredientManager;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

	@Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategories();
        registration.addRecipeCategories(allCategories.toArray(IRecipeCategory[]::new));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
		ingredientManager = registration.getIngredientManager();
		allCategories.forEach(c -> c.registerRecipes(registration));

		// fabric: hide flowing fluids
		registration.getIngredientManager().removeIngredientsAtRuntime(
				FabricTypes.FLUID_STACK,
				List.of(
						new JeiFluidIngredient(CeiFluids.EXPERIENCE.get().getFlowing(), 1),
						new JeiFluidIngredient(CeiFluids.HYPER_EXPERIENCE.get().getFlowing(), 1),
						new JeiFluidIngredient(CeiFluids.INK.get().getFlowing(), 1)
				)
		);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        allCategories.forEach(c -> c.registerCatalysts(registration));
    }

    private static <T extends Recipe<?>> RecipeCategoryBuilder<T> builder(Class<T> cls) {
        return new RecipeCategoryBuilder<>(EnchantmentIndustry.ID, cls);
    }

    private void loadCategories() {
        allCategories.clear();
        allCategories.add(
                builder(DisenchantRecipe.class)
                        .addTypedRecipes(CeiRecipeTypes.DISENCHANTING)
                        .catalyst(CeiBlocks.DISENCHANTER::get)
                        .emptyBackground(177, 50)
                        .build("disenchanting", DisenchantingCategory::new)
        );
    }
}
