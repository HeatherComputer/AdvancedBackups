package co.uk.mommyheather.advancedbackups.client;

import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class ClientWrapper implements ClientModInitializer {

    public static void handle(MinecraftClient client, PacketBackupStatus message) {
        
        client.execute(() -> {
            BackupToast.starting = message.starting;
            BackupToast.started = message.started;
            BackupToast.failed = message.failed;
            BackupToast.finished = message.finished;
            BackupToast.cancelled = message.cancelled;
    
            BackupToast.progress = message.progress;
            BackupToast.max = message.max;
    
            if (!BackupToast.exists) {
                BackupToast.exists = true;
                client.getToastManager().add(new BackupToast());
            }

        });

    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.STATUS_PACKET_ID, PacketBackupStatus::handle);

    }
}
