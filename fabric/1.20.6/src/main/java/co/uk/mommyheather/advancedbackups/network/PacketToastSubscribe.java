package co.uk.mommyheather.advancedbackups.network;

import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public record PacketToastSubscribe(boolean enable) implements CustomPayload {
    
    public static final Id<PacketToastSubscribe> ID = CustomPayload.id("advancedbackups:toast_subscribe");
    
    public PacketToastSubscribe(boolean enable) {
        this.enable = enable;
    }

    public static final PacketCodec<PacketByteBuf, PacketToastSubscribe> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, PacketToastSubscribe::enable, PacketToastSubscribe::new);


    public static void handle(PacketToastSubscribe message, ServerPlayNetworking.Context context) {

        ServerPlayerEntity player = context.player();

        if (message.enable() && !AdvancedBackups.players.contains(player.getUuidAsString())) {
            AdvancedBackups.players.add(player.getUuidAsString());
        }
        else if (!message.enable()) {
            AdvancedBackups.players.remove(player.getUuidAsString());
        }
        
    }
    


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    
    
}