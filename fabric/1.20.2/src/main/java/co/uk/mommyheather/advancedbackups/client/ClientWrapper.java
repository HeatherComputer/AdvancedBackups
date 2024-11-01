package co.uk.mommyheather.advancedbackups.client;

import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.config.ClientConfigManager;
import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import co.uk.mommyheather.advancedbackups.network.PacketToastSubscribe;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class ClientWrapper implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {

        ABCore.infoLogger = AdvancedBackups.infoLogger;
        ABCore.warningLogger = AdvancedBackups.warningLogger;
        ABCore.errorLogger = AdvancedBackups.errorLogger;

        ClientPlayNetworking.registerGlobalReceiver(NetworkHandler.STATUS_PACKET_ID, ClientWrapper::handle);
        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            ClientConfigManager.loadOrCreateConfig();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            PacketToastSubscribe packet = new PacketToastSubscribe(ClientConfigManager.showProgress.get());
            sender.sendPacket(NetworkHandler.TOAST_SUBSCRIBE_ID, packet.write(PacketByteBufs.create()));
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            AdvancedBackupsClientCommand.register(dispatcher);

        });

        
    }

    

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {

        PacketBackupStatus message = new PacketBackupStatus();
        message.read(buf);

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
    
}
