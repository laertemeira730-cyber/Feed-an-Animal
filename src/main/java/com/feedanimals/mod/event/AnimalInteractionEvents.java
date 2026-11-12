package com.feedanimals.mod.event;

import com.feedanimals.mod.FeedAnimalsMod;
import com.feedanimals.mod.client.AnimalInfoScreen;
import com.feedanimals.mod.item.FeedFoodItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FeedAnimalsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AnimalInteractionEvents {
    private AnimalInteractionEvents() {}

    @SubscribeEvent
    public static void onAnimalInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Animal animal)
                || event.getEntity().getItemInHand(event.getHand()).getItem() instanceof FeedFoodItem) {
            return;
        }
        Minecraft.getInstance().setScreen(new AnimalInfoScreen(
                animal.getDisplayName().getString(), animal.getHealth(), animal.getMaxHealth()));
        event.setCanceled(true);
    }
}
