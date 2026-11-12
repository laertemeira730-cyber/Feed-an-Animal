package com.feedanimals.mod.item;

import com.feedanimals.mod.client.SystemMenuScreen;
import com.feedanimals.mod.data.AreaSavedData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class AreaMarkerItem extends BlockItem {
    private final AreaSavedData.AreaType type;

    public AreaMarkerItem(Block block, Properties properties, AreaSavedData.AreaType type) {
        super(block, properties);
        this.type = type;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SystemMenuScreen.open(
                    type == AreaSavedData.AreaType.PEN
                            ? SystemMenuScreen.MenuType.PEN : SystemMenuScreen.MenuType.SALES));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
