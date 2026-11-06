package com.feedanimals.mod.network;

import com.feedanimals.mod.FeedAnimalsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PhaseNetwork {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            FeedAnimalsMod.id("main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private PhaseNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, SetPhaseCountPacket.class,
                SetPhaseCountPacket::encode, SetPhaseCountPacket::decode,
                SetPhaseCountPacket::handle);
    }

    public record SetPhaseCountPacket(int count) {
        public static void encode(SetPhaseCountPacket packet, FriendlyByteBuf buffer) {
            buffer.writeVarInt(packet.count);
        }

        public static SetPhaseCountPacket decode(FriendlyByteBuf buffer) {
            return new SetPhaseCountPacket(buffer.readVarInt());
        }

        public static void handle(SetPhaseCountPacket packet,
                                  net.minecraftforge.network.NetworkEvent.Context context) {
            context.enqueueWork(() -> {
                var player = context.getSender();
                if (player == null) return;
                if (packet.count < 2 || packet.count > 64) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "Escolha entre 2 e 64 fases."));
                    return;
                }
                player.getPersistentData().putInt("feedanimals_phase_count", packet.count);
                player.getPersistentData().remove("feedanimals_phase_start");
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Delimitador configurado para " + packet.count
                                + " fases. Clique no primeiro bloco e depois no bloco final."));
            });
            context.setPacketHandled(true);
        }
    }
}
