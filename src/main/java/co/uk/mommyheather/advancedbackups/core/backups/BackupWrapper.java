package co.uk.mommyheather.advancedbackups.core.backups;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;


import co.uk.mommyheather.advancedbackups.PlatformMethodWrapper;
import co.uk.mommyheather.advancedbackups.core.config.AVConfig;

public class BackupWrapper {

    public static ArrayList<Long> configuredPlaytime = new ArrayList<>();


    public static void checkStartupBackups () {
        if (AVConfig.config.getForceOnStartup()) {
            checkAndMakeBackups(Math.max(5000, AVConfig.config.getStartupDelay() * 1000));
        }

        new BackupTimingThread().start();
    }

    public static void checkShutdownBackups() {
        if (AVConfig.config.getForceOnShutdown()) {
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
        if (!AVConfig.config.getEnabled()) return BackupCheckEnum.DISABLED;
        if (AVConfig.config.getRequireActivity() && !PlatformMethodWrapper.activity) return BackupCheckEnum.NOACTIVITY;
        if (checkMostRecentBackup()) return BackupCheckEnum.TOORECENT;

        return BackupCheckEnum.SUCCESS;

    }    

    private static void prepareBackupDestination() {
        File file = new File(AVConfig.config.getPath());

        if (!file.exists()) {
            file.mkdirs();
        }
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
    }


    public static boolean checkMostRecentBackup() {
        // Return true if the time difference between the most recent backup and the backup-to-be 
        //    is less than specified in the config.

        Date date = new Date();
        long configVal = (long) (3600000F * AVConfig.config.getMinTimer());
        return (date.getTime() - mostRecentBackupTime()) < configVal;
    }


    public static long mostRecentBackupTime() {

        File directory = new File(AVConfig.config.getPath());

        switch(AVConfig.config.getBackupType()) {
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


    public static void makeSingleBackup(long delay) {

        PlatformMethodWrapper.disableSaving();
        if (AVConfig.config.getSave()) {
            PlatformMethodWrapper.saveOnce();
        }

        // Make new thread, run backup utility.
        ThreadedBackup threadedBackup = new ThreadedBackup(delay);
        threadedBackup.start();
        // Don't re-enable saving - leave that down to the backup thread.
        
    }

    public static void finishBackup() {
        File directory = new File(AVConfig.config.getPath());
        ThreadedBackup.running = false;
        PlatformMethodWrapper.enableSaving();

        switch(AVConfig.config.getBackupType()) {
            case "zip" : {
                directory = new File(directory, "/zips/");
                long date = Long.MIN_VALUE;
                while (true) {
                    if (calculateDirectorySize(directory) < AVConfig.config.getMaxSize() * 1000000000L) return;
                    File file = getFirstBackupAfterDate(directory, date);
                    date = file.lastModified();
                    file.delete();
                }
            }
            case "differential" : {
                directory = new File(directory, "/differential/");
                long date = Long.MIN_VALUE;
                while (true) {
                    if (calculateDirectorySize(directory) < AVConfig.config.getMaxSize() * 1000000000L) return;
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
                if (!AVConfig.config.getPurgeIncrementals()) return;
                long date = Long.MIN_VALUE;
                while (true) {
                    if (calculateDirectorySize(directory) < AVConfig.config.getMaxSize() * 1000000000L) return;
                    if (calculateChainCount(directory) < 2) return;
                    PlatformMethodWrapper.errorLogger.accept("Purging incremental backup chain - too much space taken up!");
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
