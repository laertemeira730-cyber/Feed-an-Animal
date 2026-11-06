package com.feedanimals.mod.registry;

import com.feedanimals.mod.FeedAnimalsMod;
import com.feedanimals.mod.item.ManagementMenuItem;
import com.feedanimals.mod.item.PhaseMarkerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FeedAnimalsMod.MOD_ID);

    public static final RegistryObject<Item> TRAINING_BAR = ITEMS.register("training_bar",
            () -> new BlockItem(ModBlocks.TRAINING_BAR.get(), new Item.Properties()));
    public static final RegistryObject<Item> PEN_MARKER = ITEMS.register("pen_marker",
            () -> new BlockItem(ModBlocks.PEN_MARKER.get(), new Item.Properties()));
    public static final RegistryObject<Item> SALES_MARKER = ITEMS.register("sales_marker",
            () -> new BlockItem(ModBlocks.SALES_MARKER.get(), new Item.Properties()));
    public static final RegistryObject<Item> PHASE_MARKER = ITEMS.register("phase_marker",
            () -> new PhaseMarkerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MANAGEMENT_MENU = ITEMS.register("management_menu",
            () -> new ManagementMenuItem(new Item.Properties().stacksTo(1)));

    private ModItems() {}
}
