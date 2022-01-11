package io.github.orlouge.amphitritecoffer.mixin;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.structure.OceanMonumentGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

import static net.minecraft.state.property.Properties.WATERLOGGED;
import io.github.orlouge.amphitritecoffer.AmphitriteCofferMod;

@Mixin(OceanMonumentGenerator.CoreRoom.class)
public abstract class CoreRoomMixin extends StructurePiece {
    private static final BlockState WATERLOGGED_COFFER = AmphitriteCofferMod.AMPHITRITE_COFFER_BLOCK.getDefaultState().with(WATERLOGGED, true);

    protected CoreRoomMixin(StructurePieceType type, int length, BlockBox boundingBox) {
        super(type, length, boundingBox);
    }

    @Redirect(method = "generate(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Ljava/util/Random;Lnet/minecraft/util/math/BlockBox;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/util/math/BlockPos;)V",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/OceanMonumentGenerator$CoreRoom;fillWithOutline(Lnet/minecraft/world/StructureWorldAccess;Lnet/minecraft/util/math/BlockBox;IIIIIILnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;Z)V", ordinal = 12)
              )
    public void fillCoreRoomTreasure(OceanMonumentGenerator.CoreRoom coreRoom, StructureWorldAccess world, BlockBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState out, BlockState in, boolean canReplaceAir, StructureWorldAccess world2, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random) {
        this.fillWithOutline(world, box, minX, minY, minZ, maxX, maxY - 1, maxZ, Blocks.GOLD_BLOCK.getDefaultState(), Blocks.GOLD_BLOCK.getDefaultState(), canReplaceAir);
        this.fillWithOutline(world, box, minX, minY + 1, minZ, maxX, maxY, maxZ, WATERLOGGED_COFFER, WATERLOGGED_COFFER, canReplaceAir);
        int treasurePosition = random.nextInt(4), treasurePosition2 = (treasurePosition + random.nextInt(3) + 1) % 4;
        int lootRotation = 0;
        for (int x = minX; x < minX + 2; x++) {
            for (int z = minZ; z < minZ + 2; z++) {
                lootRotation++;
                Identifier lootTable;
                if (treasurePosition == lootRotation) {
                    lootTable = AmphitriteCofferMod.MONUMENT_CORE_LOOT_HEART;
                } else if (treasurePosition2 == lootRotation) {
                    lootTable = AmphitriteCofferMod.MONUMENT_CORE_LOOT_TREASURE;
                } else {
                    lootTable = AmphitriteCofferMod.MONUMENT_CORE_LOOT_GENERIC;
                }
                LootableContainerBlockEntity.setLootTable(
                        world,
                        random,
                        new BlockPos(this.applyXTransform(x, z), this.applyYTransform(minY + 1), this.applyZTransform(x, z)),
                        lootTable
                );
            }
        }
    }
}
