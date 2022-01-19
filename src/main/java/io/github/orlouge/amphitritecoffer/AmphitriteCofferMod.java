package io.github.orlouge.amphitritecoffer;

import io.github.orlouge.amphitritecoffer.config.AmphitriteCofferConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
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
				Registry.BLOCK,
				new Identifier("amphitritecoffer", "amphitrite_coffer"),
				AMPHITRITE_COFFER_BLOCK
		);

		Registry.register(
				Registry.ITEM,
				new Identifier("amphitritecoffer", "amphitrite_coffer"),
				new BlockItem(AMPHITRITE_COFFER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC).maxCount(1))
		);

		AMPHITRITE_COFFER_BLOCK_ENTITY = Registry.register(
				Registry.BLOCK_ENTITY_TYPE,
				"amphitritecoffer:amphitrite_coffer_block_entity",
				FabricBlockEntityTypeBuilder.create(AmphitriteCofferBlockEntity::new, AMPHITRITE_COFFER_BLOCK).build(null)
		);

		Registry.register(
				Registry.RECIPE_TYPE,
				new Identifier("amphitritecoffer", "water_conversion"),
				AmphitriteCofferRecipe.Type.INSTANCE
		);

		Registry.register(
				Registry.RECIPE_SERIALIZER,
				AmphitriteCofferRecipe.Serializer.ID,
				AmphitriteCofferRecipe.Serializer.INSTANCE
		);
	}


}
