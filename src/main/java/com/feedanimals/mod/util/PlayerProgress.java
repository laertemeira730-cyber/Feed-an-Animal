package com.feedanimals.mod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerProgress {
    public static final int MAX_THROW_POWER = 100;
    private static final String ROOT_TAG = "feedanimals_progress";
    private static final String THROW_POWER = "ThrowPower";
    private static final String NEXT_TRAINING_TICK = "NextTrainingTick";

    private PlayerProgress() {}

    private static CompoundTag getData(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT_TAG, new CompoundTag());
        }
        return persistent.getCompound(ROOT_TAG);
    }

    public static int getThrowPower(ServerPlayer player) {
        return Math.max(0, getData(player).getInt(THROW_POWER));
    }

    public static int increaseThrowPower(ServerPlayer player, int amount) {
        CompoundTag data = getData(player);
        int updated = Math.min(MAX_THROW_POWER, getThrowPower(player) + Math.max(0, amount));
        data.putInt(THROW_POWER, updated);
        return updated;
    }

    public static long getNextTrainingTick(ServerPlayer player) {
        return getData(player).getLong(NEXT_TRAINING_TICK);
    }

    public static void setNextTrainingTick(ServerPlayer player, long tick) {
        getData(player).putLong(NEXT_TRAINING_TICK, tick);
    }

    public static void copyFrom(ServerPlayer original, ServerPlayer replacement) {
        CompoundTag oldData = original.getPersistentData().getCompound(ROOT_TAG).copy();
        replacement.getPersistentData().put(ROOT_TAG, oldData);
    }
}
