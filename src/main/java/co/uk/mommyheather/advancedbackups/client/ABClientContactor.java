package co.uk.mommyheather.advancedbackups.client;


import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import co.uk.mommyheather.advancedbackups.interfaces.IClientContactor;
import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ServerConfigurationManager;

@SuppressWarnings("unchecked")
public class ABClientContactor implements IClientContactor {

    @Override
    public void backupComplete() {
        ServerConfigurationManager configurationManager = AdvancedBackups.server.getConfigurationManager();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, true, false, 0, 0);
        configurationManager.playerEntityList.forEach((player) -> {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (!AdvancedBackups.players.contains(playerMP.getGameProfile().getId().toString())) return;
                //if they can run the command, or are in singleplayer, they should receive info on active backups.
                if (playerMP.canCommandSenderUseCommand(3, "advancedbackups") || !AdvancedBackups.server.isDedicatedServer()) {
                    NetworkHandler.HANDLER.sendTo(packet, playerMP);
                }
            }
        });
    }

    @Override
    public void backupFailed() {
        ServerConfigurationManager configurationManager = AdvancedBackups.server.getConfigurationManager();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, true, false, false, 0, 0);
        configurationManager.playerEntityList.forEach((player) -> {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (!AdvancedBackups.players.contains(playerMP.getGameProfile().getId().toString())) return;
                if (playerMP.canCommandSenderUseCommand(3, "advancedbackups")) { //if they can run the command, they should receive info on active backups
                    NetworkHandler.HANDLER.sendTo(packet, playerMP);
                }
            }
        });
    }

    @Override
    public void backupProgress(int progress, int max) {
        ServerConfigurationManager configurationManager = AdvancedBackups.server.getConfigurationManager();
        PacketBackupStatus packet = new PacketBackupStatus(false, true, false, false, false, progress, max);
        configurationManager.playerEntityList.forEach((player) -> {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (!AdvancedBackups.players.contains(playerMP.getGameProfile().getId().toString())) return;
                if (playerMP.canCommandSenderUseCommand(3, "advancedbackups")) { //if they can run the command, they should receive info on active backups
                    NetworkHandler.HANDLER.sendTo(packet, playerMP);
                }
            }
        });
    }

    @Override
    public void backupStarting() {
        ServerConfigurationManager configurationManager = AdvancedBackups.server.getConfigurationManager();
        PacketBackupStatus packet = new PacketBackupStatus(true, false, false, false, false, 0, 0);
        configurationManager.playerEntityList.forEach((player) -> {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (!AdvancedBackups.players.contains(playerMP.getGameProfile().getId().toString())) return;
                if (playerMP.canCommandSenderUseCommand(3, "advancedbackups")) { //if they can run the command, they should receive info on active backups
                    NetworkHandler.HANDLER.sendTo(packet, playerMP);
                }
            }
        });
    }

    @Override
    public void backupCancelled() {
        ServerConfigurationManager configurationManager = AdvancedBackups.server.getConfigurationManager();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, false, true, 0, 0);
        configurationManager.playerEntityList.forEach((player) -> {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (!AdvancedBackups.players.contains(playerMP.getGameProfile().getId().toString())) return;
                if (playerMP.canCommandSenderUseCommand(3, "advancedbackups")) { //if they can run the command, they should receive info on active backups
                    NetworkHandler.HANDLER.sendTo(packet, playerMP);
                }
            }
        });
    }
    
}
