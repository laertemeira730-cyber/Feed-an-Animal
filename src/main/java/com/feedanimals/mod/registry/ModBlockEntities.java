package com.feedanimals.mod.registry;

import com.feedanimals.mod.FeedAnimalsMod;
import com.feedanimals.mod.block.entity.TrainingBarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FeedAnimalsMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<TrainingBarBlockEntity>> TRAINING_BAR =
            BLOCK_ENTITIES.register("training_bar", () -> BlockEntityType.Builder
                    .of(TrainingBarBlockEntity::new, ModBlocks.TRAINING_BAR.get())
                    .build(null));

    private ModBlockEntities() {}
}
