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
    public void backupComplete() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, false, true, 0, 0);
        for (ServerPlayerEntity player : players) {
            if (player.hasPermissions(3)) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupFailed() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, false, true, false, 0, 0);
        for (ServerPlayerEntity player : players) {
            if (player.hasPermissions(3)) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupProgress(int progress, int max) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(false, true, false, false, progress, max);
        for (ServerPlayerEntity player : players) {
            if (player.hasPermissions(3)) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }

    @Override
    public void backupStarting() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
        PacketBackupStatus packet = new PacketBackupStatus(true, false, false, false, 0, 0);
        for (ServerPlayerEntity player : players) {
            if (player.hasPermissions(3)) {
                NetworkHandler.sendToClient(player, packet);
            }
        }
    }
}