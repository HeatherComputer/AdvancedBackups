package computer.heather.advancedbackups.network;

import computer.heather.advancedbackups.AdvancedBackups;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketToastSubscribe implements IMessage{

    public boolean enable;


    public PacketToastSubscribe(boolean enable) {
        this.enable = enable;
    }

    public PacketToastSubscribe() {

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        enable = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(enable);
    }


    public static class Handler implements IMessageHandler<PacketToastSubscribe, IMessage> {

        @Override
        public IMessage onMessage(PacketToastSubscribe message, MessageContext ctx) {

            String uuid = ctx.getServerHandler().player.getGameProfile().getId().toString();

            
            if (message.enable && !AdvancedBackups.players.contains(uuid)) {
                AdvancedBackups.players.add(uuid);
            }
            else if (!message.enable) {
                AdvancedBackups.players.remove(uuid);
            }

            
            return null;

        }
        
    }




}