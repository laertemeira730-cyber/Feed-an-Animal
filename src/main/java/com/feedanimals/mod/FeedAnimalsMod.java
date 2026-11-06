package com.feedanimals.mod;

import com.feedanimals.mod.registry.ModBlockEntities;
import com.feedanimals.mod.registry.ModBlocks;
import com.feedanimals.mod.registry.ModItems;
import com.feedanimals.mod.registry.ModCreativeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FeedAnimalsMod.MOD_ID)
public class FeedAnimalsMod {
    public static final String MOD_ID = "feedanimals";

    public FeedAnimalsMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
    }
}
