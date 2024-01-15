package co.uk.mommyheather.advancedbackups.client;

import java.util.List;

import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import co.uk.mommyheather.advancedbackups.interfaces.IClientContactor;
import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.entity.player.EntityPlayerMP;

public class ClientContactor implements IClientContactor {

    @Override
    public void backupComplete() {
        List<EntityPlayerMP> players = AdvancedBackups.server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, true, false, 0, 0);
        for (EntityPlayerMP player : players) {
            if (!AdvancedBackups.server.isDedicatedServer() || player.canUseCommand(3, "backup")) {
                NetworkHandler.HANDLER.sendTo(packet, player);
            }
        }
    }

    @Override
    public void backupFailed() {
        List<EntityPlayerMP> players = AdvancedBackups.server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, true, false, false, 0, 0);
        for (EntityPlayerMP player : players) {
            if (!AdvancedBackups.server.isDedicatedServer() || player.canUseCommand(3, "backup")) {
                NetworkHandler.HANDLER.sendTo(packet, player);
            }
        }
    }

    @Override
    public void backupProgress(int progress, int max) {
        List<EntityPlayerMP> players = AdvancedBackups.server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, true, false, false, false, progress, max);
        for (EntityPlayerMP player : players) {
            if (!AdvancedBackups.server.isDedicatedServer() || player.canUseCommand(3, "backup")) {
                NetworkHandler.HANDLER.sendTo(packet, player);
            }
        }
    }

    @Override
    public void backupStarting() {
        List<EntityPlayerMP> players = AdvancedBackups.server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(true, false, false, false, false, 0, 0);
        for (EntityPlayerMP player : players) {
            if (!AdvancedBackups.server.isDedicatedServer() || player.canUseCommand(3, "backup")) {
                NetworkHandler.HANDLER.sendTo(packet, player);
            }
        }
    }

    @Override
    public void backupCancelled() {
        List<EntityPlayerMP> players = AdvancedBackups.server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, false, true, 0, 0);
        for (EntityPlayerMP player : players) {
            if (!AdvancedBackups.server.isDedicatedServer() || player.canUseCommand(3, "backup")) {
                NetworkHandler.HANDLER.sendTo(packet, player);
            }
        }
    }
    
}
