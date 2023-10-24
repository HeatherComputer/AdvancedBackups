package co.uk.mommyheather.advancedbackups.client;

import java.util.function.Supplier;

import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class ClientWrapper {

    public static void handle(Supplier<Context> ctx, PacketBackupStatus packet) {
        
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                BackupToast.starting = packet.starting;
                BackupToast.started = packet.started;
                BackupToast.failed = packet.failed;
                BackupToast.finished = packet.finished;

                BackupToast.progress = packet.progress;
                BackupToast.max = packet.max;

                if (!BackupToast.exists) {
                    BackupToast.exists = true;
                    Minecraft.getInstance().getToasts().addToast(new BackupToast());
                }
            }
        });
    }
    
}
