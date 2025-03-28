package computer.heather.advancedbackups.network;

import java.io.IOException;

import computer.heather.advancedbackups.core.ABCore;
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
            
            try {
                ClientConfigManager.loadOrCreateConfig();
            } catch (IOException e) {
                ABCore.errorLogger.accept("Unable to load client config! Default will be used...");
                ABCore.logStackTrace(e);
            }
            NetworkHandler.HANDLER.sendToServer(new PacketToastSubscribe(ClientConfigManager.showProgress.get()));

            return null;

        }
        
    }




}