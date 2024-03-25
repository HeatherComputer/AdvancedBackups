package co.uk.mommyheather.advancedbackups.client;

import co.uk.mommyheather.advancedbackups.core.config.ClientConfigManager;
import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import co.uk.mommyheather.advancedbackups.network.PacketToastSubscribe;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientWrapper {

    public static void handle(PacketBackupStatus message) {
        
        BackupToast.starting = message.starting;
        BackupToast.started = message.started;
        BackupToast.failed = message.failed;
        BackupToast.finished = message.finished;
        BackupToast.cancelled = message.cancelled;

        BackupToast.progress = message.progress;
        BackupToast.max = message.max;

        if (!BackupToast.exists) {
            BackupToast.exists = true;
            Minecraft.getInstance().getToasts().addToast(new BackupToast());
        }
    }

    public static void init(FMLClientSetupEvent e) {
        MinecraftForge.EVENT_BUS.addListener(ClientWrapper::registerClientCommands);
        MinecraftForge.EVENT_BUS.addListener(ClientWrapper::onServerConnected);
        ClientConfigManager.loadOrCreateConfig();
    }

    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        AdvancedBackupsClientCommand.register(event.getDispatcher());
    }

    public static void onServerConnected(ClientPlayerNetworkEvent.LoggingIn event) {
        NetworkHandler.INSTANCE.send(new PacketToastSubscribe(ClientConfigManager.showProgress.get()), Minecraft.getInstance().getConnection().getConnection());
        //NetworkHandler.sendToServer(new PacketToastSubscribe(ClientConfigManager.showProgress.get()));
    }
    
}
