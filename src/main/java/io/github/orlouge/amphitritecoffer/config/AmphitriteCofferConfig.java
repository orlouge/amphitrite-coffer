package io.github.orlouge.amphitritecoffer.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "amphitritecoffer")
public class AmphitriteCofferConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean allowShulkersInCoffers = true;

    public int chargePerHeart = 12000;
}
