package computer.heather.advancedbackups;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import computer.heather.advancedbackups.client.ClientContactor;
import computer.heather.advancedbackups.core.ABCore;
import computer.heather.advancedbackups.core.backups.BackupTimer;
import computer.heather.advancedbackups.core.backups.BackupWrapper;
import computer.heather.advancedbackups.core.config.ConfigManager;
import computer.heather.advancedbackups.network.PacketBackupStatus;
import computer.heather.advancedbackups.network.PacketToastSubscribe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;

public class AdvancedBackups implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.

    public static final Logger LOGGER = LoggerFactory.getLogger("advanced-backups");

    public static final Consumer<String> infoLogger = LOGGER::info;
    public static final Consumer<String> warningLogger = LOGGER::warn;
    public static final Consumer<String> errorLogger = LOGGER::error;
    
    public static final ArrayList<String> players = new ArrayList<>();

    public static MinecraftServer server;

    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            AdvancedBackups.server = server;
            ABCore.worldName = server.getSaveProperties().getLevelName();
            ABCore.worldDir = server.getSavePath(WorldSavePath.ROOT);

            ABCore.disableSaving = AdvancedBackups::disableSaving;
            ABCore.enableSaving = AdvancedBackups::enableSaving;
            ABCore.saveOnce = AdvancedBackups::saveOnce;

            ABCore.infoLogger = infoLogger;
            ABCore.warningLogger = warningLogger;
            ABCore.errorLogger = errorLogger;

            ABCore.resetActivity = AdvancedBackups::resetActivity;

            ABCore.clientContactor = new ClientContactor();
            ABCore.modJar = new File(FabricLoaderImpl.INSTANCE.getModContainer("advancedbackups").get().getOrigin().getPaths().get(0).toAbsolutePath().toString());
            
            
            ConfigManager.loadOrCreateConfig();
            LOGGER.info("Config loaded!!");
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            BackupWrapper.checkStartupBackups();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            BackupWrapper.checkShutdownBackups();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ABCore.setActivity(true);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AdvancedBackupsCommand.register(dispatcher);
        });

        
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            BackupTimer.check();
        });
        
        PayloadTypeRegistry.playS2C().register(PacketBackupStatus.ID, PacketBackupStatus.CODEC);
        PayloadTypeRegistry.playC2S().register(PacketToastSubscribe.ID, PacketToastSubscribe.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PacketToastSubscribe.ID, PacketToastSubscribe::handle);
        
            
    }




    public static void disableSaving() {
        MinecraftServer server = AdvancedBackups.server;
        for (ServerWorld level : server.getWorlds()) {
            if (level != null && !level.savingDisabled) {
                level.savingDisabled = true;
            }
        }
    }

    public static void enableSaving() {
        MinecraftServer server = AdvancedBackups.server;
        for (ServerWorld level : server.getWorlds()) {
            if (level != null && level.savingDisabled) {
                level.savingDisabled = false;
            }
        }
    }

    public static void saveOnce(boolean flush) {
        MinecraftServer server = AdvancedBackups.server;
        server.saveAll(true, flush, true);
    }

    public static void resetActivity() {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        ABCore.setActivity(!players.isEmpty());
    }
}
