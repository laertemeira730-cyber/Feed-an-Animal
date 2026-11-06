package com.feedanimals.mod.registry;

import com.feedanimals.mod.FeedAnimalsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FeedAnimalsMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.feedanimals.main"))
                    .icon(() -> new ItemStack(ModItems.TRAINING_BAR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MANAGEMENT_MENU.get());
                        output.accept(ModItems.TRAINING_BAR.get());
                        output.accept(ModItems.PEN_MARKER.get());
                        output.accept(ModItems.SALES_MARKER.get());
                        output.accept(ModItems.PHASE_MARKER.get());
                    }).build());

    private ModCreativeTabs() {}
}
