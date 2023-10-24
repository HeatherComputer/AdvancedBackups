package co.uk.mommyheather.advancedbackups.network;

import co.uk.mommyheather.advancedbackups.client.ABClientRenderer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketBackupStatus implements IMessage{
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

    @Override
    public void fromBytes(ByteBuf buf) {
        starting = buf.readBoolean();
        started = buf.readBoolean();
        failed = buf.readBoolean();
        finished = buf.readBoolean();

        progress = buf.readInt();
        max = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(starting);
        buf.writeBoolean(started);
        buf.writeBoolean(failed);
        buf.writeBoolean(finished);

        buf.writeInt(progress);
        buf.writeInt(max);

    }


    public static class Handler implements IMessageHandler<PacketBackupStatus, IMessage> {

        @Override
        public IMessage onMessage(PacketBackupStatus message, MessageContext ctx) {

            ABClientRenderer.starting = message.starting;
            ABClientRenderer.started = message.started;
            ABClientRenderer.failed = message.failed;
            ABClientRenderer.finished = message.finished;

            ABClientRenderer.progress = message.progress;
            ABClientRenderer.max = message.max;

            
            return null;

        }
        
    }




}
