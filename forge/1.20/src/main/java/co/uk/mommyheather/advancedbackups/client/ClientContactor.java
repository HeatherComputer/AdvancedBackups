package computer.heather.advancedbackups.client;

import java.util.List;

import computer.heather.advancedbackups.AdvancedBackups;
import computer.heather.advancedbackups.interfaces.IClientContactor;
import computer.heather.advancedbackups.network.NetworkHandler;
import computer.heather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ClientContactor implements IClientContactor {
    
    @Override
    public void backupComplete(boolean all) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, true, false, 0, 0);
        for (ServerPlayer player : players) {
            if (!AdvancedBackups.players.contains(player.getStringUUID())) continue;
            if (!server.isDedicatedServer() || player.hasPermissions(3) || all) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupFailed(boolean all) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, true, false, false, 0, 0);
        for (ServerPlayer player : players) {
            if (!AdvancedBackups.players.contains(player.getStringUUID())) continue;
            if (!server.isDedicatedServer() || player.hasPermissions(3) || all) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupProgress(int progress, int max, boolean all) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, true, false, false, false, progress, max);
        for (ServerPlayer player : players) {
            if (!AdvancedBackups.players.contains(player.getStringUUID())) continue;
            if (!server.isDedicatedServer() || player.hasPermissions(3) || all) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupStarting(boolean all) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(true, false, false, false, false, 0, 0);
        for (ServerPlayer player : players) {
            if (!AdvancedBackups.players.contains(player.getStringUUID())) continue;
            if (!server.isDedicatedServer() || player.hasPermissions(3) || all) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupCancelled(boolean all) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, false, true, 0, 0);
        for (ServerPlayer player : players) {
            if (!AdvancedBackups.players.contains(player.getStringUUID())) continue;
            if (!server.isDedicatedServer() || player.hasPermissions(3) || all) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }
}
