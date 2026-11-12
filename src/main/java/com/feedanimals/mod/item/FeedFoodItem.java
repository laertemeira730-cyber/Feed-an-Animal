package com.feedanimals.mod.item;

import com.feedanimals.mod.util.PlayerProgress;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FeedFoodItem extends Item {
    private final int attractionRadius;

    public FeedFoodItem(Properties properties, int attractionRadius) {
        super(properties);
        this.attractionRadius = attractionRadius;
    }

    public int getAttractionRadius() {
        return attractionRadius;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ItemStack thrownStack = held.copyWithCount(1);
            ItemEntity food = new ItemEntity(level, player.getX(),
                    player.getEyeY() - 0.15D, player.getZ(), thrownStack);
            Vec3 look = player.getLookAngle();
            float power = player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    ? PlayerProgress.getThrowPower(serverPlayer) : 0;
            double speed = 0.85D + Math.min(power, PlayerProgress.MAX_THROW_POWER) * 0.015D;
            food.setDeltaMovement(look.scale(speed).add(0.0D, 0.18D, 0.0D));
            food.setPickUpDelay(40);
            food.getPersistentData().putBoolean("feedanimals_thrown_food", true);
            food.getPersistentData().putUUID("feedanimals_thrower", player.getUUID());
            level.addFreshEntity(food);
            level.playSound(null, player.blockPosition(), SoundEvents.SNOWBALL_THROW,
                    SoundSource.PLAYERS, 0.7F, 0.8F + level.random.nextFloat() * 0.4F);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }
}
