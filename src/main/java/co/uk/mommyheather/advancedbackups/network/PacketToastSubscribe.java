package co.uk.mommyheather.advancedbackups.network;

import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketToastSubscribe(boolean enable) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<PacketToastSubscribe> ID = new CustomPacketPayload.Type<>(ResourceLocation.parse("advancedbackups:toast_subscribe"));
    
    public PacketToastSubscribe(boolean enable) {
        this.enable = enable;
    }

    public static final StreamCodec<FriendlyByteBuf, PacketToastSubscribe> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, PacketToastSubscribe::enable, PacketToastSubscribe::new);


    public static void handle(PacketToastSubscribe message, IPayloadContext context) {

        Player player = context.player();

        if (message.enable() && !AdvancedBackups.players.contains(player.getStringUUID())) {
            AdvancedBackups.players.add(player.getStringUUID());
        }
        else if (!message.enable()) {
            AdvancedBackups.players.remove(player.getStringUUID());
        }
        
    }
    


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
    
    
    
}