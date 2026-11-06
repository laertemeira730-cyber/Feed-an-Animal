package com.feedanimals.mod.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Persiste uma área de cercado e uma área de vendas por jogador e dimensão. */
public class AreaSavedData extends SavedData {
    private static final String DATA_NAME = "feedanimals_areas";

    public enum AreaType {
        PEN("cercado"),
        SALES("vendas");

        private final String displayName;

        AreaType(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private final Map<String, CornerSelection> pendingCorners = new HashMap<>();
    private final Map<String, DefinedArea> definedAreas = new HashMap<>();

    public static AreaSavedData get(ServerLevel level) {
        MinecraftServer server = level.getServer();
        return server.overworld().getDataStorage().computeIfAbsent(
                AreaSavedData::load,
                AreaSavedData::new,
                DATA_NAME
        );
    }

    public void selectCorner(ServerLevel level, ServerPlayer player, AreaType type, BlockPos clickedPos) {
        String dimensionId = level.dimension().location().toString();
        String key = makeKey(player.getUUID(), type, dimensionId);
        CornerSelection pending = pendingCorners.get(key);
        BlockPos clicked = clickedPos.immutable();

        if (pending == null || !pending.dimensionId().equals(dimensionId)) {
            pendingCorners.put(key, new CornerSelection(clicked, dimensionId));
            setDirty();
            player.sendSystemMessage(Component.literal(
                    "Primeiro canto do " + type.displayName() + " definido em " + format(clicked)
                            + ". Clique no bloco do canto oposto."
            ));
            return;
        }

        if (pending.pos().equals(clicked)) {
            player.sendSystemMessage(Component.literal("Escolha outro bloco para marcar o canto oposto."));
            return;
        }

        DefinedArea area = new DefinedArea(
                player.getUUID().toString(), type, pending.pos(), clicked, dimensionId
        );
        definedAreas.put(key, area);
        pendingCorners.remove(key);
        setDirty();

        BlockPos min = area.min();
        BlockPos max = area.max();
        long volume = ((long) max.getX() - min.getX() + 1L)
                * ((long) max.getY() - min.getY() + 1L)
                * ((long) max.getZ() - min.getZ() + 1L);

        player.sendSystemMessage(Component.literal(
                "Área de " + type.displayName() + " definida! De " + format(min)
                        + " até " + format(max) + " (" + volume + " blocos)."
        ));
    }

    public DefinedArea getArea(UUID owner, AreaType type, String dimensionId) {
        return definedAreas.get(makeKey(owner, type, dimensionId));
    }

    private static String makeKey(UUID owner, AreaType type, String dimensionId) {
        return owner + ":" + type.name() + ":" + dimensionId;
    }

    private static String format(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag pendingList = new ListTag();
        pendingCorners.forEach((key, selection) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Key", key);
            entry.put("Pos", NbtUtils.writeBlockPos(selection.pos()));
            entry.putString("Dimension", selection.dimensionId());
            pendingList.add(entry);
        });
        tag.put("PendingCorners", pendingList);

        ListTag areasList = new ListTag();
        definedAreas.forEach((key, area) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Key", key);
            entry.putString("Owner", area.ownerId());
            entry.putString("Type", area.type().name());
            entry.put("First", NbtUtils.writeBlockPos(area.first()));
            entry.put("Second", NbtUtils.writeBlockPos(area.second()));
            entry.putString("Dimension", area.dimensionId());
            areasList.add(entry);
        });
        tag.put("DefinedAreas", areasList);
        return tag;
    }

    public static AreaSavedData load(CompoundTag tag) {
        AreaSavedData data = new AreaSavedData();

        ListTag pendingList = tag.getList("PendingCorners", Tag.TAG_COMPOUND);
        for (int i = 0; i < pendingList.size(); i++) {
            CompoundTag entry = pendingList.getCompound(i);
            data.pendingCorners.put(
                    entry.getString("Key"),
                    new CornerSelection(
                            NbtUtils.readBlockPos(entry.getCompound("Pos")),
                            entry.getString("Dimension")
                    )
            );
        }

        ListTag areasList = tag.getList("DefinedAreas", Tag.TAG_COMPOUND);
        for (int i = 0; i < areasList.size(); i++) {
            CompoundTag entry = areasList.getCompound(i);
            try {
                AreaType type = AreaType.valueOf(entry.getString("Type"));
                DefinedArea area = new DefinedArea(
                        entry.getString("Owner"),
                        type,
                        NbtUtils.readBlockPos(entry.getCompound("First")),
                        NbtUtils.readBlockPos(entry.getCompound("Second")),
                        entry.getString("Dimension")
                );
                data.definedAreas.put(entry.getString("Key"), area);
            } catch (IllegalArgumentException ignored) {
                // Ignora registros de versões antigas ou corrompidos.
            }
        }
        return data;
    }

    private record CornerSelection(BlockPos pos, String dimensionId) {}

    public record DefinedArea(
            String ownerId,
            AreaType type,
            BlockPos first,
            BlockPos second,
            String dimensionId
    ) {
        public BlockPos min() {
            return new BlockPos(
                    Math.min(first.getX(), second.getX()),
                    Math.min(first.getY(), second.getY()),
                    Math.min(first.getZ(), second.getZ())
            );
        }

        public BlockPos max() {
            return new BlockPos(
                    Math.max(first.getX(), second.getX()),
                    Math.max(first.getY(), second.getY()),
                    Math.max(first.getZ(), second.getZ())
            );
        }

        public boolean contains(ServerLevel level, BlockPos pos) {
            if (!dimensionId.equals(level.dimension().location().toString())) {
                return false;
            }
            BlockPos min = min();
            BlockPos max = max();
            return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                    && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                    && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
        }
    }
}
