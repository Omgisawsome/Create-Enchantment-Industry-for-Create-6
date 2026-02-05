package plus.dragons.createenchantmentindustry;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class EnchantmentIndustryData implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		ExistingFileHelper helper = ExistingFileHelper.withResourcesFromArg();
		REGISTRATE.setupDatagen(generator.createPack(), helper);
		EnchantmentIndustry.gatherData(generator, helper);
	}
}
