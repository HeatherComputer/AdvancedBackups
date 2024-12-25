package computer.heather.advancedbackups.client;

import computer.heather.advancedbackups.AdvancedBackups;
import computer.heather.advancedbackups.core.ABCore;
import computer.heather.advancedbackups.core.config.ClientConfigManager;
import computer.heather.advancedbackups.network.PacketBackupStatus;
import computer.heather.advancedbackups.network.PacketToastSubscribe;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class ClientWrapper implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        
        ABCore.infoLogger = AdvancedBackups.infoLogger;
        ABCore.warningLogger = AdvancedBackups.warningLogger;
        ABCore.errorLogger = AdvancedBackups.errorLogger;
        
        ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
            ClientConfigManager.loadOrCreateConfig();
        });
        
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            PacketToastSubscribe packet = new PacketToastSubscribe(ClientConfigManager.showProgress.get());
            if (ClientPlayNetworking.canSend(packet.getId())) {
                //Make sure a server can receive the packet before trying to send!
                ClientPlayNetworking.send(packet);
            }
            else {
                ABCore.warningLogger.accept("Refusing to send packet " + packet + " as the server cannot accept it!");
            }
        });
        
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            AdvancedBackupsClientCommand.register(dispatcher);
            
        });
        
        
        ClientPlayNetworking.registerGlobalReceiver(PacketBackupStatus.ID, ClientWrapper::handle);
        
        
    }
    
    
    
    public static void handle(PacketBackupStatus message, ClientPlayNetworking.Context context) {
        

        
        MinecraftClient.getInstance().execute(() -> {
            BackupToast.starting = message.starting();
            BackupToast.started = message.started();
            BackupToast.failed = message.failed();
            BackupToast.finished = message.finished();
            BackupToast.cancelled = message.cancelled();
            
            BackupToast.progress = message.progress();
            BackupToast.max = message.max();
            
            if (!BackupToast.exists) {
                BackupToast.exists = true;
                MinecraftClient.getInstance().getToastManager().add(new BackupToast());
            }
            
        });
        
        
    }
    
}
