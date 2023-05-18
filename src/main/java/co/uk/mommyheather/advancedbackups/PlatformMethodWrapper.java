package co.uk.mommyheather.advancedbackups;

import java.nio.file.Path;
import java.util.function.Consumer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class PlatformMethodWrapper {

    public static String worldName = "";
    public static Path worldDir = null;
    public static Boolean activity = false;
    public static final Consumer<String> infoLogger = AdvancedBackups.infoLogger;
    public static final Consumer<String> warningLogger = AdvancedBackups.warningLogger;
    public static final Consumer<String> errorLogger = AdvancedBackups.errorLogger;
    
    public static final String savesDisabledMessage = """


***************************************
SAVING DISABLED - PREPARING FOR BACKUP!
***************************************
""";
    public static final String savesEnabledMessage = """


*********************************
SAVING ENABLED - BACKUP COMPLETE!
*********************************
""";
    public static final String saveCompleteMessage = """


*************************************
SAVE COMPLETE - PREPARING FOR BACKUP!
*************************************
""";


    public static void disableSaving() {
        MinecraftServer server = AdvancedBackups.server;
        for (ServerWorld level : server.getWorlds()) {
            if (level != null && !level.savingDisabled) {
                level.savingDisabled = true;
            }
        }
        warningLogger.accept(savesDisabledMessage);
    }

    public static void enableSaving() {
        MinecraftServer server = AdvancedBackups.server;
        for (ServerWorld level : server.getWorlds()) {
            if (level != null && !level.savingDisabled) {
                level.savingDisabled = false;
            }
        }
        warningLogger.accept(savesEnabledMessage);
    }

    public static void saveOnce() {
        MinecraftServer server = AdvancedBackups.server;
        server.saveAll(true, true, true);
        warningLogger.accept(saveCompleteMessage);
    }
}
