package co.uk.mommyheather.advancedbackups;

import java.nio.file.Path;
import java.util.function.Consumer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class PlatformMethodWrapper {

    public static String worldName = "";
    public static Path worldDir = null;
    public static Boolean activity = false;
    public static final Consumer<String> infoLogger = AdvancedBackups.infoLogger;
    public static final Consumer<String> warningLogger = AdvancedBackups.warningLogger;
    public static final Consumer<String> errorLogger = AdvancedBackups.errorLogger;
    
    public static final String savesDisabledMessage = "\n\n\n***************************************\nSAVING DISABLED - PREPARING FOR BACKUP!\n***************************************";
    public static final String savesEnabledMessage = "\n\n\n*********************************\nSAVING ENABLED - BACKUP COMPLETE!\n*********************************";
    public static final String saveCompleteMessage = "\n\n\n*************************************\nSAVE COMPLETE - PREPARING FOR BACKUP!\n*************************************";


    public static void disableSaving() {
        /*MinecraftServer server = AdvancedBackups.server;
        for (WorldServer level : server.worldServers) {
            if (level != null && !level.disableLevelSaving) {
                level.disableLevelSaving = true;
            }
        }
        warningLogger.accept(savesDisabledMessage);*/
    }

    public static void enableSaving() {
        /*MinecraftServer server = AdvancedBackups.server;
        for (WorldServer level : server.worlds) {
            if (level != null && !level.disableLevelSaving) {
                level.disableLevelSaving = false;
            }
        }
        warningLogger.accept(savesEnabledMessage);*/
    }

    public static void saveOnce() {
        /*MinecraftServer server = AdvancedBackups.server;
        server.saveAllWorlds(false);
        warningLogger.accept(saveCompleteMessage);*/
    }
}
