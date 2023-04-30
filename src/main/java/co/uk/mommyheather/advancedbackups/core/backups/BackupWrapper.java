package co.uk.mommyheather.advancedbackups.core.backups;

import java.io.File;
import java.util.Date;


import co.uk.mommyheather.advancedbackups.PlatformMethodWrapper;
import co.uk.mommyheather.advancedbackups.core.config.AVConfig;

public class BackupWrapper {


    public static void checkStartupBackups () {
        if (AVConfig.config.getForceOnStartup()) {
            checkAndMakeBackups();
        }
    }

    public static void checkShutdownBackups() {
        if (AVConfig.config.getForceOnShutdown()) {
            checkAndMakeBackups();
        }
    }

    public static void checkAndMakeBackups() {
        prepareBackupDestination();

        if (!AVConfig.config.getEnabled()) return;
        if (AVConfig.config.getRequireActivity() && !PlatformMethodWrapper.activity) return;
        if (checkMostRecentBackup()) return;

        // Finally :
        makeSingleBackup();
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


    private static boolean checkMostRecentBackup() {
        // Return true if the time difference between the most recent backup and the backup-to-be 
        //    is less than specified in the config.
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
        if (files == null || files.length == 0) return false;
        for (File file : files) {
            if (file.lastModified() > lastModifiedTime) {
                lastModifiedTime = file.lastModified();
            } 
        }

        Date date = new Date();
        long configVal = (long) (3600000F * AVConfig.config.getMinTimer());
        return (date.getTime() - lastModifiedTime) < configVal;
    }


    public static void makeSingleBackup() {

        PlatformMethodWrapper.disableSaving();
        PlatformMethodWrapper.saveOnce();

        // Make new thread, run backup utility.
        ThreadedBackup threadedBackup = new ThreadedBackup();
        threadedBackup.run();
        // Don't re-enable saving - leave that down to the backup thread.
        
    }

    public static void finishBackup() {
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

        while (true)
        {
            File[] files = directory.listFiles();
            File oldestFile = null;
            long totalLength = 0;
            long lastModifiedTime = Long.MAX_VALUE;
            if (files == null || files.length == 0) break;
            for (File file : files) {
                if (file.isFile()) {
                    totalLength += file.length();
                }
                else {
                    totalLength += calculateDirectorySize(file);
                }
                if (file.lastModified() < lastModifiedTime) {
                    lastModifiedTime = file.lastModified();
                    oldestFile = file;
                } 
            }    
            if (oldestFile != null && totalLength >= AVConfig.config.getMaxSize() * 1000000000) {
                oldestFile.delete();
            }
            else {
                break;
            }
        }

        
        PlatformMethodWrapper.enableSaving();


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
}
