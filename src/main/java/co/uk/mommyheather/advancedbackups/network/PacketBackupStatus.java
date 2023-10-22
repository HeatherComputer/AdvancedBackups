package co.uk.mommyheather.advancedbackups.network;

import java.util.function.Supplier;

import co.uk.mommyheather.advancedbackups.client.BackupToast;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

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


    public PacketBackupStatus(PacketBuffer buf) {
        starting = buf.readBoolean();
        started = buf.readBoolean();
        failed = buf.readBoolean();
        finished = buf.readBoolean();

        progress = buf.readInt();
        max = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(starting);
        buf.writeBoolean(started);
        buf.writeBoolean(failed);
        buf.writeBoolean(finished);

        buf.writeInt(progress);
        buf.writeInt(max);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                BackupToast.starting = starting;
                BackupToast.started = started;
                BackupToast.failed = failed;
                BackupToast.finished = finished;

                BackupToast.progress = progress;
                BackupToast.max = max;

                if (!BackupToast.exists) {
                    BackupToast.exists = true;
                    Minecraft.getInstance().getToasts().addToast(new BackupToast());
                }
            }
        });
        ctx.get().setPacketHandled(true);
        return true;

    }
    
}
