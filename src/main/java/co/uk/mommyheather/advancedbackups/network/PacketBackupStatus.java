package co.uk.mommyheather.advancedbackups.network;


import co.uk.mommyheather.advancedbackups.client.BackupToast;
import co.uk.mommyheather.advancedbackups.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;

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


    public PacketBackupStatus(FriendlyByteBuf buf) {
        starting = buf.readBoolean();
        started = buf.readBoolean();
        failed = buf.readBoolean();
        finished = buf.readBoolean();

        progress = buf.readInt();
        max = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(starting);
        buf.writeBoolean(started);
        buf.writeBoolean(failed);
        buf.writeBoolean(finished);

        buf.writeInt(progress);
        buf.writeInt(max);
    }

    public static boolean handle(PacketBackupStatus packet, NetworkEvent.Context ctx) {
        BackupToast.starting = packet.starting;
        BackupToast.started = packet.started;
        BackupToast.failed = packet.failed;
        BackupToast.finished = packet.finished;

        BackupToast.progress = packet.progress;
        BackupToast.max = packet.max;
        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                ClientWrapper.handle(packet);
            }
        });
        ctx.setPacketHandled(true);
        return true;

    }
    
}
