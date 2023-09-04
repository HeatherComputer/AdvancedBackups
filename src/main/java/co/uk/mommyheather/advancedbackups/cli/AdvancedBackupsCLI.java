package co.uk.mommyheather.advancedbackups.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.fusesource.jansi.AnsiConsole;

import co.uk.mommyheather.advancedbackups.core.ABCore;
public class AdvancedBackupsCLI {

    private static String backupLocation;
    private static File serverDir = new File(new File("").toPath().toAbsolutePath().getParent().toString());
    private static String type;
    private static Scanner input = new Scanner(System.in);
    private static ArrayList<String> fileNames = new ArrayList<>();
    private static File worldFile;
    private static String worldPath;
    public static void main(String args[]){

        //Loggers
        ABCore.infoLogger = AdvancedBackupsCLI::info;
        ABCore.warningLogger = AdvancedBackupsCLI::warn;
        ABCore.errorLogger = AdvancedBackupsCLI::error;
        
        
        System.out.print("\033[H\033[2J");
        System.out.flush();

        if (System.console() != null) {
            AnsiConsole.systemInstall(); //this gets ansi escape codes working on windows. this was a FUCKING PAIN IN MY ASS
        }
        
         
        info("Advanced Backups - Version " + AdvancedBackupsCLI.class.getPackage().getImplementationVersion());
        info("Searching for properties...", false);

        
        Properties props = new Properties();
        File file = new File(serverDir, "AdvancedBackups.properties");
        FileReader reader;
        try {
            reader = new FileReader(file);   
            props.load(reader);

            backupLocation = props.getProperty("config.advancedbackups.path");
            type = props.getProperty("config.advancedbackups.type");
        } catch (Exception e) {
            error("ERROR LOADING PROPERTIES!");
            error(getStackTrace(e));
            error("");
            error("");
            error("Ensure you're running this from within the mods directory, and the config file is in the parent directory!");
            // Fatal, cannot proceed
            return;
        }

        if (backupLocation == null || type == null) {
            error("ERROR LOADING PROPERTIES!");
            error("Backup location : " + backupLocation);
            error("Type : " + type);
            // Fatal, cannot proceed
            return;
        }

        info("Config loaded!");

        type = getBackupType();

        worldFile = getWorldFile();

        if (!worldFile.exists()) {
            error("Unable to find world folder!");
            error(worldFile.getAbsolutePath());
            error("Check if the location exists and the name is correct and try again.");
            return;

        }
        

        File backupDir;

        if (backupLocation.startsWith(Pattern.quote(File.separator))) {
            backupDir = new File(backupLocation, File.separator + type + File.separator);
        }
        else {
            backupDir = new File(serverDir, backupLocation.replaceAll(Pattern.quote("." + File.separator), "") + File.separator + type + File.separator);
        }
           
        if (!backupDir.exists()) {
            error("Could not find backup directory!");
            error(backupDir.getAbsolutePath());
            error("Have you made any backups before?");
            //Fatal, cannot continue
            return;
        }

        int backupDateIndex;
        try {
            backupDateIndex = getBackupDate(backupDir);
        } catch (IOException e) {
            error("ERROR VIEWING BACKUPS!");
            e.printStackTrace();
            return;
        }


        String restore = restoreWorldOrFile();

        if (!confirmWarningMessage()) {
            error("ABORTED - WILL NOT PROCEED.");
            return;
        }

        info("Preparing...");
        


        switch(restore) {
            case "world" : {
                //No going back now!
                //TODO : make a backup of current world state.
                deleteEntireWorld(worldFile);
                switch(type) {
                    case "zips" : { 
                        restoreFullZip(backupDateIndex, worldFile);
                        return;
                    }
                    case "differential" : {
                        restoreFullDifferential(backupDateIndex, worldFile);
                        return;
                    }
                    case "incremental" : {
                        restoreFullIncremental(backupDateIndex, worldFile);
                        return;
                    }
                }
            }
            case "file" : {
                switch(type) {
                    case "zips" : {
                        restorePartialZip(backupDateIndex, worldFile);
                        return;
                    }
                    case "differential" : {
                        restorePartialDifferential(backupDateIndex, worldFile);
                        return;
                    }
                    case "incremental" : {
                        restorePartialIncremental(backupDateIndex, worldFile);
                        return;
                    }
                }
            }
        }


    }


















    //HELPER METHODS!
    private static void info(String out, boolean line) {
        if (line) System.out.println(out);
        else System.out.print(out);
        
    }

    private static void warn(String out, boolean line) {
        if (line) System.out.println("\u001B[33m" + out + "\u001B[0m");
        else System.out.print("\u001B[33m" + out + "\u001B[0m");
        
    }

    private static void error(String out, boolean line) {
        if (line) System.out.println("\u001B[31m" + out + "\u001B[0m");
        else System.out.print("\u001B[31m" + out + "\u001B[0m");
        
    }
  
    private static void info(String out) {
        info(out, true);
    }

    private static void warn(String out) {
        warn(out, true);
    }

    private static void error(String out) {
        error(out, true);
    }


    private static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    private static String getBackupType() {
        // Select a type of backup to restore
        info("Select a backup type to restore. Your server is currently set to use " + type + " backups.");
        info("1: zip\n2: differential\n3: incremental");
        info("Enter a number or leave blank for " + type + ".");
        int inputType;
        try {
            String line = input.nextLine();
            if (line == "") {
                return type;
            }
            inputType = Integer.parseInt(line);
        } catch (InputMismatchException | NumberFormatException e) {
            warn("That was not a number. Please enter a number.");
            return getBackupType();
        }
        switch (inputType) {
            case 1 : return "zips";
            case 2 : return "differential";
            case 3 : return "incremental";
            default : {
                warn("Please pick an option between 1 and 3 inclusive.");
                return getBackupType();
            }
        }
    }


    private static File getWorldFile() {
        info("Are you on a client or server?");
        info("1: Client\n2: Server");
        info("Enter a number.");

        int inputType;
        File ret = new File(serverDir.getAbsolutePath());
        
        try {
            String line = input.nextLine();
            if (line == "") {
                warn("Please enter a number.");
                return getWorldFile();
            }
            inputType = Integer.parseInt(line);
        } catch (InputMismatchException | NumberFormatException e) {
            warn("That was not a number. Please enter a number.");
            return getWorldFile();
        }

        if (inputType < 1 || inputType > 2) {
            warn("Please enter 1 or 2.");
            return getWorldFile();
        }

        if (inputType == 1) {
            ret = new File(ret, "/saves/");
        }

        ret = new File(ret, getWorldName(ret));
        return ret;

    }

    private static String getWorldName(File dir) {
        ArrayList<String> worlds = new ArrayList<>();
        int worldIndex;
        for (File file : dir.listFiles()) {
            boolean flag = false;
            if (!file.isDirectory()) continue;
            for (File file2 : file.listFiles()) {
                if (file2.getName().contains("level.dat")) {
                    flag = true;
                }
            }
            if (flag) {
                worlds.add(file.getName());
            }
        }
        info("Please select your world. Default for servers is \"world\".");
        int index = 1;
        for (String world : worlds) {
            info (index + ". " + world);
            index++;
        }        
        try {
            String line = input.nextLine();
            if (line == "") {
                warn("Please enter a number.");
                return getWorldName(dir);
            }
            worldIndex = Integer.parseInt(line);
        } catch (InputMismatchException | NumberFormatException e) {
            warn("That was not a number. Please enter a number.");
            return getWorldName(dir);
        }
        if (worldIndex < 1 || worldIndex > worlds.size()) {
            warn("Please enter a number between " + worlds.size() + ".");
            return getWorldName(dir);
        }

        worldPath = worlds.get(worldIndex -1).replaceAll(" ", "_");
        return worlds.get(worldIndex - 1);

    }


    private static int getBackupDate(File backupDir) throws IOException {
        fileNames.clear();
        int inputType;

        info("Select a backup to restore.");

        for (File file : backupDir.listFiles()) {
            if (!file.getName().contains(worldPath)) continue;
            fileNames.add(file.getAbsolutePath());
            String out = file.getName();
            out = out.replaceAll(".zip", "");
            out = out.replaceAll(worldPath + "_", ": ");
            out = out.replaceAll("-partial", "\u001B[33m partial\u001B[0m");
            out = out.replaceAll("-full", "\u001B[32m full\u001B[0m");
            info(fileNames.size() + out);
        }

        try {
            String line = input.nextLine();
            if (line == "") {
                warn("Please enter a number.");
                return getBackupDate(backupDir);
            }
            inputType = Integer.parseInt(line);
        } catch (InputMismatchException | NumberFormatException e) {
            warn("That was not a number. Please enter a number.");
            return getBackupDate(backupDir);
        }

        if (inputType < 1 || inputType > fileNames.size()) {
            warn("Please enter a number between " + fileNames.size() + ".");
            return getBackupDate(backupDir);
        }
        
        return inputType - 1;
    }

    private static String restoreWorldOrFile() {
        info("Do you want to restore the entire world state at this point, or a singular file?");
        info("1: Entire world\n2: Single file");
        info("Enter a number.");
        int inputType;
        try {
            String line = input.nextLine();
            if (line == "") {
                return type;
            }
            inputType = Integer.parseInt(line);
        } catch (InputMismatchException | NumberFormatException e) {
            warn("That was not a number. Please enter a number.");
            return restoreWorldOrFile();
        }
        switch (inputType) {
            case 1 : return "world";
            case 2 : return "file";
            default : {
                warn("Please pick 1 or 2.");
                return restoreWorldOrFile();
            }
        }
        
    }



    private static boolean confirmWarningMessage() {
        warn("");
        warn("");
        warn("WARNING! DOING THIS WHILST THE SERVER IS RUNNING CAN CAUSE SEVERE CORRUPTION, PARTIAL RESTORATION, AND OTHER ISSUES.");
        warn("TYPE \"continue\" IF YOU WISH TO CONTINUE...", false);

        
        String line = input.nextLine();
        if (line.equals("")) {
            return confirmWarningMessage();
        }
        return line.equals("continue");
    }


    private static void restoreFullZip(int index, File worldFile) {
        byte[] buffer = new byte[1024];
        //The most basic of the bunch.
        ZipEntry entry;
        try {
            FileInputStream fileInputStream = new FileInputStream(fileNames.get(index));
            ZipInputStream zip = new ZipInputStream(fileInputStream);
            while ((entry = zip.getNextEntry()) != null) {
                File outputFile;

                //FTB Backups and some other mods need special handling.
                if (entry.getName().startsWith(worldFile.getName())) {
                    outputFile = new File(worldFile.getParentFile(), entry.getName());
                }
                else {
                    outputFile = new File(worldFile, entry.getName());
                }
                
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }

                FileOutputStream outputStream = new FileOutputStream(outputFile);
                int length = 0;
                while ((length = zip.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void restoreFullDifferential(int index, File worldFile) {
        //Do we need to check for past backups? if selected is a full backup, we do not.
        File backup = new File(fileNames.get(index));
        if (backup.getName().contains("-full")) {
            if (backup.isFile()) {
                restoreFullZip(index, worldFile);
                return;
            }
            restoreFolder(index, worldFile);
            return;
        }
        //find last FULL backup
        for (int i = index;i>0;i--) {
            String name = fileNames.get(i);
            if (name.contains("-full")) {
                info("Restoring last full backup...");
                File file = new File(name);
                if (file.isFile()) {
                    restoreFullZip(i, worldFile);
                }
                else {
                    restoreFolder(i, worldFile);
                }
                break;
            }
        }
        info("\n\nRestoring selected backup...");
        if (backup.isFile()) {
            restoreFullZip(index, worldFile);
        }
        else {
            restoreFolder(index, worldFile);
        }
    }

    private static void restoreFullIncremental(int index, File worldFile) {
        //Do we need to check for past backups? if selected is a full backup, we do not.
        File backup = new File(fileNames.get(index));
        if (backup.getName().contains("-full")) {
            if (backup.isFile()) {
                restoreFullZip(index, worldFile);
                return;
            }
            restoreFolder(index, worldFile);
            return;
        }
        //find last FULL backup
        int i = index;
        while(i >= 0) {
            String name = fileNames.get(i);
            if (name.contains("-full")) {
                info("Restoring last full backup...");
                File file = new File(name);
                if (file.isFile()) {
                    restoreFullZip(i, worldFile);
                }
                else {
                    restoreFolder(i, worldFile);
                }
                break;
            }
            i--;
        }
        //restore backups up until the selected one
        while(i < index) {
            String name = fileNames.get(i);
            info("Restoring chained backup...");
            File file = new File(name);
            if (file.isFile()) {
                restoreFullZip(i, worldFile);
            }
            else {
                restoreFolder(i, worldFile);
            }
            i++;
        }
        
        
        info("\n\nRestoring selected backup...");
        if (backup.isFile()) {
            restoreFullZip(index, worldFile);
        }
        else {
            restoreFolder(index, worldFile);
        }
    }

    private static void restorePartialZip(int index, File worldFile) {
        Path file;
        HashMap<String, Path> entries = new HashMap<>();

        try {
            FileSystem zipFs = FileSystems.newFileSystem(new File(fileNames.get(index)).toPath(), AdvancedBackupsCLI.class.getClassLoader());
            Path root = zipFs.getPath("");
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    entries.put(file.toString(), file);
                    return FileVisitResult.CONTINUE;
                }
            });

            file = getFileToRestore(entries, "");
            info("Restoring " + file.toString() + "...");
            Path outputFile = new File(worldFile, file.toString()).toPath();
            Files.copy(file, outputFile, StandardCopyOption.REPLACE_EXISTING);
            info("Done.");
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
         
    }

    private static void restorePartialDifferential(int index, File worldFile) {
        //Do we need to check for past backups? if selected is a full backup, we do not.
        HashMap<String, Object> filePaths = new HashMap<>();
        HashMap<String, String> dates = new HashMap<>();
        HashMap<String, ZipFile> entryOwners = new HashMap<>();
        try {
            File backup = new File(fileNames.get(index));
            if (!backup.getName().contains("-full")) {
                //find last FULL backup
                for (int i = index;i>=0;i--) {
                    String name = fileNames.get(i);
                    if (name.contains("-full")) {
                        addBackupNamesToLists(new File(name), entryOwners, filePaths, dates, "\u001b[31m");
                        break;
                    }    
                }
            }

            File file = new File(fileNames.get(index));
            addBackupNamesToLists(file, entryOwners, filePaths, dates, "\u001B[32m");

            HashMap<String, Object> properMapping = new HashMap<>();
            for (String date : dates.keySet()) {
                properMapping.put(
                    date + " " + dates.get(date),
                    filePaths.get(date)
                );
            }

            Object select = getFileToRestore(properMapping, "");
            if (select instanceof Path) {
                Path input = (Path) select;


                if (select.toString().replace("\\", "/").contains("-full/")) {
                    select = new File(
                        select.toString().replace("\\", "/")
                        .split("-full/")[1]
                    ).toPath();
                }
                if (select.toString().replace("\\", "/").contains("-partial/")) {
                    select = new File(
                        select.toString().replace("\\", "/")
                        .split("-partial/")[1]
                    ).toPath();
                }
    
                File outputFile = new File(worldFile, select.toString());
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
                info("\n\nRestoring file : " + select);
                Files.copy(input, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            else if (select instanceof ZipEntry) {
                ZipEntry entry = (ZipEntry) select;

                File outputFile = new File(worldFile, entry.toString());
                FileOutputStream outputStream = new FileOutputStream(outputFile);

                info("Restoring " + entry.toString() + "...");

                byte[] buffer = new byte[1028];
                InputStream inputSteam = entryOwners.get(entry.toString()).getInputStream(entry);
                int length;
                while ((length = inputSteam.read(buffer, 0, buffer.length)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
            }


        }
        catch (IOException e){
            //TODO : Scream at user
            e.printStackTrace();
        }
    }

    private static void restorePartialIncremental(int index, File worldFile) {
        //Do we need to check for past backups? if selected is a full backup, we do not.
        HashMap<String, Object> filePaths = new HashMap<>();
        HashMap<String, String> dates = new HashMap<>();
        HashMap<String, ZipFile> entryOwners = new HashMap<>();
        try {
            File backup = new File(fileNames.get(index));
            if (!backup.getName().contains("-full")) {
                int i;
                //find last FULL backup
                for (i = index;i>=0;i--) {
                    String name = fileNames.get(i);
                    if (name.contains("-full")) {
                        addBackupNamesToLists(new File(name), entryOwners, filePaths, dates, "\u001b[31m");
                        break;
                    }    
                }
                while (i < index) {
                    String name = fileNames.get(i);
                    addBackupNamesToLists(new File(name), entryOwners, filePaths, dates, "\u001b[31m");
                    i++;
                }
                
            }

            File file = new File(fileNames.get(index));
            addBackupNamesToLists(file, entryOwners, filePaths, dates, "\u001B[32m");

            HashMap<String, Object> properMapping = new HashMap<>();
            for (String date : dates.keySet()) {
                properMapping.put(
                    date + " " + dates.get(date),
                    filePaths.get(date)
                );
            }

            Object select = getFileToRestore(properMapping, "");
            if (select instanceof Path) {
                Path input = (Path) select;


                if (select.toString().replace("\\", "/").contains("-full/")) {
                    select = new File(
                        select.toString().replace("\\", "/")
                        .split("-full/")[1]
                    ).toPath();
                }
                if (select.toString().replace("\\", "/").contains("-partial/")) {
                    select = new File(
                        select.toString().replace("\\", "/")
                        .split("-partial/")[1]
                    ).toPath();
                }
    
                File outputFile = new File(worldFile, select.toString());
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
                info("\n\nRestoring file : " + select);
                Files.copy(input, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            else if (select instanceof ZipEntry) {
                ZipEntry entry = (ZipEntry) select;

                File outputFile = new File(worldFile, entry.toString());
                FileOutputStream outputStream = new FileOutputStream(outputFile);

                info("Restoring " + entry.toString() + "...");

                byte[] buffer = new byte[1028];
                InputStream inputSteam = entryOwners.get(entry.toString()).getInputStream(entry);
                int length;
                while ((length = inputSteam.read(buffer, 0, buffer.length)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
            }


        }
        catch (IOException e){
            //TODO : Scream at user
            e.printStackTrace();
        }
    }


    private static void restoreFolder(int index, File worldFile) {
        File backup = new File(fileNames.get(index));

        try {
            Files.walkFileTree(backup.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    File source = backup.toPath().relativize(file).toFile();
                    File outputFile = new File(worldFile, source.getPath());

                    if (!outputFile.getParentFile().exists()) {
                        outputFile.getParentFile().mkdirs();
                    }
                    Files.copy(file, outputFile.toPath());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private static void deleteEntireWorld(File worldDir) {
        backupExistingWorld(worldDir);
        try {
            Files.walkFileTree(worldDir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    file.toFile().delete();
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult postVisitDirectory(Path file, java.io.IOException arg1) {
                    if (file.toFile().listFiles().length == 0) {
                        file.toFile().delete();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            warn("Failed to delete file :");
            e.printStackTrace();
        }
    }

    private static void backupExistingWorld(File worldDir) {
        try {
            File out = new File(worldDir, "../cli" + ABCore.serialiseBackupName("backup") + ".zip");
            FileOutputStream outputStream = new FileOutputStream(out);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            zipOutputStream.setLevel(4);
            Files.walkFileTree(worldDir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    Path targetFile;
                    try {
                        targetFile = worldDir.toPath().relativize(file);
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
                    }
                    
                        return FileVisitResult.CONTINUE;
                }
            });
            zipOutputStream.flush();
            zipOutputStream.close();
        } catch (Exception e) {
            
        }
    }


    private static <T> T getFileToRestore(HashMap<String, T> files, String directory) {
        ArrayList<String> toDisplay = new ArrayList<>();
        HashMap<String, String> keyMap = new HashMap<>();
        int index = 1;
        for (String name : files.keySet()) {
            String name2 = name.replace("\\", "/"); // this replacement can fix some problems with wsl, and helps keep code neat.
            if (name2.contains(directory)) {
                name2 = name2.replace(directory, "");
                if (name2.startsWith(worldFile.getName())) {
                    name2 = name2.replace(worldFile.getName() + "/", "");
                }
                if (name2.contains("/")) {
                    name2 = "\u001B[33mdirectory\u001B[0m   " + name2.split("/")[0];
                }
                if (!toDisplay.contains(name2)) {
                    toDisplay.add(name2);
                    keyMap.put(name2, name);
                }
            }
        }
        toDisplay = sortStringsAlphabeticallyWithDirectoryPriority(toDisplay);
        info("Choose a file to restore.\n");
        for (String name : toDisplay) {
            if (index < 10) {
                info(index + ":  " + name);
            }
            else {
                info(index + ": " + name);
            }
            index++;
        }
        if (!directory.equals("")) {
            info(index + ": ../");
        }


        int userInput;
        try {
            String line = input.nextLine();
            if (line == "") {
                warn("Please enter a number!");
                return getFileToRestore(files, directory);
            }
            userInput = Integer.parseInt(line);
        } catch (InputMismatchException | NumberFormatException e) {
            warn("That was not a number. Please enter a number.");
            return getFileToRestore(files, directory);
        }

        if (userInput <= 0 || userInput > (directory.equals("") ? toDisplay.size() : toDisplay.size() + 1)) {
            warn("Please enter a number in the specified range!");
            return getFileToRestore(files, directory);
        }
        else if (userInput > toDisplay.size()) {
            return null;
        }
        else if (!toDisplay.get(userInput - 1).contains("\u001B[33mdirectory\u001B[0m   ")) {
            return files.get(keyMap.get(toDisplay.get(userInput -1)));
        }

        T result = getFileToRestore(files, directory + toDisplay.get(userInput -1).replace("\u001B[33mdirectory\u001B[0m   ", "") + "/");
        if (result != null) {
            return result;
        }
        else {
            return getFileToRestore(files, directory);
        }
    }


    private static ArrayList<String> sortStringsAlphabeticallyWithDirectoryPriority(ArrayList<String> in) {
        ArrayList<String> out = new ArrayList<>();
        Collections.sort(in);
        for (String string : in) {
            if (string.contains("\u001B[33mdirectory\u001B[0m")) {
                out.add(string);
            }
        }
        for (String string : in) {
            if (!string.contains("\u001B[33mdirectory\u001B[0m")) {
                out.add(string);
            }
        }
        return out;
    }


    private static void addBackupNamesToLists(File file, HashMap<String, ZipFile> entryOwners, HashMap<String, Object> filePaths, 
        HashMap<String, String> dates, String colour) throws IOException {
        
        if (file.isFile()) {
            
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();

            while (entryEnum.hasMoreElements()) {
                ZipEntry entry = entryEnum.nextElement();

                String backupName = file.toString().replace("\\", "/");
                filePaths.put(entry.toString().replace("\\", "/"), entry);
                dates.put(entry.toString().replace("\\", "/"), "\u001b[31m"
                 + backupName
                .substring(backupName.toString().lastIndexOf("/") + 1) 
                .replace(worldPath + "_", "")
                .replace("-full.zip", "")
                .replace("-partial.zip", "")
                + "\u001B[0m");
                entryOwners.put(entry.toString(), zipFile);
            }
        }

        else {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
                    String backupName = file.toString().replace("\\", "/");
                    filePaths.put(file.toPath().relativize(path).toString().replace("\\", "/"), path);
                    dates.put(file.toPath().relativize(path).toString().replace("\\", "/"), "\u001b[31m"
                     + backupName
                    .substring(backupName.toString().lastIndexOf("/") + 1) 
                    .replace(worldPath + "_", "")
                    .replace("-full", "")
                    .replace("-partial", "")
                    + "\u001B[0m");
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

}
