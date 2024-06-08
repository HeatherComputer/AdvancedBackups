package co.uk.mommyheather.advancedbackups.network;

import co.uk.mommyheather.advancedbackups.client.ClientWrapper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    

    public static void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.commonToClient(PacketBackupStatus.ID, PacketBackupStatus.CODEC, ClientWrapper::handle);
        registrar.commonToServer(PacketToastSubscribe.ID, PacketToastSubscribe.CODEC, PacketToastSubscribe::handle);
    }

    public static <MSG extends CustomPacketPayload> void sendToClient(ServerPlayer player, MSG message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static <MSG extends CustomPacketPayload> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(message);
    }

}
