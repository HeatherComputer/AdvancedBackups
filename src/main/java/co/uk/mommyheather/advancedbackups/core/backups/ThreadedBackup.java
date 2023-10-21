package co.uk.mommyheather.advancedbackups.core.backups;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.gson.BackupManifest;
import co.uk.mommyheather.advancedbackups.core.backups.gson.HashList;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;

public class ThreadedBackup extends Thread {
    private static GsonBuilder builder = new GsonBuilder(); 
    private static Gson gson;
    private long delay;
    private static int count;
    private static float partialSize;
    private static float completeSize;
    public static volatile boolean running = false;
    public static volatile boolean wasRunning = false;
    private static String backupName;
    private Consumer<String> output;
    private boolean snapshot = false;
    
    static {
        builder.setPrettyPrinting();
        gson = builder.create();
    }
    
    public ThreadedBackup(long delay, Consumer<String> output) {
        setName("AB Active Backup Thread");
        this.output = output;
        this.delay = delay;
        count = 0;
        partialSize = 0F;
        completeSize = 0F;
    }

    @Override
    public void run() {
        try {
            sleep(delay);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            makeBackup();
        } catch (Exception e) {
            ABCore.errorLogger.accept("ERROR MAKING BACKUP!");
            e.printStackTrace();
        }

        BackupWrapper.finishBackup();
        output.accept("Backup complete!");
        wasRunning = true;
        running = false;
    }

    public void makeBackup() throws Exception {

        File file = new File(ABCore.backupPath);
        backupName = ABCore.serialiseBackupName("backup");

        if (snapshot) {
            makeZipBackup(file, true);
            if (!running) ABCore.enableSaving();
            output.accept("Snapshot created! This will not be auto-deleted.");
            return;
        }

        switch(ConfigManager.type.get()) {
            case "zip" : {
                makeZipBackup(file, false);
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

    }


    private void makeZipBackup(File file, boolean b) {
        try {

            File zip = new File(file.toString() + (snapshot ? "/snapshots/" : "/zips/"), backupName + ".zip");
            if (!ConfigManager.silent.get()) {
                ABCore.infoLogger.accept("Preparing " + (snapshot ? "snapshot" : "zip") + " backup name: " + zip.getName());
            }
            output.accept("Preparing " + (snapshot ? "snapshot" : "zip") + " backup name: " + zip.getName());
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


    private void makeDifferentialOrIncrementalBackup(File location, boolean differential) {
        try {
            if (!ConfigManager.silent.get()) {
                ABCore.infoLogger.accept("Preparing " + (differential ? "differential" : "incremental") + " backup name: " + backupName);
            }
            output.accept("Preparing " + (differential ? "differential" : "incremental") + " backup name: " + backupName);
            long time = 0;


            File manifestFile = new File(location.toString() + "/manifest.json");
            BackupManifest manifest;
            if (manifestFile.exists()) {
                manifest = gson.fromJson(new String(Files.readAllBytes(manifestFile.toPath())), BackupManifest.class);
            }
            else {
                manifest = BackupManifest.defaults();
            }

            if (manifest.differential.hashList == null) manifest.differential.hashList = new HashList();
            if (manifest.incremental.hashList == null) manifest.incremental.hashList = new HashList();

            //mappings here - file path and md5 hash
            Map<String, String> comp = differential ? manifest.differential.getHashList().getHashes() : manifest.incremental.getHashList().getHashes();
            Map<String, String> newHashes = new HashMap<String, String>();

            //long comp = differential ? manifest.differential.getLastBackup() : manifest.incremental.getLastBackup();
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
                    String hash = getFileHash(file.toAbsolutePath());
                    String compHash = comp.getOrDefault(targetFile.toString(), "");
                    completeBackup.add(targetFile);
                    if (completeTemp || !compHash.equals(hash)) {
                        toBackup.add(targetFile);
                        partialSize += attributes.size();
                        newHashes.put(targetFile.toString(), hash);
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

            if (complete || !differential) {
                comp.putAll(newHashes);
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
                    //manifest.differential.setLastBackup(new Date().getTime());
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

    public void snapshot() {
        snapshot = true;
    }



    private String getFileHash(Path path) {
        try {
            byte[] data = Files.readAllBytes(path);
            byte[] hash = MessageDigest.getInstance("MD5").digest(data);
            String checksum = new BigInteger(1, hash).toString(16);
            return checksum;
        } catch (IOException | NoSuchAlgorithmException e) {
            ABCore.errorLogger.accept("ERROR CALCULATING HASH FOR FILE! " + path.getFileName());
            ABCore.errorLogger.accept("It will be backed up anyway.");
            e.printStackTrace();
            return Integer.toString(new Random().nextInt());
        }
    }


}
