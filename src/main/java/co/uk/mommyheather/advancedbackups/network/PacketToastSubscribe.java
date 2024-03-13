package co.uk.mommyheather.advancedbackups.network;

import java.util.function.Supplier;

import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketToastSubscribe {
    
    private boolean enable;
    
    public PacketToastSubscribe(boolean enable) {
        this.enable = enable;
    }


    public PacketToastSubscribe(FriendlyByteBuf buf) {
        enable = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(enable);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() == null) return;
            if (enable && !AdvancedBackups.players.contains(ctx.get().getSender().getStringUUID())) {
                AdvancedBackups.players.add(ctx.get().getSender().getStringUUID());
            }
            else if (!enable) {
                AdvancedBackups.players.remove(ctx.get().getSender().getStringUUID());
            }
        });

        return true;

    }
    
}
