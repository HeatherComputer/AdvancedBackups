package co.uk.mommyheather.advancedbackups.core.backups;

import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.gson.BackupManifest;
import co.uk.mommyheather.advancedbackups.core.backups.gson.HashList;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private boolean shutdown = false;
    private ArrayList<String> erroringFiles = new ArrayList<>();

    public static final ArrayList<Pattern> blacklist = new ArrayList<>();

    static {
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    public ThreadedBackup(long delay, Consumer<String> output) {
        this.setName("AB Active Backup Thread");
        this.output = output;
        this.delay = delay;
        count = 0;
        partialSize = 0F;
        completeSize = 0F;
    }

    @Override
    public void run() {
        try {
            sleep(this.delay);
        } catch (Exception e) {
            ABCore.logStackTrace(e);
        }
        if (!this.shutdown) {
            BackupStatusInstance instance = new BackupStatusInstance();
            instance.setAge(System.currentTimeMillis());
            instance.setState(BackupStatusInstance.State.STARTING);
            BackupStatusInstance.setInstance(instance);
        }

        try {
            this.makeBackup();
            if (!this.shutdown) {
                BackupStatusInstance instance = new BackupStatusInstance();
                instance.setAge(System.currentTimeMillis());
                instance.setState(BackupStatusInstance.State.COMPLETE);
                BackupStatusInstance.setInstance(instance);
            }
        } catch (InterruptedException e) {
            this.output.accept("Backup cancelled!");
            this.performDelete(new File(ABCore.backupPath));
            if (!this.shutdown) {
                BackupStatusInstance instance = new BackupStatusInstance();
                instance.setAge(System.currentTimeMillis());
                instance.setState(BackupStatusInstance.State.CANCELLED);
                BackupStatusInstance.setInstance(instance);
            }
            wasRunning = true;
            running = false;
            return;
        } catch (Exception e) {
            ABCore.errorLogger.accept("ERROR MAKING BACKUP!");
            ABCore.logStackTrace(e);
            if (!this.shutdown) {
                BackupStatusInstance instance = new BackupStatusInstance();
                instance.setAge(System.currentTimeMillis());
                instance.setState(BackupStatusInstance.State.FAILED);
                BackupStatusInstance.setInstance(instance);
            }
            this.performDelete(new File(ABCore.backupPath));
            wasRunning = true;
            running = false;
            return;
        }

        BackupWrapper.finishBackup(this.snapshot);
        if (this.erroringFiles.isEmpty()) {
            this.output.accept("Backup complete!");
        } else {
            this.output.accept("Backup completed with errors - the following files could not be backed up.");
            this.output.accept("Check the logs for more information :");
            for (String string : this.erroringFiles) {
                this.output.accept(string);
                //TODO have a toast for backup complete with errors
            }
        }
        wasRunning = true;
        running = false;
    }


    public void makeBackup() throws Exception {

        File file = new File(ABCore.backupPath);
        backupName = ABCore.serialiseBackupName("incomplete");

        if (this.snapshot) {
            this.makeZipBackup(file, true);
            this.output.accept("Snapshot created! This will not be auto-deleted.");
            performRename(file);
            return;
        }

        switch (ConfigManager.type.get()) {
            case "zip": {
                this.makeZipBackup(file, false);
                break;
            }
            case "differential": {
                this.makeDifferentialOrIncrementalBackup(file, true);
                break;
            }
            case "incremental": {
                this.makeDifferentialOrIncrementalBackup(file, false);
                break;
            }
        }

        performRename(file);

    }


    private void makeZipBackup(File file, boolean b) throws InterruptedException, IOException {
        try {

            File zip = new File(file.toString() + (this.snapshot ? "/snapshots/" : "/zips/"), backupName + ".zip");
            ABCore.infoLogger.accept("Preparing " + (this.snapshot ? "snapshot" : "zip") + " backup with name: " + zip.getName().replace("incomplete", "backup"));
            this.output.accept("Preparing " + (this.snapshot ? "snapshot" : "zip") + " backup with name: " + zip.getName().replace("incomplete", "backup"));
            FileOutputStream outputStream = new FileOutputStream(zip);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            zipOutputStream.setLevel((int) ConfigManager.compression.get());

            ArrayList<Path> paths = new ArrayList<>();

            Files.walkFileTree(ABCore.worldDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    Path targetFile;
                    targetFile = ABCore.worldDir.relativize(file);
                    if (targetFile.toFile().getName().compareTo("session.lock") == 0) {
                        return FileVisitResult.CONTINUE;
                    }
                    if (matchesBlacklist(targetFile)) return FileVisitResult.CONTINUE;

                    paths.add(file);
                    return FileVisitResult.CONTINUE;
                }
            });

            Path targetFile;

            int max = paths.size();
            int index = 0;

            if (!this.shutdown) {
                BackupStatusInstance instance = new BackupStatusInstance();
                instance.setAge(System.currentTimeMillis());
                instance.setMax(max);
                instance.setProgress(index);
                instance.setState(BackupStatusInstance.State.STARTED);
                BackupStatusInstance.setInstance(instance);
            }

            for (Path path : paths) {
                try {
                    targetFile = ABCore.worldDir.relativize(path);
                    zipOutputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                    byte[] bytes = new byte[(int) ConfigManager.buffer.get()];
                    try {
                        FileInputStream is = new FileInputStream(path.toFile());
                        while (true) {
                            int i = is.read(bytes);
                            if (i < 0) break;
                            zipOutputStream.write(bytes, 0, i);
                        }
                        is.close();
                    } catch (Exception e) {
                        ABCore.errorLogger.accept("Error backing up file : " + targetFile.toString());
                        ABCore.logStackTrace(e);
                        this.erroringFiles.add(targetFile.toString());
                    }

                    zipOutputStream.closeEntry();
                    //We need to handle interrupts in various styles in different parts of the process!
                    if (this.isInterrupted()) {
                        zipOutputStream.close();
                        //Here, we need to close the outputstream - otherwise we risk a leak!
                        throw new InterruptedException();
                    }
                    index++;
                    if (!this.shutdown) {
                        BackupStatusInstance instance = new BackupStatusInstance();
                        instance.setAge(System.currentTimeMillis());
                        instance.setMax(max);
                        instance.setProgress(index);
                        instance.setState(BackupStatusInstance.State.STARTED);
                        BackupStatusInstance.setInstance(instance);
                    }
                } catch (IOException e) {
                    ABCore.logStackTrace(e);
                    ABCore.errorLogger.accept(file.toString());
                    throw e;
                }
            }

            zipOutputStream.flush();
            zipOutputStream.close();

        } catch (IOException e) {
            ABCore.logStackTrace(e);
            throw e;
        }
    }


    private void makeDifferentialOrIncrementalBackup(File location, boolean differential) throws InterruptedException, IOException {
        try {
            ABCore.infoLogger.accept("Preparing " + (differential ? "differential" : "incremental") + " backup with name: " + backupName.replace("incomplete", "backup"));
            this.output.accept("Preparing " + (differential ? "differential" : "incremental") + " backup with name: " + backupName.replace("incomplete", "backup"));
            long time = 0;


            File manifestFile = new File(location.toString() + "/manifest.json");
            BackupManifest manifest;
            if (manifestFile.exists()) {
                try {
                    manifest = gson.fromJson(new String(Files.readAllBytes(manifestFile.toPath())), BackupManifest.class);

                } catch (JsonParseException e) {

                    ABCore.errorLogger.accept("Malformed backup manifest! It will have to be reset...");
                    this.output.accept("Malformed backup manifest! It will have to be reset...");
                    ABCore.logStackTrace(e);

                    manifest = BackupManifest.defaults();
                }
            } else {
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
                    if (matchesBlacklist(targetFile)) return FileVisitResult.CONTINUE;
                    count++;
                    completeSize += attributes.size();
                    String hash = ThreadedBackup.this.getFileHash(file.toAbsolutePath());
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

            backupName += complete ? "-full" : "-partial";


            //We need to handle interrupts in various styles in different parts of the process!
            if (this.isInterrupted()) {
                //Here however, we have nothing to close! just throw
                throw new InterruptedException();
            }

            if (ConfigManager.compressChains.get()) {
                File zip = differential ? new File(location.toString() + "/differential/", backupName + ".zip") : new File(location.toString() + "/incremental/", backupName + ".zip");
                FileOutputStream outputStream = new FileOutputStream(zip);
                ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
                zipOutputStream.setLevel((int) ConfigManager.compression.get());

                int max = toBackup.size();
                int index = 0;

                if (!this.shutdown) {
                    BackupStatusInstance instance = new BackupStatusInstance();
                    instance.setAge(System.currentTimeMillis());
                    instance.setMax(max);
                    instance.setProgress(index);
                    instance.setState(BackupStatusInstance.State.STARTED);
                    BackupStatusInstance.setInstance(instance);
                }
                for (Path path : toBackup) {
                    zipOutputStream.putNextEntry(new ZipEntry(path.toString()));

                    byte[] bytes = new byte[(int) ConfigManager.buffer.get()];
                    try {
                        FileInputStream is = new FileInputStream(new File(ABCore.worldDir.toString(), path.toString()));
                        while (true) {
                            int i = is.read(bytes);
                            if (i < 0) break;
                            zipOutputStream.write(bytes, 0, i);
                        }
                        is.close();
                    } catch (Exception e) {
                        ABCore.errorLogger.accept("Error backing up file : " + path.toString());
                        ABCore.logStackTrace(e);
                        this.erroringFiles.add(path.toString());
                    }
                    zipOutputStream.closeEntry();
                    index++;
                    if (!this.shutdown) {
                        BackupStatusInstance instance = new BackupStatusInstance();
                        instance.setAge(System.currentTimeMillis());
                        instance.setMax(max);
                        instance.setProgress(index);
                        instance.setState(BackupStatusInstance.State.STARTED);
                        BackupStatusInstance.setInstance(instance);
                    }

                    //We need to handle interrupts in various styles in different parts of the process!
                    if (this.isInterrupted()) {
                        zipOutputStream.close();
                        //again, we need to close the outputstream - otherwise we risk a leak!
                        throw new InterruptedException();
                    }
                }
                zipOutputStream.flush();
                zipOutputStream.close();

                time = zip.lastModified();
            } else {
                File dest = differential ? new File(location.toString() + "/differential/", backupName + "/") : new File(location.toString() + "/incremental/", backupName + "/");
                dest.mkdirs();
                int max = toBackup.size();
                int index = 0;

                if (!this.shutdown) {
                    BackupStatusInstance instance = new BackupStatusInstance();
                    instance.setAge(System.currentTimeMillis());
                    instance.setMax(max);
                    instance.setProgress(index);
                    instance.setState(BackupStatusInstance.State.STARTED);
                    BackupStatusInstance.setInstance(instance);
                }
                for (Path path : toBackup) {
                    File out = new File(dest, path.toString());
                    if (!out.getParentFile().exists()) {
                        out.getParentFile().mkdirs();
                    }
                    Files.copy(new File(ABCore.worldDir.toString(), path.toString()).toPath(), out.toPath());
                    index++;
                    if (!this.shutdown) {
                        BackupStatusInstance instance = new BackupStatusInstance();
                        instance.setAge(System.currentTimeMillis());
                        instance.setMax(max);
                        instance.setProgress(index);
                        instance.setState(BackupStatusInstance.State.STARTED);
                        BackupStatusInstance.setInstance(instance);
                    }
                }
                time = dest.lastModified();
            }

            //Finally, update + write the manifest
            if (complete || toBackup.size() >= count) {
                if (differential) {
                    manifest.differential.setChainLength(0);
                    manifest.differential.setLastBackup(new Date().getTime());
                } else {
                    manifest.incremental.setChainLength(0);
                    manifest.incremental.setLastBackup(new Date().getTime());
                }
            } else {
                if (differential) {
                    manifest.differential.chainLength++;
                    //manifest.differential.setLastBackup(new Date().getTime());
                } else {
                    manifest.incremental.chainLength++;
                    manifest.incremental.setLastBackup(new Date().getTime());
                }
            }

            FileWriter writer = new FileWriter(manifestFile);
            writer.write(gson.toJson(manifest));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            ABCore.logStackTrace(e);
            throw e;
        }


    }

    public void snapshot() {
        this.snapshot = true;
    }

    public void shutdown() {
        this.shutdown = true;
    }


    private String getFileHash(Path path) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] data = new byte[(int) ConfigManager.buffer.get()];
            FileInputStream is = new FileInputStream(path.toFile());
            while (true) {
                int i = is.read(data);
                if (i < 0) break;
                md5.update(data, 0, i);

            }
            is.close();
            byte[] hash = md5.digest();
            String checksum = new BigInteger(1, hash).toString(16);
            return checksum;
        } catch (IOException | NoSuchAlgorithmException e) {
            ABCore.errorLogger.accept("ERROR CALCULATING HASH FOR FILE! " + path.getFileName());
            ABCore.errorLogger.accept("It will be backed up anyway.");
            ABCore.logStackTrace(e);
            return Integer.toString(new Random().nextInt());
        }
    }


    public static void performRename(File location) {
        //Renames all incomplete backups to no longer have the incomplete marker. This is only done after a successful backup!
        for (String string : new String[]{"/zips/", "/snapshots/", "/differential/", "/incremental/"}) {
            File file = new File(location, string);
            for (String backupName : file.list()) {
                if (backupName.contains("incomplete")) {
                    File file2 = new File(file, backupName);
                    File file3 = new File(file, backupName.replace("incomplete", "backup"));
                    file2.renameTo(file3);
                }
            }
        }
    }

    private void performDelete(File location) {
        //Purges all incomplete backups. This is only done after a cancelled or failed backup!
        for (String string : new String[]{"/zips/", "/snapshots/", "/differential/", "/incremental/"}) {
            File file = new File(location, string);
            for (String backupName : file.list()) {
                if (backupName.contains("incomplete")) {
                    File file2 = new File(file, backupName);
                    file2.delete();
                }
            }
        }
    }

    private static boolean matchesBlacklist(Path path) {
        for (Pattern pattern : blacklist) {
            Matcher matcher = pattern.matcher(path.toString().replace("\\", "/"));
            if (matcher.matches()) return true;
        }

        return false;
    }


}