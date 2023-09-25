package co.uk.mommyheather.advancedbackups.core;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.backups.gson.BackupManifest;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;
import co.uk.mommyheather.advancedbackups.core.util.BackupCheckEnum;

public class CoreCommandSystem {
    private static GsonBuilder builder = new GsonBuilder(); 
    private static Gson gson;    
    static {
        builder.setPrettyPrinting();
        gson = builder.create();
    }
    

    //These methods are all called by relevant command classes in version specific code
    public static void checkBackups(Consumer<String> chat) {
        BackupCheckEnum check = BackupWrapper.checkBackups();
        chat.accept(check.getCheckMessage());
    }

    public static void startBackup(Consumer<String> chat) {
        BackupCheckEnum check = BackupWrapper.checkBackups();
        chat.accept(check.getCheckMessage());
        if (check.success()) {
            chat.accept("Starting backup...");
            BackupWrapper.makeSingleBackup(0);
        }
    }

    public static void forceBackup(Consumer<String> chat) {
        chat.accept("Forcing a backup...");
        BackupWrapper.makeSingleBackup(0);
    }

    public static void reloadConfig(Consumer<String> chat) {
        chat.accept("Reloading config...");
         ConfigManager.loadOrCreateConfig();
        chat.accept("Done!");
    }

    public static void resetChainLength(Consumer<String> chat) {
        chat.accept("Resetting chain length... The next backup will be a complete backup.");
        try {

            File file = new File(ConfigManager.path.get());
            File backupManifest = new File(file, "manifest.json");
            if (backupManifest.exists()) {
                    BackupManifest manifest = gson.fromJson(new String(Files.readAllBytes(backupManifest.toPath())), BackupManifest.class);
                    
                    manifest.incremental.chainLength += (int) ConfigManager.length.get();
                    manifest.differential.chainLength += (int) ConfigManager.length.get();
                    
                    
                    FileWriter writer = new FileWriter(backupManifest);
                    writer.write(gson.toJson(manifest));
                    writer.flush();
                    writer.close();
    
            }

            else {
                chat.accept("No manifest file exists.");
            }
        } catch (Exception e) {
            chat.accept("Error resetting chain length. - check logs for more info.");
            e.printStackTrace();
        }
    }
}
