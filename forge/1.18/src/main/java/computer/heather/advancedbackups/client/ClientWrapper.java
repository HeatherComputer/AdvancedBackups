package computer.heather.advancedbackups.client;

import java.io.IOException;

import computer.heather.advancedbackups.core.ABCore;
import computer.heather.advancedbackups.core.CoreCommandSystem;
import computer.heather.advancedbackups.core.config.ClientConfigManager;
import computer.heather.advancedbackups.network.NetworkHandler;
import computer.heather.advancedbackups.network.PacketBackupStatus;
import computer.heather.advancedbackups.network.PacketToastSubscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
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

    public static void init(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ClientWrapper::onClientChat);
        MinecraftForge.EVENT_BUS.addListener(ClientWrapper::onServerConnected);
        try {
            ClientConfigManager.loadOrCreateConfig();
        } catch (IOException e) {
            ABCore.errorLogger.accept("Unable to load client config! Default will be used...");
            ABCore.logStackTrace(e);
        }
    }

    public static void onClientChat(ClientChatEvent event) {
        if (event.getMessage().equals("/backup reload-client-config")) {
            event.setCanceled(true);

            try {
                CoreCommandSystem.reloadClientConfig((response) -> {
                    Minecraft.getInstance().player.sendMessage(new TextComponent(response), null);
                });
            } catch (IOException e) {
                Minecraft.getInstance().player.sendMessage(new TextComponent("Command failed to execute! Check log for error"), null);
                ABCore.errorLogger.accept("Error reloading client config :");
            }
        }

    }

    public static void onServerConnected(ClientPlayerNetworkEvent.LoggedInEvent event) {
        NetworkHandler.sendToServer(new PacketToastSubscribe(ClientConfigManager.showProgress.get()));
    }
    
}
