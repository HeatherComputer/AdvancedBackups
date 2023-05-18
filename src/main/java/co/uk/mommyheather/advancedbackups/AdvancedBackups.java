package co.uk.mommyheather.advancedbackups;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.config.AVConfig;

public class AdvancedBackups implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.

    public static final Logger LOGGER = LoggerFactory.getLogger("advanced-backups");

    public static final Consumer<String> infoLogger = LOGGER::info;
    public static final Consumer<String> warningLogger = LOGGER::warn;
    public static final Consumer<String> errorLogger = LOGGER::error;

    public static MinecraftServer server;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            AdvancedBackups.server = server;
            AVConfig.loadOrCreateConfig();
            LOGGER.info("Config loaded!!");
            PlatformMethodWrapper.worldName = server.getSaveProperties().getLevelName();
            PlatformMethodWrapper.worldDir = server.getSavePath(WorldSavePath.ROOT);
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            BackupWrapper.checkStartupBackups();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            BackupWrapper.checkShutdownBackups();
        });
    }
}