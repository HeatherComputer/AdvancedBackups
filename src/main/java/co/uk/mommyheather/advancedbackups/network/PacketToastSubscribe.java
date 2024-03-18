package co.uk.mommyheather.advancedbackups.network;

import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class PacketToastSubscribe {
    
    public boolean enable;
    
    
    public PacketToastSubscribe(boolean enable) {
        this.enable = enable;
    }
    
    public PacketToastSubscribe() {
        
    }
    
    public void read(PacketByteBuf buf) {
        enable = buf.readBoolean();
    }
    
    public PacketByteBuf write(PacketByteBuf buf) {
        buf.writeBoolean(enable);
        
        return buf;
        
    }
    
    
    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        
        PacketToastSubscribe message = new PacketToastSubscribe();
        message.read(buf);
        
        
        server.execute(() -> {            
            if (message.enable && !AdvancedBackups.players.contains(player.getUuidAsString())) {
                AdvancedBackups.players.add(player.getUuidAsString());
            }
            else if (!message.enable) {
                AdvancedBackups.players.remove(player.getUuidAsString());
            }
            
        });
        
        
        
        
        
    }
    
    
    
}