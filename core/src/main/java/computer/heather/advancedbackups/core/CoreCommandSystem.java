package computer.heather.advancedbackups.core;

import computer.heather.advancedbackups.core.backups.BackupWrapper;
import computer.heather.advancedbackups.core.backups.ThreadedBackup;
import computer.heather.advancedbackups.core.backups.gson.BackupManifest;
import computer.heather.advancedbackups.core.config.ClientConfigManager;
import computer.heather.advancedbackups.core.config.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.function.Consumer;

public class CoreCommandSystem {
    private static GsonBuilder builder = new GsonBuilder();
    private static Gson gson;

    static {
        builder.setPrettyPrinting();
        gson = builder.create();
    }


    //These methods are all called by relevant command classes in version specific code
    public static void startBackup(Consumer<String> chat) {
        chat.accept("Starting backup...");
        BackupWrapper.checkBackups(); //makes sure the backups folder is present etc
        if (ThreadedBackup.running) {
            chat.accept("Cannot start a backup whilst a backup is already running!");
            return;
        }
        BackupWrapper.makeSingleBackup(0, chat, false);
    }

    public static void reloadConfig(Consumer<String> chat) {
        chat.accept("Reloading config...");
        ConfigManager.loadOrCreateConfig();
        chat.accept("Done!");
    }

    public static void reloadClientConfig(Consumer<String> chat) {
        chat.accept("Reloading client config...");
        ClientConfigManager.loadOrCreateConfig();
        chat.accept("Done!");
    }

    public static void snapshot(Consumer<String> chat, String snapshotName) {
        BackupWrapper.checkBackups(); //makes sure the backups folder is present etc
        if (ThreadedBackup.running) {
            chat.accept("Cannot start a snapshot whilst a backup is already running!");
            return;
        }
        BackupWrapper.makeSnapshot(chat, snapshotName);
    }

    public static void resetChainLength(Consumer<String> chat) {
        chat.accept("Resetting chain length... The next backup will be a complete backup.");
        try {

            File file = new File(ABCore.backupPath);
            File backupManifest = new File(file, "manifest.json");
            if (backupManifest.exists()) {
                try {
                    BackupManifest manifest = gson.fromJson(new String(Files.readAllBytes(backupManifest.toPath())), BackupManifest.class);

                    manifest.incremental.chainLength += (int) ConfigManager.length.get();
                    manifest.differential.chainLength += (int) ConfigManager.length.get();


                    FileWriter writer = new FileWriter(backupManifest);
                    writer.write(gson.toJson(manifest));
                    writer.flush();
                    writer.close();

                } catch (JsonParseException | NullPointerException e) {
                    chat.accept("Malformed backup manifest! Will be completely replaced, with no side effects...");
                    chat.accept("Check logs for more info.");
                    ABCore.logStackTrace(e);

                    BackupManifest manifest = BackupManifest.defaults();

                    //don't actually need to set them here

                    FileWriter writer = new FileWriter(backupManifest);
                    writer.write(gson.toJson(manifest));
                    writer.flush();
                    writer.close();

                }
            } else {
                chat.accept("No manifest file exists.");
            }
        } catch (Exception e) {
            chat.accept("Error resetting chain length. - check logs for more info.");
            ABCore.logStackTrace(e);
        }
    }

    public static void cancelBackup(Consumer<String> chat) {
        chat.accept("Cancelling ongoing backup if one exists...");
        chat.accept("This may fail or take a while depending on backup stage.");
        Thread.getAllStackTraces().keySet().forEach(thread -> {
            if (thread instanceof ThreadedBackup) {
                thread.interrupt();
            }
        });

    }
}