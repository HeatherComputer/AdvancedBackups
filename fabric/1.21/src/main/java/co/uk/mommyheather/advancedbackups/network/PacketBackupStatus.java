package computer.heather.advancedbackups.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PacketBackupStatus(boolean starting, boolean started, boolean failed, boolean finished, boolean cancelled, int progress, int max) implements CustomPayload {

    public static final Id<PacketBackupStatus> ID = new CustomPayload.Id<PacketBackupStatus>(Identifier.of("advancedbackups:backup_status"));

    public static final PacketCodec<PacketByteBuf, PacketBackupStatus> CODEC = PacketCodec.of((packet, buf) -> {
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
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    
}
