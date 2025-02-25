package computer.heather.advancedbackups.client;

import computer.heather.advancedbackups.core.config.ClientConfigManager;
import computer.heather.advancedbackups.network.NetworkHandler;
import computer.heather.advancedbackups.network.PacketBackupStatus;
import computer.heather.advancedbackups.network.PacketToastSubscribe;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientWrapper {

    public static void handle(PacketBackupStatus packet) {

        BackupToast.starting = packet.starting;
        BackupToast.started = packet.started;
        BackupToast.failed = packet.failed;
        BackupToast.finished = packet.finished;

        BackupToast.cancelled = packet.cancelled;

        BackupToast.progress = packet.progress;
        BackupToast.max = packet.max;

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
        NetworkHandler.sendToServer(new PacketToastSubscribe(ClientConfigManager.showProgress.get()));
    }
    
}
