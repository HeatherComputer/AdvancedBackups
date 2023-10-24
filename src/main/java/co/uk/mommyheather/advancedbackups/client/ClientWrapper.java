package co.uk.mommyheather.advancedbackups.client;

import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.client.Minecraft;

public class ClientWrapper {

    public static void handle(PacketBackupStatus packetBackupStatus) {
        if (!BackupToast.exists) {
            BackupToast.exists = true;
            Minecraft.getInstance().getToasts().addToast(new BackupToast());
        }
    }
    
}
