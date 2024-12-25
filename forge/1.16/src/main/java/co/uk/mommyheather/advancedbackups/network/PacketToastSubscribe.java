package computer.heather.advancedbackups.network;

import java.util.function.Supplier;

import computer.heather.advancedbackups.AdvancedBackups;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketToastSubscribe {
    
    private boolean enable;
    
    public PacketToastSubscribe(boolean enable) {
        this.enable = enable;
    }


    public PacketToastSubscribe(PacketBuffer buf) {
        enable = buf.readBoolean();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBoolean(enable);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
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
