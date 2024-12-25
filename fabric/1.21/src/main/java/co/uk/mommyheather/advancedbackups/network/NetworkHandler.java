package computer.heather.advancedbackups.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class NetworkHandler {

    public static void sendToClient(ServerPlayerEntity player, PacketBackupStatus packet) {

        ServerPlayNetworking.send(player, packet);

    }



}
