package computer.heather.advancedbackups.network;

import computer.heather.advancedbackups.core.config.ClientConfigManager;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketToastTest implements IMessage{



    public PacketToastTest(boolean enable) {
        
    }

    public PacketToastTest() {

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        
    }

    @Override
    public void toBytes(ByteBuf buf) {
        
    }


    public static class Handler implements IMessageHandler<PacketToastTest, IMessage> {

        @Override
        public IMessage onMessage(PacketToastTest message, MessageContext ctx) {
            
            ClientConfigManager.loadOrCreateConfig();
            NetworkHandler.HANDLER.sendToServer(new PacketToastSubscribe(ClientConfigManager.showProgress.get()));

            return null;

        }
        
    }




}