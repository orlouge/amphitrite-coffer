package io.github.orlouge.amphitritecoffer.rei;

import io.github.orlouge.amphitritecoffer.AmphitriteCofferMod;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.text.Text;

import java.util.List;
import java.util.ArrayList;

public class WaterConversionDisplayCategory implements DisplayCategory<WaterConversionDisplay> {
    @Override
    public Renderer getIcon() {
        return EntryStacks.of(AmphitriteCofferMod.AMPHITRITE_COFFER_BLOCK);
    }

    @Override
    public Text getTitle() {
        return Text.of("Water Conversion");
    }

    @Override
    public CategoryIdentifier<? extends WaterConversionDisplay> getCategoryIdentifier() {
        return AmphitriteCofferREIClientPlugin.WATER_CONVERSION;
    }

    @Override
    public List<Widget> setupDisplay(WaterConversionDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<Widget>(DisplayCategory.super.setupDisplay(display, bounds));
        Point startPoint = new Point(bounds.getCenterX() - 41, bounds.getCenterY() - 13);

        widgets.add(Widgets.createArrow(new Point(startPoint.x + 27, startPoint.y + 4)));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 4, startPoint.y + 5))
                .entries(display.getInputEntries().get(0))
                .markInput());

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 5))
                .entries(display.getOutputEntries().get(0))
                .markOutput());

        display.getAdditionalOutput().ifPresent(out -> {
            widgets.add(Widgets.createSlot(new Point(startPoint.x + 81, startPoint.y + 5))
                    .entries(out)
                    .markOutput());
        });

        if (display.getCharge() > 0) {
            widgets.add(Widgets.createLabel(new Point(startPoint.x + 40, startPoint.y + 25), Text.of("Charge: " + display.getCharge())));
        }

        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 50;
    }
}
