package co.uk.mommyheather.advancedbackups.client;

import java.util.Objects;

import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.config.ClientConfigManager;
import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import co.uk.mommyheather.advancedbackups.network.PacketToastSubscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public class ClientWrapper {

    public static void handle(PacketBackupStatus packet, IPayloadContext context) {
        BackupToast.starting = packet.starting();
        BackupToast.started = packet.started();
        BackupToast.failed = packet.failed();
        BackupToast.finished = packet.finished();
        BackupToast.cancelled = packet.cancelled();

        BackupToast.progress = packet.progress();
        BackupToast.max = packet.max();

        if (!BackupToast.exists) {
            BackupToast.exists = true;
            Minecraft.getInstance().getToasts().addToast(new BackupToast());
        }
    }

    public static void init(FMLClientSetupEvent e) {
        NeoForge.EVENT_BUS.addListener(ClientWrapper::registerClientCommands);
        NeoForge.EVENT_BUS.addListener(ClientWrapper::onServerConnected);
        ClientConfigManager.loadOrCreateConfig();
    }

    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        AdvancedBackupsClientCommand.register(event.getDispatcher());
    }

    public static void onServerConnected(ClientPlayerNetworkEvent.LoggingIn event) {
        sendToServer(new PacketToastSubscribe(ClientConfigManager.showProgress.get()));
    }

    
    public static <MSG extends CustomPacketPayload> void sendToServer(MSG message) {
        //We do this to implement custom checks!
        ServerboundCustomPayloadPacket packet = new ServerboundCustomPayloadPacket(message);
        
        ClientPacketListener listener = Objects.requireNonNull(Minecraft.getInstance().getConnection());
        try {
            NetworkRegistry.checkPacket(packet, listener);
        }
        catch (UnsupportedOperationException e) {
            ABCore.warningLogger.accept("Refusing to send packet " + message + " to server as the serve cannot receive it.");
            return;
        }
        listener.connection.send(packet);
    }
    
}
