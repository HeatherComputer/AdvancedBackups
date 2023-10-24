package co.uk.mommyheather.advancedbackups.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class NetworkHandler {

    public static final Identifier STATUS_PACKET_ID = new Identifier("advancedbackups", "backup_status");


    public static void sendToClient(ServerPlayerEntity player, PacketBackupStatus packet) {

        ServerPlayNetworking.send(player, STATUS_PACKET_ID, packet.write(PacketByteBufs.create()));

    }



}
