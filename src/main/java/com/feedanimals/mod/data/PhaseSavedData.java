package com.feedanimals.mod.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dados persistentes dos projetos de construção divididos em fases.
 *
 * A divisão usa os passos inteiros da linha entre os pontos. Assim, uma linha
 * só é aceita quando o número de passos pode ser dividido igualmente pela
 * quantidade de fases.
 */
public class PhaseSavedData extends SavedData {
    private static final String DATA_NAME = "feedanimals_phases";
    private final Map<String, PhaseProject> projects = new HashMap<>();

    public static PhaseSavedData get(ServerLevel level) {
        MinecraftServer server = level.getServer();
        return server.overworld().getDataStorage().computeIfAbsent(
                PhaseSavedData::load,
                PhaseSavedData::new,
                DATA_NAME
        );
    }

    public Validation validateLine(BlockPos start, BlockPos end, int phaseCount) {
        if (phaseCount < 1) {
            return Validation.invalid("A quantidade de fases precisa ser maior que zero.", 0, 0);
        }

        long dx = Math.abs((long) end.getX() - start.getX());
        long dy = Math.abs((long) end.getY() - start.getY());
        long dz = Math.abs((long) end.getZ() - start.getZ());
        long steps = gcd(gcd(dx, dy), dz);

        if (steps == 0) {
            return Validation.invalid("O ponto final precisa ser diferente do inicial.", 0, 0);
        }

        if (steps % phaseCount != 0) {
            long lower = (steps / phaseCount) * phaseCount;
            long upper = lower + phaseCount;
            return Validation.invalid(
                    "A distância não pode ser dividida igualmente por " + phaseCount + " fases.",
                    lower,
                    upper
            );
        }

        return Validation.valid(steps, steps / phaseCount);
    }

    public boolean createProject(UUID owner, String dimensionId, BlockPos start,
                                 BlockPos end, int phaseCount) {
        Validation validation = validateLine(start, end, phaseCount);
        if (!validation.valid()) {
            return false;
        }

        String key = makeKey(owner, dimensionId);
        List<Boolean> completed = new ArrayList<>();
        for (int i = 0; i < phaseCount; i++) {
            completed.add(false);
        }

        projects.put(key, new PhaseProject(
                owner.toString(), dimensionId, start.immutable(), end.immutable(),
                phaseCount, validation.stepsPerPhase(), completed
        ));
        setDirty();
        return true;
    }

    public PhaseProject getProject(UUID owner, String dimensionId) {
        return projects.get(makeKey(owner, dimensionId));
    }

    public boolean setPhaseCompleted(UUID owner, String dimensionId, int phaseIndex,
                                     boolean completed) {
        String key = makeKey(owner, dimensionId);
        PhaseProject project = projects.get(key);
        if (project == null || phaseIndex < 0 || phaseIndex >= project.phaseCount()) {
            return false;
        }

        List<Boolean> updated = new ArrayList<>(project.completed());
        updated.set(phaseIndex, completed);
        projects.put(key, project.withCompleted(updated));
        setDirty();
        return true;
    }

    private static String makeKey(UUID owner, String dimensionId) {
        return owner + ":" + dimensionId;
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long remainder = a % b;
            a = b;
            b = remainder;
        }
        return a;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        projects.forEach((key, project) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Key", key);
            entry.putString("Owner", project.ownerId());
            entry.putString("Dimension", project.dimensionId());
            entry.put("Start", NbtUtils.writeBlockPos(project.start()));
            entry.put("End", NbtUtils.writeBlockPos(project.end()));
            entry.putInt("PhaseCount", project.phaseCount());
            entry.putLong("StepsPerPhase", project.stepsPerPhase());

            ListTag completed = new ListTag();
            for (boolean value : project.completed()) {
                CompoundTag phase = new CompoundTag();
                phase.putBoolean("Done", value);
                completed.add(phase);
            }
            entry.put("Completed", completed);
            list.add(entry);
        });
        tag.put("Projects", list);
        return tag;
    }

    public static PhaseSavedData load(CompoundTag tag) {
        PhaseSavedData data = new PhaseSavedData();
        ListTag list = tag.getList("Projects", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            try {
                String ownerId = entry.getString("Owner");
                String dimension = entry.getString("Dimension");
                int count = entry.getInt("PhaseCount");
                if (count < 1 || count > 256) {
                    continue;
                }

                List<Boolean> completed = new ArrayList<>();
                ListTag phaseList = entry.getList("Completed", Tag.TAG_COMPOUND);
                for (int phase = 0; phase < count; phase++) {
                    completed.add(phase < phaseList.size()
                            && phaseList.getCompound(phase).getBoolean("Done"));
                }

                PhaseProject project = new PhaseProject(
                        ownerId, dimension,
                        NbtUtils.readBlockPos(entry.getCompound("Start")),
                        NbtUtils.readBlockPos(entry.getCompound("End")),
                        count, entry.getLong("StepsPerPhase"), completed
                );
                data.projects.put(entry.getString("Key"), project);
            } catch (RuntimeException ignored) {
                // Ignora apenas o projeto inválido; os demais continuam carregando.
            }
        }
        return data;
    }

    public record Validation(boolean valid, String message, long totalSteps,
                             long stepsPerPhase, long lowerValidDistance,
                             long upperValidDistance) {
        private static Validation invalid(String message, long lower, long upper) {
            return new Validation(false, message, 0, 0, lower, upper);
        }

        private static Validation valid(long total, long perPhase) {
            return new Validation(true, "", total, perPhase, total, total);
        }
    }

    public record PhaseProject(String ownerId, String dimensionId, BlockPos start,
                               BlockPos end, int phaseCount, long stepsPerPhase,
                               List<Boolean> completed) {
        public PhaseProject {
            completed = List.copyOf(completed);
        }

        public boolean allCompleted() {
            return completed.stream().allMatch(Boolean::booleanValue);
        }

        public PhaseProject withCompleted(List<Boolean> values) {
            return new PhaseProject(ownerId, dimensionId, start, end,
                    phaseCount, stepsPerPhase, values);
        }

        /** Retorna os dois extremos inclusivos da fase, sem lacunas entre fases. */
        public BlockPos[] phaseBounds(int index) {
            if (index < 0 || index >= phaseCount) {
                throw new IndexOutOfBoundsException("Índice de fase inválido: " + index);
            }

            long dx = (long) end.getX() - start.getX();
            long dy = (long) end.getY() - start.getY();
            long dz = (long) end.getZ() - start.getZ();
            long divisor = gcd(gcd(Math.abs(dx), Math.abs(dy)), Math.abs(dz));
            long stepX = dx / divisor;
            long stepY = dy / divisor;
            long stepZ = dz / divisor;

            long firstStep = (long) index * stepsPerPhase;
            long lastStep = firstStep + stepsPerPhase;
            BlockPos first = new BlockPos(
                    (int) (start.getX() + stepX * firstStep),
                    (int) (start.getY() + stepY * firstStep),
                    (int) (start.getZ() + stepZ * firstStep)
            );
            BlockPos last = new BlockPos(
                    (int) (start.getX() + stepX * lastStep),
                    (int) (start.getY() + stepY * lastStep),
                    (int) (start.getZ() + stepZ * lastStep)
            );
            return new BlockPos[]{first, last};
        }
    }
}
