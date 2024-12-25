package computer.heather.advancedbackups.client;

import computer.heather.advancedbackups.core.CoreCommandSystem;
import computer.heather.advancedbackups.core.config.ClientConfigManager;
import computer.heather.advancedbackups.network.NetworkHandler;
import computer.heather.advancedbackups.network.PacketBackupStatus;
import computer.heather.advancedbackups.network.PacketToastSubscribe;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
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
        MinecraftForge.EVENT_BUS.addListener(ClientWrapper::onClientChat);
        MinecraftForge.EVENT_BUS.addListener(ClientWrapper::onServerConnected);
        ClientConfigManager.loadOrCreateConfig();
    }

    public static void onClientChat(ClientChatEvent event) {
        if (event.getMessage().equals("/backup reload-client-config")) {
            event.setCanceled(true);

            CoreCommandSystem.reloadClientConfig(Minecraft.getInstance().player::chat);
        }

    }

    public static void onServerConnected(ClientPlayerNetworkEvent.LoggedInEvent event) {
        NetworkHandler.sendToServer(new PacketToastSubscribe(ClientConfigManager.showProgress.get()));
    }
    
}
