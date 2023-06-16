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

import co.uk.mommyheather.advancedbackups.PlatformMethodWrapper;
import co.uk.mommyheather.advancedbackups.core.backups.gson.DifferentialManifest;
import co.uk.mommyheather.advancedbackups.core.config.AVConfig;

public class ThreadedBackup extends Thread {
    private static GsonBuilder builder = new GsonBuilder(); 
    private static Gson gson;
    private long delay;
    private static int count;
    private static float partialSize;
    private static float completeSize;
    public static boolean running = false;
    
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

        File file = new File(AVConfig.config.getPath());

        switch(AVConfig.config.getBackupType()) {
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

            File zip = new File(file.toString() + "/zips/", serialiseBackupName() + ".zip");
            FileOutputStream outputStream = new FileOutputStream(zip);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            zipOutputStream.setLevel(AVConfig.config.getCompressionLevel());

            Files.walkFileTree(PlatformMethodWrapper.worldDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    Path targetFile;
                    try {
                        targetFile = PlatformMethodWrapper.worldDir.relativize(file);
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
                        PlatformMethodWrapper.errorLogger.accept(file.toString());
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
            String name = serialiseBackupName();
            long time = 0;
            File manifestFile = differential ? new File(location.toString() + "/differential/manifest.json") : new File(location.toString() + "/incremental/manifest.json");
            DifferentialManifest manifest;
            if (manifestFile.exists()) {
                manifest = gson.fromJson(new String(Files.readAllBytes(manifestFile.toPath())), DifferentialManifest.class);
            }
            else {
                manifest = DifferentialManifest.defaultValues();
            }

            long comp = differential ? manifest.getLastFull() : manifest.getLastPartial();
            ArrayList<Path> toBackup = new ArrayList<>();
            ArrayList<Path> completeBackup = new ArrayList<>();


            boolean completeTemp = manifest.getComplete().size() == 0 || manifest.getChain() >= AVConfig.config.getMaxDepth() ? true : false;
            
            Files.walkFileTree(PlatformMethodWrapper.worldDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    Path targetFile;
                    targetFile = PlatformMethodWrapper.worldDir.relativize(file);
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
            if ((partialSize / completeSize) * 100F > AVConfig.config.getMaxSizePercent()) {
                complete = true;
                toBackup.clear();
                toBackup.addAll(completeBackup);
            }
            
            name += complete? "-full":"-partial";

            

            if (AVConfig.config.getCompressChains()) {
                File zip = differential ? new File(location.toString() + "/differential/", name +".zip") : new File(location.toString() + "/incremental/", name +".zip");
                FileOutputStream outputStream = new FileOutputStream(zip);
                ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
                zipOutputStream.setLevel(AVConfig.config.getCompressionLevel());

                for (Path path : toBackup) {
                    zipOutputStream.putNextEntry(new ZipEntry(path.toString()));
                    byte[] bytes = Files.readAllBytes(new File(PlatformMethodWrapper.worldDir.toString(), path.toString()).toPath());
                    zipOutputStream.write(bytes, 0, bytes.length);
                    zipOutputStream.closeEntry();
                }
                zipOutputStream.flush();
                zipOutputStream.close();

                time = zip.lastModified();
            }
            else {
                File dest = differential ? new File(location.toString() + "/differential/", name + "/") :new File(location.toString() + "/incremental/", name + "/");
                dest.mkdirs();
                for (Path path : toBackup) {
                    File out = new File(dest, path.toString());
                    if (!out.getParentFile().exists()) {
                        out.getParentFile().mkdirs();
                    }
                    Files.copy(new File(PlatformMethodWrapper.worldDir.toString(), path.toString()).toPath(), out.toPath());
                }
                time = dest.lastModified();
            }

            //Finally, update + write the manifest
            if (complete || toBackup.size() >= count) {
                manifest.setChain(0);
                manifest.getComplete().add(time);
                manifest.setLastFull(time);
            }
            else {
                manifest.setChain(manifest.getChain() + 1);
                manifest.getPartial().add(new Date().getTime());
                manifest.setLastPartial(new Date().getTime());
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


    public static String serialiseBackupName() {
        Date date = new Date();
        String pattern = "yyyy-MM-dd hh-mm-ss";
        
        return "backup-" + new SimpleDateFormat(pattern).format(date);
    }
}
