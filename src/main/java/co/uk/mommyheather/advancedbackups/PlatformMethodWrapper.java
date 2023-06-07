package co.uk.mommyheather.advancedbackups;

import java.nio.file.Path;
import java.util.function.Consumer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (ServerWorld level : server.getAllLevels()) {
            if (level != null && !level.noSave) {
                level.noSave = true;
            }
        }
        warningLogger.accept(savesDisabledMessage);
    }

    public static void enableSaving() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (ServerWorld level : server.getAllLevels()) {
            if (level != null && !level.noSave) {
                level.noSave = false;
            }
        }
        warningLogger.accept(savesEnabledMessage);
    }

    public static void saveOnce() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.saveAllChunks(true, true, true);
        warningLogger.accept(saveCompleteMessage);
    }
}