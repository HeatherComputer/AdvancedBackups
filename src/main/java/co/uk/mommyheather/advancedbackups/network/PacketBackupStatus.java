package co.uk.mommyheather.advancedbackups.network;

import co.uk.mommyheather.advancedbackups.client.BackupToast;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class PacketBackupStatus {
    
    public boolean starting;
    public boolean started;
    public boolean failed;
    public boolean finished;

    public int progress;
    public int max;


    public PacketBackupStatus(boolean starting, boolean started, boolean failed, boolean finished, int progress,
            int max) {
        this.starting = starting;
        this.started = started;
        this.failed = failed;
        this.finished = finished;
        this.progress = progress;
        this.max = max;
    }

    public PacketBackupStatus() {

    }

    public void read(PacketByteBuf buf) {
        starting = buf.readBoolean();
        started = buf.readBoolean();
        failed = buf.readBoolean();
        finished = buf.readBoolean();

        progress = buf.readInt();
        max = buf.readInt();
    }

    public PacketByteBuf write(PacketByteBuf buf) {
        buf.writeBoolean(starting);
        buf.writeBoolean(started);
        buf.writeBoolean(failed);
        buf.writeBoolean(finished);

        buf.writeInt(progress);
        buf.writeInt(max);

        return buf;

    }

}
