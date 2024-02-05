package co.uk.mommyheather.advancedbackups.client;

import java.util.List;

import co.uk.mommyheather.advancedbackups.AdvancedBackups;
import co.uk.mommyheather.advancedbackups.interfaces.IClientContactor;
import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import co.uk.mommyheather.advancedbackups.network.PacketBackupStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class ClientContactor implements IClientContactor {
    
    @Override
    public void backupComplete() {
        MinecraftServer server = AdvancedBackups.server;
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, true, false, 0, 0);
        for (ServerPlayerEntity player : players) {
            if (!AdvancedBackups.players.contains(player.getUuidAsString())) continue;
            if (!server.isDedicated() || player.hasPermissionLevel(3)) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupFailed() {
        MinecraftServer server = AdvancedBackups.server;
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, true, false, false, 0, 0);
        for (ServerPlayerEntity player : players) {
            if (!AdvancedBackups.players.contains(player.getUuidAsString())) continue;
            if (!server.isDedicated() || player.hasPermissionLevel(3)) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupProgress(int progress, int max) {
        MinecraftServer server = AdvancedBackups.server;
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        PacketBackupStatus packet = new PacketBackupStatus(false, true, false, false, false, progress, max);
        for (ServerPlayerEntity player : players) {
            if (!AdvancedBackups.players.contains(player.getUuidAsString())) continue;
            if (!server.isDedicated() || player.hasPermissionLevel(3)) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupStarting() {
        MinecraftServer server = AdvancedBackups.server;
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        PacketBackupStatus packet = new PacketBackupStatus(true, false, false, false, false, 0, 0);
        for (ServerPlayerEntity player : players) {
            if (!AdvancedBackups.players.contains(player.getUuidAsString())) continue;
            if (!server.isDedicated() || player.hasPermissionLevel(3)) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupCancelled() {        
        MinecraftServer server = AdvancedBackups.server;
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, false, true, 0, 0);
        for (ServerPlayerEntity player : players) {
            if (!AdvancedBackups.players.contains(player.getUuidAsString())) continue;
            if (!server.isDedicated() || player.hasPermissionLevel(3)) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }
}
