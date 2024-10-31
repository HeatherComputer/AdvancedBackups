package co.uk.mommyheather.advancedbackups.core;

import co.uk.mommyheather.advancedbackups.core.backups.gson.BackupManifest;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;
import co.uk.mommyheather.advancedbackups.interfaces.IClientContactor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

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

    public static Runnable resetActivity;

    public static IClientContactor clientContactor;

    public static String backupPath;

    public static void disableSaving() {
        if (ConfigManager.toggleSave.get()) disableSaving.run();
    }

    public static void enableSaving() {
        //Technically there's an edgecase here where someone could let saving be disabled, then adjust + reload config to stop it being enabled again...
        //but I don't know a good way to counter this right now and there's a failsafe on server boot regardless.
        if (ConfigManager.toggleSave.get()) enableSaving.run();
    }

    public static void resetActivity() {
        resetActivity.run();
    }

    public static void saveOnce() {
        saveOnce.accept(ConfigManager.flush.get());
    }

    public static void setActivity(boolean in) {
        if (in != activity) {
            activity = in;
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().create();
            //i should thread this at some point
            File file = new File(ABCore.backupPath);
            File backupManifest = new File(file, "manifest.json");
            try {
                if (backupManifest.exists()) {
                    try {
                        BackupManifest manifest = gson.fromJson(new String(Files.readAllBytes(backupManifest.toPath())), BackupManifest.class);
                        manifest.general.activity = activity;

                        FileWriter writer = new FileWriter(backupManifest);
                        writer.write(gson.toJson(manifest));
                        writer.flush();
                        writer.close();
                    } catch (JsonParseException e) {
                        ABCore.errorLogger.accept("Malformed backup manifest! Overwriting, meaning next backup has to be a full backup...");
                        ABCore.logStackTrace(e);

                        BackupManifest manifest = BackupManifest.defaults();
                        manifest.general.activity = activity;

                        FileWriter writer = new FileWriter(backupManifest);
                        writer.write(gson.toJson(manifest));
                        writer.flush();
                        writer.close();
                    }
                } else {
                    BackupManifest manifest = BackupManifest.defaults();
                    manifest.general.activity = activity;

                    FileWriter writer = new FileWriter(backupManifest);
                    writer.write(gson.toJson(manifest));
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                ABCore.errorLogger.accept("Error writing player activty to backup manifest!!");
                ABCore.logStackTrace(e);
            }
        }
    }


    public static String serialiseBackupName(String in) {
        Date date = new Date();
        String pattern = "yyyy-MM-dd_HH-mm-ss";
        return in + "_" + new SimpleDateFormat(pattern).format(date);
    }

    public static void logStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        ABCore.errorLogger.accept(sw.toString());
        pw.close();
    }
}
