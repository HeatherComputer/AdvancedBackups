package co.uk.mommyheather.advancedbackups.core.backups;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.file.SimplePathVisitor;

import co.uk.mommyheather.advancedbackups.PlatformMethodWrapper;
import co.uk.mommyheather.advancedbackups.core.config.AVConfig;

public class ThreadedBackup extends Thread {
    
    @Override
    public void run() {
        File file = new File(AVConfig.config.getPath());

        switch(AVConfig.config.getBackupType()) {
            case "zip" : makeZipBackup(file);
        }

        BackupWrapper.finishBackup();
    }


    private static void makeZipBackup(File file) {
        try {

            File zip = new File(file.toString() + "/zips/", serialiseBackupName() + ".zip");
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zip));
            zipOutputStream.setLevel(AVConfig.config.getCompressionLevel());

            Files.walkFileTree(PlatformMethodWrapper.worldDir, new SimplePathVisitor() {
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

            zipOutputStream.close();

        } catch (IOException e){
            // TODO : Scream at user
            e.printStackTrace();
        }
        
    }


    private static String serialiseBackupName() {
        Date date = new Date();
        String pattern = "yyyy-MM-dd hh-mm-ss";
        
        return "backup-" + new SimpleDateFormat(pattern).format(date);
    }
}
