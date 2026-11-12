package com.feedanimals.mod.item;

import com.feedanimals.mod.data.AreaSavedData;
import com.feedanimals.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class AnimalTokenItem extends Item {
    public AnimalTokenItem(Properties properties) {
        super(properties);
    }

    public static ItemStack createToken(Entity animal) {
        ItemStack token = new ItemStack(ModItems.ANIMAL_TOKEN.get());
        CompoundTag data = animal.saveWithoutId(new CompoundTag());
        data.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()).toString());
        data.remove("UUID");
        data.remove("UUIDMost");
        data.remove("UUIDLeast");
        token.getOrCreateTag().put("AnimalData", data);
        token.getOrCreateTag().putString("AnimalName", animal.getDisplayName().getString());
        token.getOrCreateTag().putString("AnimalType", BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()).toString());
        token.getOrCreateTag().putInt("SellValue", 10);
        token.setHoverName(Component.literal("Animal: " + animal.getDisplayName().getString()));
        return token;
    }

    public static int getSellValue(ItemStack token) {
        return Math.max(1, token.getOrCreateTag().getInt("SellValue"));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level instanceof ServerLevel serverLevel)
                || !(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        AreaSavedData.DefinedArea pen = AreaSavedData.get(serverLevel).getArea(
                player.getUUID(), AreaSavedData.AreaType.PEN,
                serverLevel.dimension().location().toString());
        if (pen == null || !pen.contains(serverLevel, spawnPos)) {
            player.sendSystemMessage(Component.literal(
                    "Coloque o animal dentro do seu cercado delimitado."));
            return InteractionResult.CONSUME;
        }

        ItemStack stack = context.getItemInHand();
        if (!stack.hasTag() || !stack.getTag().contains("AnimalData")) {
            player.sendSystemMessage(Component.literal("Este registro de animal está vazio."));
            return InteractionResult.FAIL;
        }

        CompoundTag savedAnimal = stack.getTag().getCompound("AnimalData").copy();
        EntityType.create(savedAnimal, serverLevel).ifPresentOrElse(animal -> {
            animal.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(),
                    spawnPos.getZ() + 0.5D, player.getYRot(), 0.0F);
            if (serverLevel.addFreshEntity(animal)) {
                if (!player.getAbilities().instabuild) stack.shrink(1);
                player.sendSystemMessage(Component.literal(
                        stack.isEmpty() ? "Animal colocado no cercado!" :
                                "Animal colocado no cercado!"));
            } else {
                player.sendSystemMessage(Component.literal("Não foi possível colocar este animal."));
            }
        }, () -> player.sendSystemMessage(Component.literal(
                "Não foi possível recuperar este tipo de animal.")));
        return InteractionResult.CONSUME;
    }
}
