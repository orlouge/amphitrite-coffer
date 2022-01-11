package io.github.orlouge.amphitritecoffer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.GenericContainerScreenHandler;

@Environment(EnvType.CLIENT)
public class AmphitriteCofferClientMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(AmphitriteCofferMod.AMPHITRITE_COFFER_BLOCK, RenderLayer.getCutout());
        ScreenRegistry.register(AmphitriteCofferMod.AMPHITRITE_COFFER_SCREEN_HANDLER, AmphitriteCofferScreen::new);
    }
}

