package computer.heather.advancedbackups.client;

import computer.heather.advancedbackups.core.CoreCommandSystem;
import computer.heather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;

public class ClientBridge {

    public static void handle(PacketBackupStatus message) {

        Minecraft.getMinecraft().addScheduledTask(() -> { 
            BackupToast.starting = message.starting;
            BackupToast.started = message.started;
            BackupToast.failed = message.failed;
            BackupToast.finished = message.finished;
            BackupToast.cancelled = message.cancelled;
    
            BackupToast.progress = message.progress;
            BackupToast.max = message.max;
    
            if (!BackupToast.exists) {
                BackupToast.exists = true;
                Minecraft.getMinecraft().getToastGui().add(new BackupToast());
            }

        });
               
    }

    
    public static void onClientChat(ClientChatEvent event) {
        if (event.getMessage().equals("/backup reload-client-config")) {
            event.setCanceled(true);
            CoreCommandSystem.reloadClientConfig(Minecraft.getMinecraft().player::sendChatMessage);
        }
    }
    
}
