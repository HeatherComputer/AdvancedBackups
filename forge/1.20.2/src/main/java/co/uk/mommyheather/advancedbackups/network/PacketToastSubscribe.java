package co.uk.mommyheather.advancedbackups.network;

import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class PacketToastSubscribe {
    
    public boolean enable;

    
    public PacketToastSubscribe(boolean enable) {
        this.enable = enable;
    }


    public PacketToastSubscribe(FriendlyByteBuf buf) {
        enable = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(enable);
    }

    public static boolean handle(PacketToastSubscribe message, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.getSender() == null) return;
            if (message.enable && !AdvancedBackups.players.contains(ctx.getSender().getStringUUID())) {
                AdvancedBackups.players.add(ctx.getSender().getStringUUID());
            }
            else if (!message.enable) {
                AdvancedBackups.players.remove(ctx.getSender().getStringUUID());
            }
        });
        ctx.setPacketHandled(true);
        return true;

    }
    
}
