package com.feedanimals.mod.item;

import com.feedanimals.mod.data.PhaseSavedData;
import com.feedanimals.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class PhaseMarkerItem extends Item {
    public PhaseMarkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(context.getPlayer() instanceof ServerPlayer player)
                || context.getHand() != InteractionHand.MAIN_HAND
                || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        int count = player.getPersistentData().getInt("feedanimals_phase_count");
        if (count < 2) {
            player.sendSystemMessage(Component.literal(
                    "Primeiro configure a quantidade de fases: use o item com o botão direito no ar."));
            return InteractionResult.CONSUME;
        }

        BlockPos point = context.getClickedPos().relative(context.getClickedFace()).immutable();
        String dimension = level.dimension().location().toString();
        var data = PhaseSavedData.get(serverLevel);

        if (!player.getPersistentData().contains("feedanimals_phase_start")) {
            player.getPersistentData().putLong("feedanimals_phase_start", point.asLong());
            player.getPersistentData().putString("feedanimals_phase_dimension", dimension);
            player.sendSystemMessage(Component.literal(
                    "Ponto inicial marcado em " + format(point)
                            + ". Agora clique no bloco final."));
            return InteractionResult.CONSUME;
        }

        if (!dimension.equals(player.getPersistentData().getString("feedanimals_phase_dimension"))) {
            player.getPersistentData().remove("feedanimals_phase_start");
            player.sendSystemMessage(Component.literal("A seleção foi cancelada: dimensão diferente."));
            return InteractionResult.CONSUME;
        }

        BlockPos start = BlockPos.of(player.getPersistentData().getLong("feedanimals_phase_start"));
        PhaseSavedData.Validation validation = data.validateLine(start, point, count);
        if (!validation.valid()) {
            player.sendSystemMessage(Component.literal(validation.message()));
            player.sendSystemMessage(Component.literal(
                    "Distâncias válidas mais próximas: " + validation.lowerValidDistance()
                            + " ou " + validation.upperValidDistance() + " passos; cada fase terá "
                            + (validation.lowerValidDistance() / count) + " ou "
                            + (validation.upperValidDistance() / count) + " passos."));
            player.sendSystemMessage(Component.literal(
                    "O ponto inicial foi mantido. Escolha outro ponto final."));
            return InteractionResult.CONSUME;
        }

        boolean created = data.createProject(player.getUUID(), dimension, start, point, count);
        if (!created) {
            player.sendSystemMessage(Component.literal("Não foi possível criar o projeto."));
            return InteractionResult.CONSUME;
        }

        if (!serverLevel.getBlockState(start).canBeReplaced()) {
            player.sendSystemMessage(Component.literal(
                    "A divisão foi salva, mas o bloco inicial está ocupado. O marcador não foi colocado."));
        } else {
            serverLevel.setBlock(start, ModBlocks.PHASE_MARKER.get().defaultBlockState(), 3);
        }
        player.getPersistentData().remove("feedanimals_phase_start");
        player.getPersistentData().remove("feedanimals_phase_dimension");
        player.sendSystemMessage(Component.literal(
                "Projeto criado! " + count + " fases de " + validation.stepsPerPhase()
                        + " passos cada. O marcador foi colocado no ponto inicial."));
        context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<net.minecraft.world.item.ItemStack> use(
            Level level, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> 
                    com.feedanimals.mod.client.PhaseClient.open());
        }
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(
                player.getItemInHand(hand), level.isClientSide);
    }

    private static String format(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }
}
