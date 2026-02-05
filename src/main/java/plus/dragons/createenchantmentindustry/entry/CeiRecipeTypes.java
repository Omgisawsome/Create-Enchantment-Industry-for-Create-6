package plus.dragons.createenchantmentindustry.entry;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import io.github.fabricators_of_create.porting_lib.util.ShapedRecipeUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.disenchanter.DisenchantRecipe;

public enum CeiRecipeTypes implements IRecipeTypeInfo {
    DISENCHANTING(DisenchantRecipe::new);

    private final ResourceLocation id;
    private final RecipeSerializer<?> serializerObject;
    @Nullable
    private final RecipeType<?> typeObject;
    private final Supplier<RecipeType<?>> type;

    CeiRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
        String name = EnchantmentIndustry.LANG.asId(name());
        id = EnchantmentIndustry.genRL(name);
		serializerObject = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializerSupplier.get());
		typeObject = simpleType(id);
		Registry.register(BuiltInRegistries.RECIPE_TYPE, id, typeObject);
		type = () -> typeObject;
    }

    CeiRecipeTypes(ProcessingRecipeBuilder.ProcessingRecipeFactory<?> processingFactory) {
        this(() -> new ProcessingRecipeSerializer<>(processingFactory));
    }

    public static <T extends Recipe<?>> RecipeType<T> simpleType(ResourceLocation id) {
        String stringId = id.toString();
        return new RecipeType<T>() {
            @Override
            public String toString() {
                return stringId;
            }
        };
    }

	public static void register() {
		ShapedRecipeUtil.setCraftingSize(9, 9);
		// fabric: just load the class
	}

    public <C extends Container, T extends Recipe<C>> Optional<T> find(C inv, Level world) {
        return world.getRecipeManager().getRecipeFor(this.getType(), inv, world);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializerObject;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeType<?>> T getType() {
        return (T) type.get();
    }
}
