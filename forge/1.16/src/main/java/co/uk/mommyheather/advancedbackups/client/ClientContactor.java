package co.uk.mommyheather.advancedbackups.client;

import java.util.List;

import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import co.uk.mommyheather.advancedbackups.interfaces.IClientContactor;
import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ClientContactor implements IClientContactor {
    
    @Override
    public void backupComplete(boolean all) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, true, false, 0, 0);
        for (ServerPlayerEntity player : players) {
            if (!AdvancedBackups.players.contains(player.getStringUUID())) continue;
            if (!server.isDedicatedServer() || player.hasPermissions(3) || all) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupFailed(boolean all) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, true, false, false, 0, 0);
        for (ServerPlayerEntity player : players) {
            if (!AdvancedBackups.players.contains(player.getStringUUID())) continue;
            if (!server.isDedicatedServer() || player.hasPermissions(3) || all) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupProgress(int progress, int max, boolean all) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, true, false, false, false, progress, max);
        for (ServerPlayerEntity player : players) {
            if (!AdvancedBackups.players.contains(player.getStringUUID())) continue;
            if (!server.isDedicatedServer() || player.hasPermissions(3) || all) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupStarting(boolean all) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(true, false, false, false, false, 0, 0);
        for (ServerPlayerEntity player : players) {
            if (!AdvancedBackups.players.contains(player.getStringUUID())) continue;
            if (!server.isDedicatedServer() || player.hasPermissions(3) || all) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupCancelled(boolean all) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, false, true, 0, 0);
        for (ServerPlayerEntity player : players) {
            if (!AdvancedBackups.players.contains(player.getStringUUID())) continue;
            if (!server.isDedicatedServer() || player.hasPermissions(3) || all) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }
}
