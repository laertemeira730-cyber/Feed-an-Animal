package com.feedanimals.mod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerProgress {
    public static final int MAX_THROW_POWER = 100;
    private static final String ROOT_TAG = "feedanimals_progress";
    private static final String THROW_POWER = "ThrowPower";
    private static final String NEXT_TRAINING_TICK = "NextTrainingTick";
    private static final String MONEY = "Money";
    private static final String AUTO_TRAINING = "AutoTraining";
    private static final String AUTO_TRAINING_POS = "AutoTrainingPos";
    private static final String AUTO_TRAINING_DIMENSION = "AutoTrainingDimension";

    private PlayerProgress() {}

    private static CompoundTag getData(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT_TAG, new CompoundTag());
        }
        return persistent.getCompound(ROOT_TAG);
    }

    public static void initialize(ServerPlayer player) {
        CompoundTag data = getData(player);
        if (!data.contains(MONEY, Tag.TAG_INT)) {
            data.putInt(MONEY, EconomyRules.STARTING_MONEY);
        }
    }

    public static int getMoney(ServerPlayer player) {
        initialize(player);
        return Math.max(0, getData(player).getInt(MONEY));
    }

    public static void addMoney(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        initialize(player);
        CompoundTag data = getData(player);
        data.putInt(MONEY, (int) Math.min(Integer.MAX_VALUE, (long) data.getInt(MONEY) + amount));
    }

    public static boolean spendMoney(ServerPlayer player, int amount) {
        if (amount < 0 || getMoney(player) < amount) return false;
        CompoundTag data = getData(player);
        data.putInt(MONEY, data.getInt(MONEY) - amount);
        return true;
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

    public static void startAutoTraining(ServerPlayer player, BlockPos pos) {
        CompoundTag data = getData(player);
        data.putBoolean(AUTO_TRAINING, true);
        data.putLong(AUTO_TRAINING_POS, pos.asLong());
        data.putString(AUTO_TRAINING_DIMENSION, player.level().dimension().location().toString());
    }

    public static void stopAutoTraining(ServerPlayer player) {
        CompoundTag data = getData(player);
        data.putBoolean(AUTO_TRAINING, false);
        data.remove(AUTO_TRAINING_POS);
        data.remove(AUTO_TRAINING_DIMENSION);
    }

    public static boolean isAutoTraining(ServerPlayer player) {
        return getData(player).getBoolean(AUTO_TRAINING);
    }

    public static BlockPos getAutoTrainingPos(ServerPlayer player) {
        return BlockPos.of(getData(player).getLong(AUTO_TRAINING_POS));
    }

    public static String getAutoTrainingDimension(ServerPlayer player) {
        return getData(player).getString(AUTO_TRAINING_DIMENSION);
    }

    public static void copyFrom(ServerPlayer original, ServerPlayer replacement) {
        CompoundTag oldData = original.getPersistentData().getCompound(ROOT_TAG).copy();
        replacement.getPersistentData().put(ROOT_TAG, oldData);
        String starterKey = "feedanimals_starter_items_claimed";
        if (original.getPersistentData().getBoolean(starterKey)) {
            replacement.getPersistentData().putBoolean(starterKey, true);
        }
    }
}
