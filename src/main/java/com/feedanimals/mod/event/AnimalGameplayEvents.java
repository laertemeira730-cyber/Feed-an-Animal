package com.feedanimals.mod.event;

import com.feedanimals.mod.FeedAnimalsMod;
import com.feedanimals.mod.data.AreaSavedData;
import com.feedanimals.mod.item.AnimalTokenItem;
import com.feedanimals.mod.item.FeedFoodItem;
import com.feedanimals.mod.util.EconomyRules;
import com.feedanimals.mod.util.PlayerProgress;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = FeedAnimalsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AnimalGameplayEvents {
    private AnimalGameplayEvents() {}

    @SubscribeEvent
    public static void onAnimalTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Animal animal)
                || !(animal.level() instanceof ServerLevel level)
                || animal.tickCount % 5 != 0) {
            return;
        }

        for (ItemEntity foodEntity : level.getEntitiesOfClass(ItemEntity.class,
                animal.getBoundingBox().inflate(16.0D),
                item -> item.getPersistentData().getBoolean("feedanimals_thrown_food"))) {
            if (!(foodEntity.getItem().getItem() instanceof FeedFoodItem food)) continue;
            double radius = food.getAttractionRadius();
            if (animal.distanceToSqr(foodEntity) > radius * radius) continue;
            if (!foodEntity.getPersistentData().hasUUID("feedanimals_thrower")) continue;

            UUID ownerId = foodEntity.getPersistentData().getUUID("feedanimals_thrower");
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
            if (owner == null || owner.level() != level) continue;

            var token = AnimalTokenItem.createToken(animal);
            if (!owner.getInventory().add(token)) {
                owner.drop(token, false);
            }
            owner.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    animal.getDisplayName().getString() + " foi atraído e coletado!"));
            com.feedanimals.mod.network.GameNetwork.refreshStatus(owner);
            animal.discard();
            foodEntity.discard();
            break;
        }
    }

    @SubscribeEvent
    public static void onPlayerIncome(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel level)
                || level.getGameTime() % EconomyRules.PEN_INCOME_INTERVAL_TICKS != 0) {
            return;
        }

        AreaSavedData.DefinedArea pen = AreaSavedData.get(level).getArea(
                player.getUUID(), AreaSavedData.AreaType.PEN,
                level.dimension().location().toString());
        if (pen == null) return;

        var min = pen.min();
        var max = pen.max();
        AABB bounds = new AABB(min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D);
        int animals = level.getEntitiesOfClass(Animal.class, bounds).size();
        if (animals > 0) {
            int earned = animals * EconomyRules.COINS_PER_ANIMAL;
            PlayerProgress.addMoney(player, earned);
            com.feedanimals.mod.network.GameNetwork.refreshStatus(player);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "+" + earned + " moedas pelo seu cercado (" + animals + " animais)."), true);
        }
    }
}
