package computer.heather.advancedbackups.network;

import computer.heather.advancedbackups.core.CoreCommandSystem;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

//I hate this.
public class PacketClientReload implements IMessage{
    
    
    public PacketClientReload(boolean enable) {
        
    }

    public PacketClientReload () { 

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        
    }

    @Override
    public void toBytes(ByteBuf buf) {
        
    }


    public static class Handler implements IMessageHandler<PacketClientReload, IMessage> {

        @Override
        public IMessage onMessage(PacketClientReload message, MessageContext ctx) {

            CoreCommandSystem.reloadClientConfig(Minecraft.getMinecraft().thePlayer::sendChatMessage);
            return null;

        }
        
    }
    
}
