package computer.heather.advancedbackups.client;


import computer.heather.advancedbackups.AdvancedBackups;
import computer.heather.advancedbackups.interfaces.IClientContactor;
import computer.heather.advancedbackups.network.NetworkHandler;
import computer.heather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ServerConfigurationManager;

@SuppressWarnings("unchecked")
public class ABClientContactor implements IClientContactor {

    @Override
    public void backupComplete(boolean all) {
        ServerConfigurationManager configurationManager = AdvancedBackups.server.getConfigurationManager();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, true, false, 0, 0);
        configurationManager.playerEntityList.forEach((player) -> {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (!AdvancedBackups.players.contains(playerMP.getGameProfile().getId().toString())) return;
                //if they can run the command, or are in singleplayer, or config says all players, they should receive info on active backups.
                if (playerMP.canCommandSenderUseCommand(3, "advancedbackups") || !AdvancedBackups.server.isDedicatedServer() || all) {
                    NetworkHandler.HANDLER.sendTo(packet, playerMP);
                }
            }
        });
    }

    @Override
    public void backupFailed(boolean all) {
        ServerConfigurationManager configurationManager = AdvancedBackups.server.getConfigurationManager();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, true, false, false, 0, 0);
        configurationManager.playerEntityList.forEach((player) -> {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (!AdvancedBackups.players.contains(playerMP.getGameProfile().getId().toString())) return;
                //if they can run the command, or are in singleplayer, or config says all players, they should receive info on active backups.
                if (playerMP.canCommandSenderUseCommand(3, "advancedbackups") || !AdvancedBackups.server.isDedicatedServer() || all) {
                    NetworkHandler.HANDLER.sendTo(packet, playerMP);
                }
            }
        });
    }

    @Override
    public void backupProgress(int progress, int max, boolean all) {
        ServerConfigurationManager configurationManager = AdvancedBackups.server.getConfigurationManager();
        PacketBackupStatus packet = new PacketBackupStatus(false, true, false, false, false, progress, max);
        configurationManager.playerEntityList.forEach((player) -> {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (!AdvancedBackups.players.contains(playerMP.getGameProfile().getId().toString())) return;
                //if they can run the command, or are in singleplayer, or config says all players, they should receive info on active backups.
                if (playerMP.canCommandSenderUseCommand(3, "advancedbackups") || !AdvancedBackups.server.isDedicatedServer() || all) {
                    NetworkHandler.HANDLER.sendTo(packet, playerMP);
                }
            }
        });
    }

    @Override
    public void backupStarting(boolean all) {
        ServerConfigurationManager configurationManager = AdvancedBackups.server.getConfigurationManager();
        PacketBackupStatus packet = new PacketBackupStatus(true, false, false, false, false, 0, 0);
        configurationManager.playerEntityList.forEach((player) -> {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (!AdvancedBackups.players.contains(playerMP.getGameProfile().getId().toString())) return;
                //if they can run the command, or are in singleplayer, or config says all players, they should receive info on active backups.
                if (playerMP.canCommandSenderUseCommand(3, "advancedbackups") || !AdvancedBackups.server.isDedicatedServer() || all) {
                    NetworkHandler.HANDLER.sendTo(packet, playerMP);
                }
            }
        });
    }

    @Override
    public void backupCancelled(boolean all) {
        ServerConfigurationManager configurationManager = AdvancedBackups.server.getConfigurationManager();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, false, true, 0, 0);
        configurationManager.playerEntityList.forEach((player) -> {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) player;
                if (!AdvancedBackups.players.contains(playerMP.getGameProfile().getId().toString())) return;
                //if they can run the command, or are in singleplayer, or config says all players, they should receive info on active backups.
                if (playerMP.canCommandSenderUseCommand(3, "advancedbackups") || !AdvancedBackups.server.isDedicatedServer() || all) {
                    NetworkHandler.HANDLER.sendTo(packet, playerMP);
                }
            }
        });
    }
    
}
