package co.uk.mommyheather.advancedbackups.network;

import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
    public static SimpleNetworkWrapper HANDLER;
    private static int packetId = 0;

    
    public static int nextID() {
        return packetId++;
    }

    public static void registerMessages() {
        HANDLER = NetworkRegistry.INSTANCE.newSimpleChannel(AdvancedBackups.MODID);
        // Register messages which are sent from the client to the server here:
        HANDLER.registerMessage(PacketBackupStatus.Handler.class, PacketBackupStatus.class, nextID(), Side.CLIENT);
        HANDLER.registerMessage(PacketToastSubscribe.Handler.class, PacketToastSubscribe.class, nextID(), Side.SERVER);
    }
}
