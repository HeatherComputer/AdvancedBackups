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
import com.google.gson.JsonParseException;

import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.gson.BackupManifest;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;

public class BackupWrapper {
    
    
    private static GsonBuilder builder = new GsonBuilder(); 
    private static Gson gson = builder.setPrettyPrinting().create();
    
    public static ArrayList<Long> configuredPlaytime = new ArrayList<>();
    
    
    public static void checkStartupBackups () {
        //do it here to prevent excess file i/o and reduce work needed for support on a version-by-version basis
        
        prepareBackupDestination();
        
        File file = new File(ABCore.backupPath);
        File backupManifest = new File(file, "manifest.json");
        if (backupManifest.exists()) {
            try {
                try {
                    BackupManifest manifest = gson.fromJson(new String(Files.readAllBytes(backupManifest.toPath())), BackupManifest.class);
                    
                    ABCore.activity = manifest.general.activity;
                    
                } catch (JsonParseException e) {
                    ABCore.errorLogger.accept("Malformed backup manifest! Will be completely replaced, and will assume player activity has changed...");
                    
                    BackupManifest manifest = BackupManifest.defaults();
                    
                    manifest.general.activity = true;
                    ABCore.activity = true;
                    
                    FileWriter writer = new FileWriter(backupManifest);
                    writer.write(gson.toJson(manifest));
                    writer.flush();
                    writer.close();
                    
                }
            }
            catch (IOException e) {
                ABCore.errorLogger.accept("Error reading player actiivty from backup manifest!!");
                e.printStackTrace();
            }
        }
        
        if (ConfigManager.startup.get()) {
            checkAndMakeBackups(Math.max(5000, ConfigManager.delay.get() * 1000), false);
        }
        
        //new BackupTimingThread().start();
    }
    
    public static void checkShutdownBackups() {
        if (ConfigManager.shutdown.get()) {
            checkAndMakeBackups(0, true);
        }
    }
    
    public static void checkAndMakeBackups(long delay, boolean shutdown) {
        BackupCheckEnum e = checkBackups();
        if (e.success()) {
            makeSingleBackup(delay, shutdown);
        }
    }
    
    public static void checkAndMakeBackups() {
        checkAndMakeBackups(0, false);
    }
    
    
    public static BackupCheckEnum checkBackups() {
        //We shouldn't need this anymore.
        //prepareBackupDestination();
        if (!ConfigManager.enabled.get()) return BackupCheckEnum.DISABLED;
        if (ConfigManager.activity.get() && !ABCore.activity) return BackupCheckEnum.NOACTIVITY;
        if (checkMostRecentBackup()) return BackupCheckEnum.TOORECENT;
        
        return BackupCheckEnum.SUCCESS;
        
    }    
    
    private static void prepareBackupDestination() {
        File file = new File(ABCore.backupPath);
        
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
        File snapshots = new File(file, "/snapshots/");
        if (!snapshots.exists()) {
            snapshots.mkdirs();
        }
        
        File backupManifest = new File(file, "manifest.json");
        if (!backupManifest.exists()) {
            try {
                backupManifest.createNewFile();
                BackupManifest manifest = BackupManifest.defaults();
                
                //NOW DISABLED - CODE FOR MIGRATING FROM 1.X TO 2.X, BUT IS USELESS IN 3.X
                /*
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
                }*/
                
                
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
        File readme = new File(path.getParent(), "README-BEFORE-RESTORING.txt");
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
            FileWriter writer;
            
            
            script = new File(path.getParent(), "restore-script.bat");
            
            if (script.exists()) script.delete();
            
            script.createNewFile();
            writer = new FileWriter(script);
            
            writer.append("@echo off \n");
            writer.append(dir.charAt(0) + ":\n"); // command prompt defaults to c:, if this is wrong then we need to change drive.
            writer.append("cd \"" + dir + "\"\n");
            writer.append("java -jar \"" + file + "\"\n");
            writer.append("pause\n"); //simply prompts the user to press any key to continue, so they can see the confirmation message or error message
            
            writer.flush();
            writer.close(); 
            
            script = new File(path.getParent(), "restore-script.sh");
            
            if (script.exists()) script.delete();
            
            script.createNewFile();
            writer = new FileWriter(script);
            
            writer.append("cd \"" + dir + "\"\n");
            writer.append("java -jar \"" + file + "\"\n");
            
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
        
        File directory = new File(ABCore.backupPath);
        
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
    
    
    public static void makeSingleBackup(long delay, boolean shutdown) {
        makeSingleBackup(delay, (s) -> {}, shutdown);
    }
    
    public static void makeSingleBackup(long delay, Consumer<String> output, boolean shutdown) {
        
        try {  
            ABCore.disableSaving();
            if (ConfigManager.save.get()) {
                ABCore.saveOnce();
            }
        } catch (Exception e) {
            ABCore.errorLogger.accept("Error saving or disabling saving!");
            e.printStackTrace();
        }
        
        if (ThreadedBackup.running) {
            ABCore.errorLogger.accept("Backup already running!");
            new Exception().printStackTrace();
            return;
        }
        // Make new thread, run backup utility.
        ThreadedBackup.running = true;
        ThreadedBackup threadedBackup = new ThreadedBackup(delay, output);
        if (shutdown) threadedBackup.shutdown();
        
        threadedBackup.start();
        // Don't re-enable saving - leave that down to the backup thread.
    }
    
    public static void makeSnapshot(Consumer<String> output) {
        ABCore.disableSaving();
        if (ConfigManager.save.get()) {
            ABCore.saveOnce();
        }
        
        if (ThreadedBackup.running) {
            ABCore.errorLogger.accept("Backup already running!");
            new Exception().printStackTrace();
            return;
        }
        
        ThreadedBackup.running = true;
        ThreadedBackup threadedBackup = new ThreadedBackup(0, output);
        threadedBackup.snapshot();
        threadedBackup.start();
        
    }
    
    public static void finishBackup(boolean snapshot) {
        
        ABCore.resetActivity();
        
        if (snapshot) return;
        
        File directory = new File(ABCore.backupPath);
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
        
        checkSize(directory);
        checkCount(directory);
        checkDates(directory);
        
    }
    
    public static File getDependent(File in) {
        
        File file = getFirstBackupAfterDate(in.getParentFile(), in.lastModified());
        if (file.getName().contains("-partial")) return file;
        
        return null;
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
    
    
    public static int calculateBackupCount(File directory) {
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) return 0;
        return files.length;
    }
    
    private static void checkCount(File directory) {
        if (ConfigManager.backupsToKeep.get() <= 0) return;
        long date = 0;
        while (true) {
            if (calculateBackupCount(directory) <= ConfigManager.backupsToKeep.get()) return;
            File file = getFirstBackupAfterDate(directory, date);
            File dependent = getDependent(file);
            if (dependent == null) {
                //either a broken differential / incremental chain, a full backup with no dependencies or a zip backup
                if (file.isDirectory()) deleteDirectoryContents(file);
                file.delete();
                return;
            }
            else {
                
                //because we can only purge full incremental chains, we need to make sure we're good to delete the entire chain
                if (ConfigManager.type.get().equals("incremental")) {
                    if (!ConfigManager.purgeIncrementals.get()) return;
                    if (calculateChainCount(directory) <= ConfigManager.incrementalChains.get()) return;

                    if (file.isDirectory()) deleteDirectoryContents(file);
                    file.delete();
                    if (dependent.isDirectory()) deleteDirectoryContents(dependent);
                    dependent.delete();
                    while ((dependent = getDependent(dependent)) != null) {
                        if (dependent.isDirectory()) deleteDirectoryContents(dependent);
                        dependent.delete();
                    }
                }
                else {
                    if (dependent.isDirectory()) deleteDirectoryContents(dependent);
                    dependent.delete();
                }
            }
        }
        
    }
    
    private static void checkDates(File directory) {
        if (ConfigManager.daysToKeep.get() <= 0) return;
        
        long date = 0;
        long comp = mostRecentBackupTime();
        while (true) {
            File file = getFirstBackupAfterDate(directory, date);
            date = file.lastModified();
            long days = (comp - date) / 86400000L;
            if (days <= ConfigManager.daysToKeep.get()) return;
            File dependent = getDependent(file);
            if (dependent == null) {
                //either a broken differential / incremental chain, a full backup with no dependencies or a zip backup
                if (file.isDirectory()) deleteDirectoryContents(file);
                file.delete();
                return;
            }
            else {
                date = dependent.lastModified();
                days = (comp - date) / 86400000L;
                if (days <= ConfigManager.daysToKeep.get()) return;
                //don't purge unless the dependent is also eligible for deletion
                
                //because we can only purge full incremental chains, we need to make sure we're good to delete the entire chain
                if (ConfigManager.type.get().equals("incremental")) {
                    if (!ConfigManager.purgeIncrementals.get()) return;
                    if (calculateChainCount(directory) <= ConfigManager.incrementalChains.get()) return;
                    
                    //now we need to make sure the entire chain meets the deletion date.
                    //is this a good strategy?
                    while ((dependent = getDependent(dependent)) != null) {
                        date = dependent.lastModified();
                        days = (comp - date) / 86400000L;
                        if (days <= ConfigManager.daysToKeep.get()) return;
                    }
                    dependent = getDependent(file);
                    if (file.isDirectory()) deleteDirectoryContents(file);
                    file.delete();
                    if (dependent.isDirectory()) deleteDirectoryContents(dependent);
                    dependent.delete();
                    while ((dependent = getDependent(dependent)) != null) {
                        if (dependent.isDirectory()) deleteDirectoryContents(dependent);
                        dependent.delete();
                    }


                }
                else {
                    if (dependent.isDirectory()) deleteDirectoryContents(dependent);
                    dependent.delete();
                }
            }
        }
    }
    
    private static void checkSize(File directory) {
        if (ConfigManager.size.get() <= 0F) return;
        if (calculateDirectorySize(directory) < ConfigManager.size.get() * 1000000000L) return;
        long date = 0;
        while (true) {
            if (calculateDirectorySize(directory) < ConfigManager.size.get() * 1000000000L) return;
            File file = getFirstBackupAfterDate(directory, date);
            File dependent = getDependent(file);
            if (dependent == null) {
                //either a broken differential / incremental chain, a full backup with no dependencies or a zip backup
                if (file.isDirectory()) deleteDirectoryContents(file);
                file.delete();
                return;
            }
            else {
                
                //because we can only purge full incremental chains, we need to make sure we're good to delete the entire chain
                if (ConfigManager.type.get().equals("incremental")  ) {
                    if (!ConfigManager.purgeIncrementals.get()) return;
                    if (calculateChainCount(directory) <= ConfigManager.incrementalChains.get()) return;

                    if (file.isDirectory()) deleteDirectoryContents(file);
                    file.delete();
                    if (dependent.isDirectory()) deleteDirectoryContents(dependent);
                    dependent.delete();
                    while ((dependent = getDependent(dependent)) != null) {
                        if (dependent.isDirectory()) deleteDirectoryContents(dependent);
                        dependent.delete();
                    }
                }
                else {
                    if (dependent.isDirectory()) deleteDirectoryContents(dependent);
                    dependent.delete();
                }
            }
        }
        
    }

    private static void deleteDirectoryContents(File directory) {
        if (!directory.isDirectory()) return; //safety check
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) deleteDirectoryContents(file);
            file.delete();
        }
    }
    
    
    /*private static void checkSize(File directory) {
        if (ConfigManager.size.get() <= 0F) return;
        if (calculateDirectorySize(directory) < ConfigManager.size.get() * 1000000000L) return;
        long date = 0;
        
        switch(ConfigManager.type.get()) {
            case "zip" : {
                while (true) {
                    if (calculateDirectorySize(directory) < ConfigManager.size.get() * 1000000000L) return;
                    File file = getFirstBackupAfterDate(directory, date);
                    date = file.lastModified();
                    file.delete();
                }
            }
            case "differential" : {
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
                if (!ConfigManager.purgeIncrementals.get()) return;
                while (true) {
                    if (calculateDirectorySize(directory) < ConfigManager.size.get() * 1000000000L) return;
                    if (calculateChainCount(directory) <= ConfigManager.incrementalChains.get()) return;
                    ABCore.errorLogger.accept("Purging incremental backup chain - too much space taken up!");
                    File file = getFirstBackupAfterDate(directory, date);
                    date = file.lastModified();
                    if (file.getName().contains("full")) {
                        file.delete();
                        while (true) {
                            file = getFirstBackupAfterDate(directory, date);
                            date = file.lastModified();
                            if (!file.getName().contains("full")) {
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
    }*/
}
