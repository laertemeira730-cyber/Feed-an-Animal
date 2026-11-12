package com.feedanimals.mod.client;

import com.feedanimals.mod.FeedAnimalsMod;
import com.feedanimals.mod.network.GameNetwork;
import com.feedanimals.mod.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = FeedAnimalsMod.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TrainingInputEvents {
    private TrainingInputEvents() {}

    @SubscribeEvent
    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem() || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND
                || !Screen.hasControlDown()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !(minecraft.hitResult instanceof BlockHitResult hit)) return;
        BlockPos pos = hit.getBlockPos();
        if (!minecraft.level.getBlockState(pos).is(ModBlocks.TRAINING_BAR.get())) return;

        event.setCanceled(true);
        event.setSwingHand(true);
        GameNetwork.CHANNEL.sendToServer(new GameNetwork.StartAutoTrainingPacket(pos));
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS
                && (event.getKey() == GLFW.GLFW_KEY_LEFT_SHIFT
                || event.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            GameNetwork.CHANNEL.sendToServer(new GameNetwork.StopAutoTrainingPacket());
        }
    }
}
