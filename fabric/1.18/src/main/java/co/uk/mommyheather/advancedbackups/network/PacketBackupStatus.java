package computer.heather.advancedbackups.network;

import computer.heather.advancedbackups.client.ClientWrapper;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

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

    public PacketBackupStatus() {

    }

    public void read(PacketByteBuf buf) {
        starting = buf.readBoolean();
        started = buf.readBoolean();
        failed = buf.readBoolean();
        finished = buf.readBoolean();
        cancelled = buf.readBoolean();

        progress = buf.readInt();
        max = buf.readInt();
    }

    public PacketByteBuf write(PacketByteBuf buf) {
        buf.writeBoolean(starting);
        buf.writeBoolean(started);
        buf.writeBoolean(failed);
        buf.writeBoolean(finished);
        buf.writeBoolean(cancelled);

        buf.writeInt(progress);
        buf.writeInt(max);

        return buf;

    }



    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {

        PacketBackupStatus message = new PacketBackupStatus();
        message.read(buf);

        ClientWrapper.handle(client, message);



    }
    
    }
