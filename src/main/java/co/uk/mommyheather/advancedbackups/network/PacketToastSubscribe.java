package co.uk.mommyheather.advancedbackups.network;


import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class PacketToastSubscribe implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation("advancedbackups", "toast_subscribe");

    @Override
    public ResourceLocation id() {
        return ID;
    }
    
    private boolean enable;
    
    public PacketToastSubscribe(boolean enable) {
        this.enable = enable;
    }


    public PacketToastSubscribe(FriendlyByteBuf buf) {
        enable = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(enable);
    }

    public boolean handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            if (!ctx.player().isPresent()) return;
            if (enable && !AdvancedBackups.players.contains(ctx.player().get().getStringUUID())) {
                AdvancedBackups.players.add(ctx.player().get().getStringUUID());
            }
            else if (!enable) {
                AdvancedBackups.players.remove(ctx.player().get().getStringUUID());
            }
        });

        return true;

    }
    
}
