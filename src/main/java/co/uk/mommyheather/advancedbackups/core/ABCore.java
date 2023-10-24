package co.uk.mommyheather.advancedbackups.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.uk.mommyheather.advancedbackups.core.backups.gson.BackupManifest;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;
import co.uk.mommyheather.advancedbackups.interfaces.IClientContactor;

public class ABCore {
    
    public static String worldName;
    public static Path worldDir;
    public static Boolean activity = false;

    public static File modJar;
    //version specific stuff will locate the mod's jar
    
    public static Consumer<String> infoLogger;
    public static Consumer<String> warningLogger;
    public static Consumer<String> errorLogger;

    public static Runnable disableSaving;
    public static Runnable enableSaving;
    public static Consumer<Boolean> saveOnce;

    public static IClientContactor clientContactor;
    
    public static String backupPath;

    public static void disableSaving() {
        disableSaving.run();
    }

    public static void enableSaving() {
        enableSaving.run();
    }

    public static void saveOnce() {
        saveOnce.accept(ConfigManager.flush.get());;
    }

    public static void setActivity() {
        if (!activity) {
            GsonBuilder builder = new GsonBuilder(); 
            Gson gson = builder.setPrettyPrinting().create();
            //i should thread this at some point
            File file = new File(ABCore.backupPath);
            File backupManifest = new File(file, "manifest.json");
            if (backupManifest.exists()) {
                try {
                    BackupManifest manifest = gson.fromJson(new String(Files.readAllBytes(backupManifest.toPath())), BackupManifest.class);
                    
                    manifest.general.activity = true;
                    
                    FileWriter writer = new FileWriter(backupManifest);
                    writer.write(gson.toJson(manifest));
                    writer.flush();
                    writer.close();
    
                }
                catch (IOException e) {
                    ABCore.errorLogger.accept("Error writing player actiivty to backup manifest!!");
                    e.printStackTrace();
                }
            }
        }
        activity = true;
    }

    

    public static String serialiseBackupName(String in) {
        Date date = new Date();
        String pattern = "yyyy-MM-dd_HH-mm-ss";
        
        return in + "_" + new SimpleDateFormat(pattern).format(date);
    }
}
