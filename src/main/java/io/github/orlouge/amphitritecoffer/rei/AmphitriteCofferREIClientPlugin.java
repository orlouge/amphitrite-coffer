package io.github.orlouge.amphitritecoffer.rei;

import io.github.orlouge.amphitritecoffer.AmphitriteCofferMod;
import io.github.orlouge.amphitritecoffer.WaterConversionRecipe;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.util.Identifier;

public class AmphitriteCofferREIClientPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<WaterConversionDisplay> WATER_CONVERSION =
            new CategoryIdentifier<WaterConversionDisplay>() {
                @Override
                public Identifier getIdentifier() {
                    return new Identifier(WaterConversionRecipe.Serializer.ID);
                }
            };

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new WaterConversionDisplayCategory());
        registry.removePlusButton(WATER_CONVERSION);
        registry.addWorkstations(WATER_CONVERSION, EntryStacks.of(AmphitriteCofferMod.AMPHITRITE_COFFER_BLOCK));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(WaterConversionRecipe.class, WaterConversionDisplay::new);
    }
}
