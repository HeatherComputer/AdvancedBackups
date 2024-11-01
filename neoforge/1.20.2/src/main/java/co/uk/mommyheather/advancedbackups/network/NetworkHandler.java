package co.uk.mommyheather.advancedbackups.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.SimpleChannel;

public class NetworkHandler {
    
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;
    private static int id() {
        return id++;
    }

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("advancedbackups", "main"),
        () -> PROTOCOL_VERSION,
        (version) -> true,
        (version) -> true
    );


    public static void register() {
        INSTANCE.messageBuilder(PacketBackupStatus.class, id())
        .encoder(PacketBackupStatus::toBytes)
        .decoder(buf -> new PacketBackupStatus(buf))
        .consumerNetworkThread(PacketBackupStatus::handle)
        .add();
        INSTANCE.messageBuilder(PacketToastSubscribe.class, id())
        .encoder(PacketToastSubscribe::toBytes)
        .decoder(buf -> new PacketToastSubscribe(buf))
        .consumerNetworkThread(PacketToastSubscribe::handle)
        .add();
    }

    public static void sendToClient(ServerPlayer player, Object packet) {
        INSTANCE.sendTo(packet, player.connection.connection, PlayNetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

}
