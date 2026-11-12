package com.feedanimals.mod.event;

import com.feedanimals.mod.FeedAnimalsMod;
import com.feedanimals.mod.block.entity.TrainingBarBlockEntity;
import com.feedanimals.mod.registry.ModBlocks;
import com.feedanimals.mod.util.PlayerProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FeedAnimalsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TrainingAutoEvents {
    private TrainingAutoEvents() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || !PlayerProgress.isAutoTraining(player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)
                || !PlayerProgress.getAutoTrainingDimension(player)
                .equals(level.dimension().location().toString())) {
            PlayerProgress.stopAutoTraining(player);
            return;
        }

        BlockPos pos = PlayerProgress.getAutoTrainingPos(player);
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D,
                pos.getZ() + 0.5D) > 64.0D
                || !level.getBlockState(pos).is(ModBlocks.TRAINING_BAR.get())) {
            PlayerProgress.stopAutoTraining(player);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "Treinamento automático encerrado: aproxime-se da barra."), true);
            return;
        }
        if (PlayerProgress.getThrowPower(player) >= PlayerProgress.MAX_THROW_POWER) {
            PlayerProgress.stopAutoTraining(player);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "Força máxima alcançada!"), true);
            return;
        }

        if (level.getGameTime() % 20L == 0L
                && level.getBlockEntity(pos) instanceof TrainingBarBlockEntity trainingBar) {
            trainingBar.train(player);
        }
    }
}
