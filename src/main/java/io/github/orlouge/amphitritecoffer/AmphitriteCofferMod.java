package io.github.orlouge.amphitritecoffer;

import io.github.orlouge.amphitritecoffer.config.AmphitriteCofferConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.block.Block;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AmphitriteCofferMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("amphitrite.coffer");
    public static final boolean ALLOW_NESTED_COFFERS = false;
	public static final AmphitriteCofferConfig CONFIG = AutoConfig.register(AmphitriteCofferConfig.class, GsonConfigSerializer::new).getConfig();

	public static final Block AMPHITRITE_COFFER_BLOCK = new AmphitriteCofferBlock();

    public static final ScreenHandlerType<?> AMPHITRITE_COFFER_SCREEN_HANDLER =
			ScreenHandlerRegistry.registerSimple(
					new Identifier("amphitritecoffer", "amphitrite_coffer"),
					AmphitriteCofferScreenHandler::new
			);

    public static BlockEntityType<AmphitriteCofferBlockEntity> AMPHITRITE_COFFER_BLOCK_ENTITY;

	public static Identifier MONUMENT_CORE_LOOT_HEART =
			new Identifier("amphitritecoffer", "chests/monument_core_heart");

	public static Identifier MONUMENT_CORE_LOOT_TREASURE =
			new Identifier("amphitritecoffer", "chests/monument_core_treasure");

	public static Identifier MONUMENT_CORE_LOOT_GENERIC =
			new Identifier("amphitritecoffer", "chests/monument_core_generic");

	@Override
	public void onInitialize() {
		Registry.register(
				Registries.BLOCK,
				new Identifier("amphitritecoffer", "amphitrite_coffer"),
				AMPHITRITE_COFFER_BLOCK
		);

		BlockItem block_item = new BlockItem(AMPHITRITE_COFFER_BLOCK, new FabricItemSettings().maxCount(1));
		Registry.register(
				Registries.ITEM,
				new Identifier("amphitritecoffer", "amphitrite_coffer"),
				block_item
		);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(block_item));

		AMPHITRITE_COFFER_BLOCK_ENTITY = Registry.register(
				Registries.BLOCK_ENTITY_TYPE,
				"amphitritecoffer:amphitrite_coffer_block_entity",
				FabricBlockEntityTypeBuilder.create(AmphitriteCofferBlockEntity::new, AMPHITRITE_COFFER_BLOCK).build(null)
		);

		Registry.register(
				Registries.RECIPE_TYPE,
				new Identifier("amphitritecoffer", "water_conversion"),
				WaterConversionRecipe.Type.INSTANCE
		);

		Registry.register(
				Registries.RECIPE_SERIALIZER,
				WaterConversionRecipe.Serializer.ID,
				WaterConversionRecipe.Serializer.INSTANCE
		);
	}


}
