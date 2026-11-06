package com.feedanimals.mod.util;

import com.feedanimals.mod.FeedAnimalsMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FeedAnimalsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerProgressEvents {
    private PlayerProgressEvents() {}

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()
                && event.getOriginal() instanceof ServerPlayer original
                && event.getEntity() instanceof ServerPlayer replacement) {
            PlayerProgress.copyFrom(original, replacement);
        }
    }
}
