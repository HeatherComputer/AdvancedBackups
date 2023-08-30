package co.uk.mommyheather.advancedbackups.core.backups;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;


import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.config.ABConfig;

public class BackupWrapper {

    public static ArrayList<Long> configuredPlaytime = new ArrayList<>();


    public static void checkStartupBackups () {
        if (ABConfig.config.getForceOnStartup()) {
            checkAndMakeBackups(Math.max(5000, ABConfig.config.getStartupDelay() * 1000));
        }

        new BackupTimingThread().start();
    }

    public static void checkShutdownBackups() {
        if (ABConfig.config.getForceOnShutdown()) {
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
        if (!ABConfig.config.getEnabled()) return BackupCheckEnum.DISABLED;
        if (ABConfig.config.getRequireActivity() && !ABCore.activity) return BackupCheckEnum.NOACTIVITY;
        if (checkMostRecentBackup()) return BackupCheckEnum.TOORECENT;

        return BackupCheckEnum.SUCCESS;

    }    

    private static void prepareBackupDestination() {
        File file = new File(ABConfig.config.getPath());

        if (!file.exists()) {
            file.mkdirs();
        }
        prepareReadMe(file);

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


    public static boolean checkMostRecentBackup() {
        // Return true if the time difference between the most recent backup and the backup-to-be 
        //    is less than specified in the config.

        Date date = new Date();
        long configVal = (long) (3600000F * ABConfig.config.getMinTimer());
        return (date.getTime() - mostRecentBackupTime()) < configVal;
    }


    public static long mostRecentBackupTime() {

        File directory = new File(ABConfig.config.getPath());

        switch(ABConfig.config.getBackupType()) {
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

        ABCore.disableSaving();
        if (ABConfig.config.getSave()) {
            ABCore.saveOnce();
        }

        // Make new thread, run backup utility.
        ThreadedBackup threadedBackup = new ThreadedBackup(delay);
        threadedBackup.start();
        // Don't re-enable saving - leave that down to the backup thread.
        
    }

    public static void finishBackup() {
        File directory = new File(ABConfig.config.getPath());
        ThreadedBackup.running = false;
        ABCore.enableSaving();

        switch(ABConfig.config.getBackupType()) {
            case "zip" : {
                directory = new File(directory, "/zips/");
                long date = Long.MIN_VALUE;
                while (true) {
                    if (calculateDirectorySize(directory) < ABConfig.config.getMaxSize() * 1000000000L) return;
                    File file = getFirstBackupAfterDate(directory, date);
                    date = file.lastModified();
                    file.delete();
                }
            }
            case "differential" : {
                directory = new File(directory, "/differential/");
                long date = Long.MIN_VALUE;
                while (true) {
                    if (calculateDirectorySize(directory) < ABConfig.config.getMaxSize() * 1000000000L) return;
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
                if (!ABConfig.config.getPurgeIncrementals()) return;
                long date = Long.MIN_VALUE;
                while (true) {
                    if (calculateDirectorySize(directory) < ABConfig.config.getMaxSize() * 1000000000L) return;
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
