package computer.heather.AdvancedBackups.network;

import co.uk.mommyheather.advancedbackups.interfaces.IClientContactor;
import computer.heather.AdvancedBackups.AdvancedBackups;

public class ClientContactor implements IClientContactor {

    private static byte[] toBytes(boolean starting, boolean started, boolean failed, boolean finished, boolean cancelled, int progress, int max) {
        byte[] ret = new byte[13];

        ret[0] = (byte) (starting ? 1 : 0);
        ret[1] = (byte) (started ? 1 : 0);
        ret[2] = (byte) (failed ? 1 : 0);
        ret[3] = (byte) (finished ? 1 : 0);
        ret[4] = (byte) (cancelled ? 1 : 0);

        ret[5] = (byte) ((progress & 0xFF000000) >> 24);
        ret[6] = (byte) ((progress & 0x00FF0000) >> 16);
        ret[7] = (byte) ((progress & 0x0000FF00) >> 8);
        ret[8] = (byte) ((progress & 0x000000FF) >> 0);

        ret[9] = (byte) ((max & 0xFF000000) >> 24);
        ret[10] = (byte) ((max & 0x00FF0000) >> 16);
        ret[11] = (byte) ((max & 0x0000FF00) >> 8);
        ret[12] = (byte) ((max & 0x000000FF) >> 0);

        return ret;
    }

    //starting, started, failed, finished, cancelled, progress, max
    @Override
    public void backupComplete(boolean all) {
        byte[] bytes = toBytes(false, false, false, true, false, 0, 0);
        
        AdvancedBackups.server.getOnlinePlayers().forEach((player) -> {
            if (!AdvancedBackups.players.contains(player.getUniqueId().toString())) return;
            if (player.isOp() || all) player.sendPluginMessage(AdvancedBackups.getPlugin(AdvancedBackups.class), "advancedbackups:backup_status", bytes);
        });
    }
    
    @Override
    public void backupFailed(boolean all) {
        byte[] bytes = toBytes(false, false, true, false, false, 0, 0);
        
        AdvancedBackups.server.getOnlinePlayers().forEach((player) -> {
            if (!AdvancedBackups.players.contains(player.getUniqueId().toString())) return;
            if (player.isOp() || all) player.sendPluginMessage(AdvancedBackups.getPlugin(AdvancedBackups.class), "advancedbackups:backup_status", bytes);
        });
    }
    
    @Override
    public void backupProgress(int progress, int max, boolean all) {
        byte[] bytes = toBytes(false, true, false, false, false, progress, max);
        
        AdvancedBackups.server.getOnlinePlayers().forEach((player) -> {
            if (!AdvancedBackups.players.contains(player.getUniqueId().toString())) return;
            if (player.isOp() || all) player.sendPluginMessage(AdvancedBackups.getPlugin(AdvancedBackups.class), "advancedbackups:backup_status", bytes);
        });
    }
    
    @Override
    public void backupStarting(boolean all) {
        byte[] bytes = toBytes(true, false, false, false, false, 0, 0);
        
        AdvancedBackups.server.getOnlinePlayers().forEach((player) -> {
            if (!AdvancedBackups.players.contains(player.getUniqueId().toString())) return;
            if (player.isOp() || all) player.sendPluginMessage(AdvancedBackups.getPlugin(AdvancedBackups.class), "advancedbackups:backup_status", bytes);
        });
    }
    
    @Override
    public void backupCancelled(boolean all) {
        byte[] bytes = toBytes(false, false, false, false, true, 0, 0);
        
        AdvancedBackups.server.getOnlinePlayers().forEach((player) -> {
            if (!AdvancedBackups.players.contains(player.getUniqueId().toString())) return;
            if (player.isOp() || all) player.sendPluginMessage(AdvancedBackups.getPlugin(AdvancedBackups.class), "advancedbackups:backup_status", bytes);
        });
    }
    
}
