package com.feedanimals.mod.block;

import com.feedanimals.mod.data.AreaSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

public class AreaMarkerBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2, 0, 2, 14, 3, 14),
            Block.box(5, 3, 5, 11, 13, 11),
            Block.box(3, 13, 3, 13, 16, 13)
    );

    private final AreaSavedData.AreaType areaType;

    public AreaMarkerBlock(AreaSavedData.AreaType areaType, Properties properties) {
        super(properties);
        this.areaType = areaType;
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                               BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            AreaSavedData.get(serverLevel).selectCorner(serverLevel, serverPlayer, areaType, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
