package co.uk.mommyheather.advancedbackups.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.uk.mommyheather.advancedbackups.core.backups.BackupCheckEnum;
import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.backups.gson.DifferentialManifest;
import co.uk.mommyheather.advancedbackups.core.config.AVConfig;

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
        AVConfig.loadConfig();
        chat.accept("Done!");
    }

    public static void resetChainLength(Consumer<String> chat) {
        chat.accept("Resetting chain length... The next backup will be a complete backup.");
        boolean differential = AVConfig.config.getBackupType().equals("differential") ? true : false;
        File manifestFile = differential ? new File(AVConfig.config.getPath() + "/differential/manifest.json") : new File(AVConfig.config.getPath() + "/incremental/manifest.json");
        DifferentialManifest manifest;
        try {
            if (manifestFile.exists()) {
               manifest = gson.fromJson(new String(Files.readAllBytes(manifestFile.toPath())), DifferentialManifest.class);
               manifest.setChain(manifest.getChain() + 1);
               FileWriter writer = new FileWriter(manifestFile);
               writer.write(gson.toJson(manifest));
               writer.flush();
               writer.close();
               chat.accept("Done!");
               return;
            }
            else {
                chat.accept("No manifest file exists!");
                return;
            }
        } catch (Exception e) {
            chat.accept("Error resetting chain length. - check logs for more info.");
            e.printStackTrace();
        }
    }
}
