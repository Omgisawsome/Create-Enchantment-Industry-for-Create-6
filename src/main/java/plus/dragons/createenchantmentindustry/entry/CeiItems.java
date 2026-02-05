package plus.dragons.createenchantmentindustry.entry;

import static plus.dragons.createenchantmentindustry.EnchantmentIndustry.REGISTRATE;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllFluids;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import plus.dragons.createenchantmentindustry.content.contraptions.enchanting.enchanter.EnchantingGuideItem;
import plus.dragons.createenchantmentindustry.content.contraptions.fluids.experience.HyperExperienceBottleItem;

public class CeiItems {

    static {
        REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB.key());
    }

    public static final ItemEntry<EnchantingGuideItem> ENCHANTING_GUIDE = REGISTRATE.item("enchanting_guide", EnchantingGuideItem::new)
            .properties(prop -> prop.stacksTo(1))
            .register();

    public static final ItemEntry<HyperExperienceBottleItem> HYPER_EXP_BOTTLE = REGISTRATE.item("hyper_experience_bottle", HyperExperienceBottleItem::new)
            .properties(prop -> prop.rarity(Rarity.RARE))
            .lang("Bottle O' Hyper Enchanting")
            .tag(CeiTags.ItemTag.UPRIGHT_ON_BELT.tag)
            .register();

    public static void register() {}

}
