package co.uk.mommyheather.advancedbackups.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;

public class NetworkHandler {
    
    private static final String PROTOCOL_VERSION = "1";
    private static int id = 0;
    private static int id() {
        return id++;
    }

    public static final SimpleChannel INSTANCE = ChannelBuilder.named(new ResourceLocation("advancedbackups", "main")).clientAcceptedVersions((status, version) -> true).simpleChannel();


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
        INSTANCE.send(packet, player.connection.getConnection());
    }

}
