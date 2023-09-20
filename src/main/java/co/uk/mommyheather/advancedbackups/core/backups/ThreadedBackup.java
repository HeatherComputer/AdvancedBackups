package co.uk.mommyheather.advancedbackups.core.backups;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.gson.BackupManifest;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;

public class ThreadedBackup extends Thread {
    private static GsonBuilder builder = new GsonBuilder(); 
    private static Gson gson;
    private long delay;
    private static int count;
    private static float partialSize;
    private static float completeSize;
    public static boolean running = false;
    private static String backupName;
    
    static {
        builder.setPrettyPrinting();
        gson = builder.create();
    }
    
    public ThreadedBackup(long delay) {
        setName("AB Active Backup Thread");
        this.delay = delay;
        count = 0;
        partialSize = 0F;
        completeSize = 0F;
    }

    @Override
    public void run() {
        try {
            sleep(delay);
        } catch (InterruptedException e) {
            return;
        }
        if (running) {
            return;
        }
        running = true;

        File file = new File(ConfigManager.path.get());
        backupName = ABCore.serialiseBackupName(ABCore.worldDir.getParent().toFile().getName().replaceAll(" ", "_"));

        switch(ConfigManager.type.get()) {
            case "zip" : {
                makeZipBackup(file);
                break;
            }
            case "differential" : {
                makeDifferentialOrIncrementalBackup(file, true);
                break;
            }
            case "incremental" : {
                makeDifferentialOrIncrementalBackup(file, false);
                break;
            }
        }

        BackupWrapper.finishBackup();
    }


    private static void makeZipBackup(File file) {
        try {

            File zip = new File(file.toString() + "/zips/", backupName + ".zip");
            if (!ConfigManager.silent.get()) {
                ABCore.infoLogger.accept("Preparing zip backup name: " + zip.getName());  
            }
            FileOutputStream outputStream = new FileOutputStream(zip);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            zipOutputStream.setLevel((int) ConfigManager.compression.get());

            Files.walkFileTree(ABCore.worldDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    Path targetFile;
                    try {
                        targetFile = ABCore.worldDir.relativize(file);
                        if (targetFile.toFile().getName().compareTo("session.lock") == 0) {
                            return FileVisitResult.CONTINUE;
                        }
                        zipOutputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        zipOutputStream.write(bytes, 0, bytes.length);
                        zipOutputStream.closeEntry();

                    } catch (IOException e) {
                        // TODO : Scream at user
                        e.printStackTrace();
                        ABCore.errorLogger.accept(file.toString());
                    }
                    
                        return FileVisitResult.CONTINUE;
                }
            });
            zipOutputStream.flush();
            zipOutputStream.close();

        } catch (IOException e){
            // TODO : Scream at user
            e.printStackTrace();
        }
        
    }


    private static void makeDifferentialOrIncrementalBackup(File location, boolean differential) {
        try {
            if (!ConfigManager.silent.get()) {
                ABCore.infoLogger.accept("Preparing " + (differential ? "differential" : "incremental") + " backup name: " + backupName);
            }
            long time = 0;


            File manifestFile = new File(location.toString() + "/manifest.json");
            BackupManifest manifest;
            if (manifestFile.exists()) {
                manifest = gson.fromJson(new String(Files.readAllBytes(manifestFile.toPath())), BackupManifest.class);
            }
            else {
                manifest = BackupManifest.defaults();
            }

            long comp = differential ? manifest.differential.getLastBackup() : manifest.incremental.getLastBackup();
            ArrayList<Path> toBackup = new ArrayList<>();
            ArrayList<Path> completeBackup = new ArrayList<>();

            int chain = differential ? manifest.differential.chainLength : manifest.incremental.chainLength;


            boolean completeTemp = chain >= ConfigManager.length.get() ? true : false;
            
            Files.walkFileTree(ABCore.worldDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    Path targetFile;
                    targetFile = ABCore.worldDir.relativize(file);
                    if (targetFile.toFile().getName().compareTo("session.lock") == 0) {
                        return FileVisitResult.CONTINUE;
                    }
                    count++;
                    completeSize += attributes.size();
                    completeBackup.add(targetFile);
                    if (completeTemp || attributes.lastModifiedTime().toMillis() >= comp) {
                        toBackup.add(targetFile);
                        partialSize += attributes.size();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            boolean complete = completeTemp;
            if (toBackup.size() >= count) {
                complete = true;
            }
            if ((partialSize / completeSize) * 100F > ConfigManager.chainsPercent.get()) {
                complete = true;
                toBackup.clear();
                toBackup.addAll(completeBackup);
            }
            
            backupName += complete? "-full":"-partial";

            

            if (ConfigManager.compressChains.get()) {
                File zip = differential ? new File(location.toString() + "/differential/", backupName +".zip") : new File(location.toString() + "/incremental/", backupName +".zip");
                FileOutputStream outputStream = new FileOutputStream(zip);
                ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
                zipOutputStream.setLevel((int) ConfigManager.compression.get());

                for (Path path : toBackup) {
                    zipOutputStream.putNextEntry(new ZipEntry(path.toString()));
                    byte[] bytes = Files.readAllBytes(new File(ABCore.worldDir.toString(), path.toString()).toPath());
                    zipOutputStream.write(bytes, 0, bytes.length);
                    zipOutputStream.closeEntry();
                }
                zipOutputStream.flush();
                zipOutputStream.close();

                time = zip.lastModified();
            }
            else {
                File dest = differential ? new File(location.toString() + "/differential/", backupName + "/") :new File(location.toString() + "/incremental/", backupName + "/");
                dest.mkdirs();
                for (Path path : toBackup) {
                    File out = new File(dest, path.toString());
                    if (!out.getParentFile().exists()) {
                        out.getParentFile().mkdirs();
                    }
                    Files.copy(new File(ABCore.worldDir.toString(), path.toString()).toPath(), out.toPath());
                }
                time = dest.lastModified();
            }

            //Finally, update + write the manifest
            if (complete || toBackup.size() >= count) {
                if (differential) {
                    manifest.differential.setChainLength(0);
                    manifest.differential.setLastBackup(new Date().getTime());
                }
                else {
                    manifest.incremental.setChainLength(0);
                    manifest.incremental.setLastBackup(new Date().getTime());
                }
            }
            else {
                if (differential) {
                    manifest.differential.chainLength++;
                    manifest.differential.setLastBackup(new Date().getTime());
                }
                else {
                    manifest.incremental.chainLength++;
                    manifest.incremental.setLastBackup(new Date().getTime());
                }
            }

            FileWriter writer = new FileWriter(manifestFile);
            writer.write(gson.toJson(manifest));
            writer.flush();
            writer.close();
    
        } catch (IOException e) {
            // TODO Scream at user
            e.printStackTrace();
        }


    }

}
