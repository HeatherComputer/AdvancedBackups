package co.uk.mommyheather.advancedbackups.network;

import co.uk.mommyheather.advancedbackups.client.ClientWrapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class PacketBackupStatus implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation("advancedbackups", "backup_status");

    @Override
    public ResourceLocation id() {
        return ID;
    }
    
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

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(starting);
        buf.writeBoolean(started);
        buf.writeBoolean(failed);
        buf.writeBoolean(finished);
        buf.writeBoolean(cancelled);

        buf.writeInt(progress);
        buf.writeInt(max);
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            if (ctx.flow().getReceptionSide() == LogicalSide.CLIENT) {
                ClientWrapper.handle(this);
            }
        });

    }
    
}
