package co.uk.mommyheather.advancedbackups.network;

import co.uk.mommyheather.advancedbackups.client.BackupToast;
import co.uk.mommyheather.advancedbackups.client.ClientWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketBackupStatus(boolean starting, boolean started, boolean failed, boolean finished, boolean cancelled, int progress, int max) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketBackupStatus> ID = new CustomPacketPayload.Type<PacketBackupStatus>(new ResourceLocation("advancedbackups:backup_status"));

    public static final StreamCodec<FriendlyByteBuf, PacketBackupStatus> CODEC = StreamCodec.of((buf, packet) -> {
        buf.writeBoolean(packet.starting);
        buf.writeBoolean(packet.started);
        buf.writeBoolean(packet.failed);
        buf.writeBoolean(packet.finished);
        buf.writeBoolean(packet.cancelled);
        buf.writeInt(packet.progress);
        buf.writeInt(packet.max);
        }, 
        
        buf -> new PacketBackupStatus(
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readInt(),
            buf.readInt()
        ));


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static void handle(PacketBackupStatus packet, IPayloadContext context) {
        ClientWrapper.handle(packet, context);
    }

    
}
