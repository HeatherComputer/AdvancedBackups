package computer.heather.advancedbackups.network;

import java.util.function.Supplier;

import computer.heather.advancedbackups.AdvancedBackups;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

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

    public boolean handle(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.getSender() == null) return;
            if (enable && !AdvancedBackups.players.contains(ctx.getSender().getStringUUID())) {
                AdvancedBackups.players.add(ctx.getSender().getStringUUID());
            }
            else if (!enable) {
                AdvancedBackups.players.remove(ctx.getSender().getStringUUID());
            }
        });

        return true;

    }
    
}
