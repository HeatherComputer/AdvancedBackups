package co.uk.mommyheather.advancedbackups.client;

import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class ClientWrapper implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.STATUS_PACKET_ID, ClientWrapper::handle);
    }

    

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {

        PacketBackupStatus message = new PacketBackupStatus();
        message.read(buf);

        client.execute(() -> {
            BackupToast.starting = message.starting;
            BackupToast.started = message.started;
            BackupToast.failed = message.failed;
            BackupToast.finished = message.finished;
    
            BackupToast.progress = message.progress;
            BackupToast.max = message.max;
    
            if (!BackupToast.exists) {
                BackupToast.exists = true;
                client.getToastManager().add(new BackupToast());
            }

        });


    }
    
}
