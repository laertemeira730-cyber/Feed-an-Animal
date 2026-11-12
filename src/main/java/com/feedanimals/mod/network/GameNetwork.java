package com.feedanimals.mod.network;

import com.feedanimals.mod.FeedAnimalsMod;
import com.feedanimals.mod.block.entity.TrainingBarBlockEntity;
import com.feedanimals.mod.data.AreaSavedData;
import com.feedanimals.mod.data.PhaseSavedData;
import com.feedanimals.mod.item.AnimalTokenItem;
import com.feedanimals.mod.registry.ModBlocks;
import com.feedanimals.mod.registry.ModItems;
import com.feedanimals.mod.util.EconomyRules;
import com.feedanimals.mod.util.PlayerProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class GameNetwork {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            FeedAnimalsMod.id("game"), () -> VERSION, VERSION::equals, VERSION::equals);

    private GameNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, RequestStatusPacket.class,
                RequestStatusPacket::encode, RequestStatusPacket::decode, RequestStatusPacket::handle);
        CHANNEL.registerMessage(1, PlayerStatusPacket.class,
                PlayerStatusPacket::encode, PlayerStatusPacket::decode, PlayerStatusPacket::handle);
        CHANNEL.registerMessage(2, TrainOncePacket.class,
                TrainOncePacket::encode, TrainOncePacket::decode, TrainOncePacket::handle);
        CHANNEL.registerMessage(3, StartAutoTrainingPacket.class,
                StartAutoTrainingPacket::encode, StartAutoTrainingPacket::decode, StartAutoTrainingPacket::handle);
        CHANNEL.registerMessage(4, StopAutoTrainingPacket.class,
                StopAutoTrainingPacket::encode, StopAutoTrainingPacket::decode, StopAutoTrainingPacket::handle);
        CHANNEL.registerMessage(5, PurchaseFoodPacket.class,
                PurchaseFoodPacket::encode, PurchaseFoodPacket::decode, PurchaseFoodPacket::handle);
        CHANNEL.registerMessage(6, SellAnimalsPacket.class,
                SellAnimalsPacket::encode, SellAnimalsPacket::decode, SellAnimalsPacket::handle);
        CHANNEL.registerMessage(7, SetPhaseDonePacket.class,
                SetPhaseDonePacket::encode, SetPhaseDonePacket::decode, SetPhaseDonePacket::handle);
    }

    public static void requestStatus() {
        CHANNEL.sendToServer(new RequestStatusPacket());
    }

    public static void refreshStatus(ServerPlayer player) {
        sendStatus(player);
    }

    private static void sendStatus(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int tokens = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof AnimalTokenItem) tokens += stack.getCount();
        }

        int penAnimals = 0;
        AreaSavedData.DefinedArea pen = AreaSavedData.get(level).getArea(
                player.getUUID(), AreaSavedData.AreaType.PEN,
                level.dimension().location().toString());
        if (pen != null) {
            BlockPos min = pen.min();
            BlockPos max = pen.max();
            AABB bounds = new AABB(min.getX(), min.getY(), min.getZ(),
                    max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D);
            penAnimals = level.getEntitiesOfClass(Animal.class, bounds).size();
        }

        PhaseSavedData.PhaseProject project = PhaseSavedData.get(level).getProject(
                player.getUUID(), level.dimension().location().toString());
        int phaseCount = project == null ? 0 : project.phaseCount();
        int completed = project == null ? 0 : (int) project.completed().stream()
                .filter(Boolean::booleanValue).count();
        StringBuilder flags = new StringBuilder();
        if (project != null) {
            for (boolean done : project.completed()) flags.append(done ? '1' : '0');
        }

        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new PlayerStatusPacket(PlayerProgress.getMoney(player),
                        PlayerProgress.getThrowPower(player), tokens, penAnimals,
                        phaseCount, completed, flags.toString()));
    }

    private static void trainAt(ServerPlayer player, BlockPos pos) {
        if (!(player.level() instanceof ServerLevel level)
                || player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D
                || !level.getBlockState(pos).is(ModBlocks.TRAINING_BAR.get())) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof TrainingBarBlockEntity trainingBar) {
            trainingBar.train(player);
            sendStatus(player);
        }
    }

    public record RequestStatusPacket() {
        public static void encode(RequestStatusPacket packet, FriendlyByteBuf buffer) {}
        public static RequestStatusPacket decode(FriendlyByteBuf buffer) {
            return new RequestStatusPacket();
        }
        public static void handle(RequestStatusPacket packet, Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) sendStatus(player);
            });
            context.setPacketHandled(true);
        }
    }

    public record PlayerStatusPacket(int money, int throwPower, int animalTokens,
                                     int penAnimals, int phaseCount, int completedPhases,
                                     String phaseFlags) {
        public static void encode(PlayerStatusPacket packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.money);
            buffer.writeVarInt(packet.throwPower);
            buffer.writeVarInt(packet.animalTokens);
            buffer.writeVarInt(packet.penAnimals);
            buffer.writeVarInt(packet.phaseCount);
            buffer.writeVarInt(packet.completedPhases);
            buffer.writeUtf(packet.phaseFlags, 256);
        }
        public static PlayerStatusPacket decode(FriendlyByteBuf buffer) {
            return new PlayerStatusPacket(buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readUtf(256));
        }
        public static void handle(PlayerStatusPacket packet, Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> com.feedanimals.mod.client.ClientGameData.apply(
                            packet.money, packet.throwPower, packet.animalTokens,
                            packet.penAnimals, packet.phaseCount, packet.completedPhases,
                            packet.phaseFlags)));
            context.setPacketHandled(true);
        }
    }

    public record TrainOncePacket(BlockPos pos) {
        public static void encode(TrainOncePacket packet, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.pos);
        }
        public static TrainOncePacket decode(FriendlyByteBuf buffer) {
            return new TrainOncePacket(buffer.readBlockPos());
        }
        public static void handle(TrainOncePacket packet, Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) trainAt(player, packet.pos);
            });
            context.setPacketHandled(true);
        }
    }

    public record StartAutoTrainingPacket(BlockPos pos) {
        public static void encode(StartAutoTrainingPacket packet, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.pos);
        }
        public static StartAutoTrainingPacket decode(FriendlyByteBuf buffer) {
            return new StartAutoTrainingPacket(buffer.readBlockPos());
        }
        public static void handle(StartAutoTrainingPacket packet, Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || !(player.level() instanceof ServerLevel level)
                        || player.distanceToSqr(packet.pos.getX() + 0.5D, packet.pos.getY() + 0.5D,
                        packet.pos.getZ() + 0.5D) > 64.0D
                        || !level.getBlockState(packet.pos).is(ModBlocks.TRAINING_BAR.get())) return;
                PlayerProgress.startAutoTraining(player, packet.pos);
                player.displayClientMessage(Component.literal(
                        "Treinamento automático iniciado. Pressione Shift para parar."), true);
            });
            context.setPacketHandled(true);
        }
    }

    public record StopAutoTrainingPacket() {
        public static void encode(StopAutoTrainingPacket packet, FriendlyByteBuf buffer) {}
        public static StopAutoTrainingPacket decode(FriendlyByteBuf buffer) {
            return new StopAutoTrainingPacket();
        }
        public static void handle(StopAutoTrainingPacket packet, Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player != null) {
                    PlayerProgress.stopAutoTraining(player);
                    player.displayClientMessage(Component.literal("Treinamento automático interrompido."), true);
                }
            });
            context.setPacketHandled(true);
        }
    }

    public record PurchaseFoodPacket(int foodId, int amount) {
        public static void encode(PurchaseFoodPacket packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.foodId);
            buffer.writeVarInt(packet.amount);
        }
        public static PurchaseFoodPacket decode(FriendlyByteBuf buffer) {
            return new PurchaseFoodPacket(buffer.readVarInt(), buffer.readVarInt());
        }
        public static void handle(PurchaseFoodPacket packet, Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null || packet.amount < 1 || packet.amount > 64) return;
                Item item;
                int price;
                switch (packet.foodId) {
                    case 0 -> { item = ModItems.FEED_BASIC.get(); price = EconomyRules.BASIC_FOOD_PRICE; }
                    case 1 -> { item = ModItems.FEED_RARE.get(); price = EconomyRules.RARE_FOOD_PRICE; }
                    case 2 -> { item = ModItems.FEED_PREMIUM.get(); price = EconomyRules.PREMIUM_FOOD_PRICE; }
                    default -> { return; }
                }
                long total = (long) price * packet.amount;
                if (total > Integer.MAX_VALUE || !PlayerProgress.spendMoney(player, (int) total)) {
                    player.displayClientMessage(Component.literal("Dinheiro insuficiente."), true);
                    sendStatus(player);
                    return;
                }
                ItemStack purchased = new ItemStack(item, packet.amount);
                if (!player.getInventory().add(purchased)) player.drop(purchased, false);
                player.displayClientMessage(Component.literal(
                        "Compra concluída: " + packet.amount + " alimento(s)."), true);
                sendStatus(player);
            });
            context.setPacketHandled(true);
        }
    }

    public record SellAnimalsPacket(boolean all) {
        public static void encode(SellAnimalsPacket packet, FriendlyByteBuf buffer) {
            buffer.writeBoolean(packet.all);
        }
        public static SellAnimalsPacket decode(FriendlyByteBuf buffer) {
            return new SellAnimalsPacket(buffer.readBoolean());
        }
        public static void handle(SellAnimalsPacket packet, Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                AreaSavedData.DefinedArea sales = AreaSavedData.get(player.serverLevel()).getArea(
                        player.getUUID(), AreaSavedData.AreaType.SALES,
                        player.level().dimension().location().toString());
                if (sales == null) {
                    player.displayClientMessage(Component.literal(
                            "Primeiro delimite sua área de vendas usando dois marcadores."), true);
                    sendStatus(player);
                    return;
                }

                int earned = 0;
                int remaining = packet.all ? Integer.MAX_VALUE : 1;
                for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!(stack.getItem() instanceof AnimalTokenItem)) continue;
                    while (!stack.isEmpty() && remaining > 0) {
                        earned += AnimalTokenItem.getSellValue(stack);
                        stack.shrink(1);
                        remaining--;
                    }
                }
                if (earned > 0) {
                    PlayerProgress.addMoney(player, earned);
                    player.displayClientMessage(Component.literal("Animais vendidos por " + earned + " moedas."), true);
                } else {
                    player.displayClientMessage(Component.literal("Você não tem animais na mochila para vender."), true);
                }
                sendStatus(player);
            });
            context.setPacketHandled(true);
        }
    }

    public record SetPhaseDonePacket(int phaseIndex) {
        public static void encode(SetPhaseDonePacket packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.phaseIndex);
        }
        public static SetPhaseDonePacket decode(FriendlyByteBuf buffer) {
            return new SetPhaseDonePacket(buffer.readVarInt());
        }
        public static void handle(SetPhaseDonePacket packet, Supplier<net.minecraftforge.network.NetworkEvent.Context> supplier) {
            var context = supplier.get();
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) return;
                PhaseSavedData data = PhaseSavedData.get(player.serverLevel());
                boolean changed = data.setPhaseCompleted(player.getUUID(),
                        player.level().dimension().location().toString(), packet.phaseIndex, true);
                if (changed) player.displayClientMessage(Component.literal(
                        "Fase " + (packet.phaseIndex + 1) + " concluída!"), true);
                sendStatus(player);
            });
            context.setPacketHandled(true);
        }
    }
}
