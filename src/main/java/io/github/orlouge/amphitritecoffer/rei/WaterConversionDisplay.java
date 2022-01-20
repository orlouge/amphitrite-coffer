package io.github.orlouge.amphitritecoffer.rei;

import io.github.orlouge.amphitritecoffer.WaterConversionRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.List;
import java.util.Optional;

public class WaterConversionDisplay extends BasicDisplay implements SimpleGridMenuDisplay {
    private final int charge;
    private final Optional<EntryIngredient> additionalOutput;

    public WaterConversionDisplay(WaterConversionRecipe recipe) {
        super(
                List.of(EntryIngredients.ofItemStacks(recipe.getInput())),
                List.of(EntryIngredients.of(recipe.getSampleOutput()))
        );
        this.charge = recipe.getCost();
        this.additionalOutput = recipe.getAdditionalOutput().map(EntryIngredients::of);
    }

    @Override
    public int getWidth() {
        return 2;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AmphitriteCofferREIClientPlugin.WATER_CONVERSION;
    }

    public int getCharge() {
        return charge;
    }

    public Optional<EntryIngredient> getAdditionalOutput() {
        return additionalOutput;
    }
}
