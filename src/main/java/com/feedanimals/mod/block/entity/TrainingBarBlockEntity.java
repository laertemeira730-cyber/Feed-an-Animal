package com.feedanimals.mod.block.entity;

import com.feedanimals.mod.registry.ModBlockEntities;
import com.feedanimals.mod.util.PlayerProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TrainingBarBlockEntity extends BlockEntity {
    private int totalTrainingSessions;

    public TrainingBarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAINING_BAR.get(), pos, state);
    }

    public void train(ServerPlayer player) {
        if (this.level == null) {
            return;
        }

        long gameTime = this.level.getGameTime();
        long nextAllowedTick = PlayerProgress.getNextTrainingTick(player);
        if (gameTime < nextAllowedTick) {
            long remainingTicks = nextAllowedTick - gameTime;
            long remainingSeconds = Math.max(1L, (remainingTicks + 19L) / 20L);
            player.displayClientMessage(Component.literal("Aguarde " + remainingSeconds
                    + "s para treinar novamente."), true);
            return;
        }

        int newPower = PlayerProgress.increaseThrowPower(player, 1);
        PlayerProgress.setNextTrainingTick(player, gameTime + 20L);
        this.totalTrainingSessions++;
        this.setChanged();

        player.displayClientMessage(Component.literal("Treino concluído! Força de arremesso: "
                + newPower + "/" + PlayerProgress.MAX_THROW_POWER), true);
    }

    public int getTotalTrainingSessions() {
        return totalTrainingSessions;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TotalTrainingSessions", totalTrainingSessions);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        totalTrainingSessions = tag.getInt("TotalTrainingSessions");
    }
}
