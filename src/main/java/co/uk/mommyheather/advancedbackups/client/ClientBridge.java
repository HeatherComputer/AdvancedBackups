package co.uk.mommyheather.advancedbackups.client;

import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.client.Minecraft;

public class ClientBridge {

    public static void handle(PacketBackupStatus message) {

        Minecraft.getMinecraft().addScheduledTask(() -> { 
            BackupToast.starting = message.starting;
            BackupToast.started = message.started;
            BackupToast.failed = message.failed;
            BackupToast.finished = message.finished;
    
            BackupToast.progress = message.progress;
            BackupToast.max = message.max;
    
            if (!BackupToast.exists) {
                BackupToast.exists = true;
                Minecraft.getMinecraft().getToastGui().add(new BackupToast());
            }

        });
               
    }
    
}
