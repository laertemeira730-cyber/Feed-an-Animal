package com.feedanimals.mod.util;

import com.feedanimals.mod.FeedAnimalsMod;
import com.feedanimals.mod.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FeedAnimalsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerProgressEvents {
    private static final String STARTER_ITEMS_CLAIMED = "feedanimals_starter_items_claimed";

    private PlayerProgressEvents() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerProgress.initialize(player);
        if (!player.getPersistentData().getBoolean(STARTER_ITEMS_CLAIMED)) {
            ItemStack starterFood = new ItemStack(ModItems.FEED_BASIC.get(), EconomyRules.STARTER_BASIC_FOOD);
            if (!player.getInventory().add(starterFood)) {
                player.drop(starterFood, false);
            }
            player.getPersistentData().putBoolean(STARTER_ITEMS_CLAIMED, true);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Você recebeu " + EconomyRules.STARTER_BASIC_FOOD + " comidas básicas para começar!"));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()
                && event.getOriginal() instanceof ServerPlayer original
                && event.getEntity() instanceof ServerPlayer replacement) {
            PlayerProgress.copyFrom(original, replacement);
        }
    }
}
