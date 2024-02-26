package co.uk.mommyheather.advancedbackups.client;

import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.client.Minecraft;

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
    
}
