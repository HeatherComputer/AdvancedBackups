package co.uk.mommyheather.advancedbackups.core.backups;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.gson.BackupManifest;
import co.uk.mommyheather.advancedbackups.core.backups.gson.DifferentialManifest;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;

public class BackupWrapper {

    
    private static GsonBuilder builder = new GsonBuilder(); 
    private static Gson gson = builder.setPrettyPrinting().create();

    public static ArrayList<Long> configuredPlaytime = new ArrayList<>();


    public static void checkStartupBackups () {
        //do it here to prevent excess file i/o and reduce work needed for support on a version-by-version basis

        prepareBackupDestination();

        File file = new File(ConfigManager.path.get());
        File backupManifest = new File(file, "manifest.json");
        if (backupManifest.exists()) {
            try {
                BackupManifest manifest = gson.fromJson(new String(Files.readAllBytes(backupManifest.toPath())), BackupManifest.class);
                
                ABCore.activity = manifest.general.activity;

            }
            catch (IOException e) {
                ABCore.errorLogger.accept("Error reading player actiivty from backup manifest!!");
                e.printStackTrace();
            }
        }

        if (ConfigManager.startup.get()) {
            checkAndMakeBackups(Math.max(5000, ConfigManager.delay.get() * 1000));
        }

        new BackupTimingThread().start();
    }

    public static void checkShutdownBackups() {
        if (ConfigManager.shutdown.get()) {
            checkAndMakeBackups();
        }
    }

    public static void checkAndMakeBackups(long delay) {
        BackupCheckEnum e = checkBackups();
        if (e.success()) {
            makeSingleBackup(delay);
        }
    }

    public static void checkAndMakeBackups() {
        checkAndMakeBackups(0);
    }


    public static BackupCheckEnum checkBackups() {
        prepareBackupDestination();
        if (!ConfigManager.enabled.get()) return BackupCheckEnum.DISABLED;
        if (ConfigManager.activity.get() && !ABCore.activity) return BackupCheckEnum.NOACTIVITY;
        if (checkMostRecentBackup()) return BackupCheckEnum.TOORECENT;

        return BackupCheckEnum.SUCCESS;

    }    

    private static void prepareBackupDestination() {
        File file = new File(ConfigManager.path.get());

        if (!file.exists()) {
            file.mkdirs();
        }
        prepareReadMe(file);
        prepareRestorationScripts(file);

        File zipFile = new File(file, "/zips/");
        if (!zipFile.exists()) {
            zipFile.mkdirs();
        }
        File differential = new File(file, "/differential/");
        if (!differential.exists()) {
            differential.mkdirs();
        }
        File incremental = new File(file, "/incremental/");
        if (!incremental.exists()) {
            incremental.mkdirs();
        }

        File backupManifest = new File(file, "manifest.json");
        if (!backupManifest.exists()) {
            try {
                backupManifest.createNewFile();
                BackupManifest manifest = BackupManifest.defaults();
                File differentialManifest = new File(file, "/differential/manifest.json");
                if (differentialManifest.exists()) {
                    try {
                        DifferentialManifest differentialManifest2 = gson.fromJson(new String(Files.readAllBytes(differentialManifest.toPath())), DifferentialManifest.class);
                        manifest.differential.setChainLength(differentialManifest2.getChain());
                        manifest.differential.setLastBackup(differentialManifest2.getLastFull());

                        differentialManifest.delete();
    
                    } catch (IOException e) {
    
                    }
                }
                
                File incrementalManifest = new File(file, "/incremental/manifest.json");
                if (incrementalManifest.exists()) {
                    try {
                        DifferentialManifest incrementalManifest2 = gson.fromJson(new String(Files.readAllBytes(incrementalManifest.toPath())), DifferentialManifest.class);
                        manifest.incremental.setChainLength(incrementalManifest2.getChain());
                        manifest.incremental.setLastBackup(incrementalManifest2.getLastFull());

                        incrementalManifest.delete();
    
                    } catch (IOException e) {
    
                    }
                }
    
                
                FileWriter writer = new FileWriter(backupManifest);
                writer.write(gson.toJson(manifest));
                writer.flush();
                writer.close();

            }
            catch (IOException e) {
                ABCore.errorLogger.accept("Error initialising backup manifest!!");
                e.printStackTrace();
            }
        }
    }

    private static void prepareReadMe(File path) {
        File readme = new File(path, "README-BEFORE-RESTORING.txt");
        if (!readme.exists()) {
            try {
                InputStream is = BackupWrapper.class.getClassLoader().getResourceAsStream("advancedbackups-readme.txt");
                readme.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(readme);
                byte[] buf = new byte[1028];
                int n;
                while ((n = is.read(buf)) > 0) {
                    outputStream.write(buf, 0, n);
                }
                outputStream.flush();
                outputStream.close();
                   
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
    }

    //generate scripts that run the `java -jar` command.
    private static void prepareRestorationScripts(File path) {
        try {
            String dir = ABCore.modJar.getParent();
            String file = ABCore.modJar.getName();
            File script;
            Boolean flag = System.getProperty("os.name").toLowerCase().contains("windows");
    
            if (flag) script = new File(path, "restore-script.bat");
    
            else script = new File(path, "restore-script.sh");
    
            if (script.exists()) script.delete();
    
            script.createNewFile();
    
            FileWriter writer = new FileWriter(script);
    
            if (flag) {
                writer.append("@echo off \n");
                writer.append(dir.charAt(0) + ":\n"); // command prompt defaults to c:, if this is wrong then we need to change drive.
                writer.append("cd \"" + dir + "\"\n");
                writer.append("java -jar \"" + file + "\"\n");
            }
            else {
                writer.append("cd \"" + dir + "\"\n");
                writer.append("java -jar \"" + file + "\"\n");
            }
            
            writer.flush();
            writer.close(); 

        }
        catch (IOException e) {
            ABCore.errorLogger.accept("Error writing restoration scripts! Manual running of jar will still work.");
            e.printStackTrace();
        }

    }


    public static boolean checkMostRecentBackup() {
        // Return true if the time difference between the most recent backup and the backup-to-be 
        //    is less than specified in the config.

        Date date = new Date();
        long configVal = (long) (3600000F * ConfigManager.minFrequency.get());
        return (date.getTime() - mostRecentBackupTime()) < configVal;
    }


    public static long mostRecentBackupTime() {

        File directory = new File(ConfigManager.path.get());

        switch(ConfigManager.type.get()) {
            case "zip" : {
                directory = new File(directory, "/zips/");
                break;
            }
            case "differential" : {
                directory = new File(directory, "/differential/");
                break;
            }
            case "incremental" : {
                directory = new File(directory, "/incremental/");
                break;
            }

        }

        File[] files = directory.listFiles();
        long lastModifiedTime = Long.MIN_VALUE;
        if (files == null || files.length == 0) return 0L;
        for (File file : files) {
            if (file.lastModified() > lastModifiedTime && !file.getName().contains("manifest")) {
                lastModifiedTime = file.lastModified();
            } 
        }
        return lastModifiedTime;
    }

    
    private static void makeSingleBackup(long delay) {
        makeSingleBackup(delay, (s) -> {});
    }

    public static void makeSingleBackup(long delay, Consumer<String> output) {

        ABCore.disableSaving();
        if (ConfigManager.save.get()) {
            ABCore.saveOnce();
        }

        // Make new thread, run backup utility.
        ThreadedBackup threadedBackup = new ThreadedBackup(delay, output);
        threadedBackup.start();
        // Don't re-enable saving - leave that down to the backup thread.
    }

    public static void makeSnapshot(Consumer<String> output) {
        ABCore.disableSaving();
        if (ConfigManager.save.get()) {
            ABCore.saveOnce();
        }

        ThreadedBackup threadedBackup = new ThreadedBackup(0, output);
        threadedBackup.snapshot();
        threadedBackup.start();

    }

    public static void finishBackup() {
        File directory = new File(ConfigManager.path.get());
        ABCore.enableSaving();

        switch(ConfigManager.type.get()) {
            case "zip" : {
                directory = new File(directory, "/zips/");
                long date = Long.MIN_VALUE;
                while (true) {
                    if (calculateDirectorySize(directory) < ConfigManager.size.get() * 1000000000L) return;
                    File file = getFirstBackupAfterDate(directory, date);
                    date = file.lastModified();
                    file.delete();
                }
            }
            case "differential" : {
                directory = new File(directory, "/differential/");
                long date = Long.MIN_VALUE;
                while (true) {
                    if (calculateDirectorySize(directory) < ConfigManager.size.get() * 1000000000L) return;
                    File file = getFirstBackupAfterDate(directory, date);
                    date = file.lastModified();
                    if (file.getName().contains("full")) {
                        File nextFile = getFirstBackupAfterDate(directory, date);
                        if (nextFile.getName().contains("partial")) {
                            nextFile.delete();
                        }
                        else {
                            file.delete();
                        }
                    }
                    else {
                        file.delete();
                    }

                }
            }
            case "incremental" : {
                directory = new File(directory, "/incremental/");
                if (!ConfigManager.purgeIncrementals.get()) return;
                long date = Long.MIN_VALUE;
                while (true) {
                    if (calculateDirectorySize(directory) < ConfigManager.size.get() * 1000000000L) return;
                    if (calculateChainCount(directory) < 2) return;
                    ABCore.errorLogger.accept("Purging incremental backup chain - too much space taken up!");
                    File file = getFirstBackupAfterDate(directory, date);
                    date = file.lastModified();
                    if (file.getName().contains("full")) {
                        file.delete();
                        while (true) {
                            file = getFirstBackupAfterDate(directory, date);
                            date = file.lastModified();
                            if (file.getName().contains("full")) {
                                file.delete();
                            }
                            else {
                                break;
                            }
                        }
                    }
                    else {
                        file.delete();
                    }

                }
            }

        }
    }


    public static long calculateDirectorySize(File directory) {
        long size = 0;
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) return size;
        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            }
            else {
                size += calculateDirectorySize(file);
            }
        }
        return size;
    }

    public static File getFirstBackupAfterDate(File directory, long date) {
        File[] files = directory.listFiles();
        File oldestFile = null;
        long currentDate = Long.MAX_VALUE;
        if (files == null || files.length == 0) return null;
        for (File file : files) {
            if (file.lastModified() < currentDate && file.lastModified() > date) {
                currentDate = file.lastModified();
                oldestFile = file;
            } 
        }

        return oldestFile;
    }

    public static int calculateChainCount(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) return 0;
        for (File file : files) {
            if (file.getName().contains("full")) {
                count++;
            } 
        }
        return count;
    }
}
