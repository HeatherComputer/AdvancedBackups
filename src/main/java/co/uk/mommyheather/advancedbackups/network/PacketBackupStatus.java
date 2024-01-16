package co.uk.mommyheather.advancedbackups.network;

import java.util.function.Supplier;

import co.uk.mommyheather.advancedbackups.client.ClientWrapper;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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


    public PacketBackupStatus(PacketBuffer buf) {
        starting = buf.readBoolean();
        started = buf.readBoolean();
        failed = buf.readBoolean();
        finished = buf.readBoolean();
        cancelled = buf.readBoolean();

        progress = buf.readInt();
        max = buf.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(starting);
        buf.writeBoolean(started);
        buf.writeBoolean(failed);
        buf.writeBoolean(finished);
        buf.writeBoolean(cancelled);

        buf.writeInt(progress);
        buf.writeInt(max);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ClientWrapper.handle(ctx, this);
        ctx.get().setPacketHandled(true);
        return true;

    }
    
}
