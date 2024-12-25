package computer.heather.AdvancedBackups.network;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import computer.heather.advancedbackups.core.ABCore;
import computer.heather.AdvancedBackups.AdvancedBackups;

public class PacketListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        String uuid = player.getUniqueId().toString();
        Boolean subscribe = message[0] != 0;
        ABCore.infoLogger.accept("Player has chosen to " + (subscribe ? "accept" : "reject") + " progress updates.");

        if (!AdvancedBackups.players.contains(uuid) && subscribe) {
            AdvancedBackups.players.add(uuid);
        }
        else if (!subscribe) {
            AdvancedBackups.players.remove(uuid);
        }
    }
    
}
