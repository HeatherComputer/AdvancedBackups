package co.uk.mommyheather.advancedbackups.network;


import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    

    public static void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1").optional(); //this .optional() seems to be required, but will it be a problem for our packets with non-neo servers...?

        registrar.commonToClient(PacketBackupStatus.ID, PacketBackupStatus.CODEC, PacketBackupStatus::handle);
        registrar.commonToServer(PacketToastSubscribe.ID, PacketToastSubscribe.CODEC, PacketToastSubscribe::handle);
    }

    public static <MSG extends CustomPacketPayload> void sendToClient(ServerPlayer player, MSG message) {
        PacketDistributor.sendToPlayer(player, message);
    }


}
