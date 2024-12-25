package computer.heather.advancedbackups.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class NetworkHandler {
    

    public static void onRegisterPayloadHandler(RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar("advancedbackups")
                .versioned("1.0")
                .optional();

        registrar.play(PacketToastSubscribe.ID, PacketToastSubscribe::new, handler -> handler
                .server(PacketToastSubscribe::handle));
                
        registrar.play(PacketBackupStatus.ID, PacketBackupStatus::new, handler -> handler
                .client(PacketBackupStatus::handle));
    }

    public static <MSG extends CustomPacketPayload> void sendToClient(ServerPlayer player, MSG message) {
        PacketDistributor.PLAYER.with(player).send(message);
    }

    public static <MSG extends CustomPacketPayload> void sendToServer(MSG message) {
        PacketDistributor.SERVER.noArg().send(message);
    }

}
