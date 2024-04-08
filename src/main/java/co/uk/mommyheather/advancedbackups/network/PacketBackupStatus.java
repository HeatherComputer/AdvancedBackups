package co.uk.mommyheather.advancedbackups.network;

import java.util.function.Supplier;

import co.uk.mommyheather.advancedbackups.client.BackupToast;
import co.uk.mommyheather.advancedbackups.client.ClientWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;

public class PacketBackupStatus {
    
    public boolean starting;
    public boolean started;

    public boolean failed;
    public boolean finished;
    public boolean cancelled;

    public int progress;
    public int max;

    
    public PacketBackupStatus(boolean starting, boolean started, boolean failed, boolean finished, boolean cancelled, int progress,
            int max) {
        this.starting = starting;
        this.started = started;
        this.failed = failed;
        this.finished = finished;
        this.cancelled = cancelled;
        this.progress = progress;
        this.max = max;
    }


    public PacketBackupStatus(FriendlyByteBuf buf) {
        starting = buf.readBoolean();
        started = buf.readBoolean();
        failed = buf.readBoolean();
        finished = buf.readBoolean();
        cancelled = buf.readBoolean();

        progress = buf.readInt();
        max = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(starting);
        buf.writeBoolean(started);
        buf.writeBoolean(failed);
        buf.writeBoolean(finished);
        buf.writeBoolean(cancelled);

        buf.writeInt(progress);
        buf.writeInt(max);
    }

    public boolean handle(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                ClientWrapper.handle(this);
            }
        });
        ctx.setPacketHandled(true);
        return true;

    }
    
}
