package com.feedanimals.mod.registry;

import com.feedanimals.mod.FeedAnimalsMod;
import com.feedanimals.mod.data.AreaSavedData.AreaType;
import com.feedanimals.mod.item.AnimalTokenItem;
import com.feedanimals.mod.item.AreaMarkerItem;
import com.feedanimals.mod.item.FeedFoodItem;
import com.feedanimals.mod.item.FoodShopItem;
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
            () -> new AreaMarkerItem(ModBlocks.PEN_MARKER.get(), new Item.Properties(), AreaType.PEN));
    public static final RegistryObject<Item> SALES_MARKER = ITEMS.register("sales_marker",
            () -> new AreaMarkerItem(ModBlocks.SALES_MARKER.get(), new Item.Properties(), AreaType.SALES));
    public static final RegistryObject<Item> PHASE_MARKER = ITEMS.register("phase_marker",
            () -> new PhaseMarkerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FOOD_SHOP = ITEMS.register("food_shop",
            () -> new FoodShopItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FEED_BASIC = ITEMS.register("feed_basic",
            () -> new FeedFoodItem(new Item.Properties(), 5));
    public static final RegistryObject<Item> FEED_RARE = ITEMS.register("feed_rare",
            () -> new FeedFoodItem(new Item.Properties(), 10));
    public static final RegistryObject<Item> FEED_PREMIUM = ITEMS.register("feed_premium",
            () -> new FeedFoodItem(new Item.Properties(), 16));
    public static final RegistryObject<Item> ANIMAL_TOKEN = ITEMS.register("animal_token",
            () -> new AnimalTokenItem(new Item.Properties().stacksTo(1)));

    private ModItems() {}
}
