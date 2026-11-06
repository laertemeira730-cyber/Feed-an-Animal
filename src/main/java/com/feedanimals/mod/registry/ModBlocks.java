package com.feedanimals.mod.registry;

import com.feedanimals.mod.FeedAnimalsMod;
import com.feedanimals.mod.block.AreaMarkerBlock;
import com.feedanimals.mod.block.TrainingBarBlock;
import com.feedanimals.mod.data.AreaSavedData.AreaType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FeedAnimalsMod.MOD_ID);

    public static final RegistryObject<Block> TRAINING_BAR = BLOCKS.register("training_bar",
            () -> new TrainingBarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static final RegistryObject<Block> PEN_MARKER = BLOCKS.register("pen_marker",
            () -> new AreaMarkerBlock(AreaType.PEN, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(1.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()));

    public static final RegistryObject<Block> SALES_MARKER = BLOCKS.register("sales_marker",
            () -> new AreaMarkerBlock(AreaType.SALES, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(1.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()));

    private ModBlocks() {}
}
