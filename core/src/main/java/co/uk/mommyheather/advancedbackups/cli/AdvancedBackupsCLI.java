package co.uk.mommyheather.advancedbackups.cli;

import org.fusesource.jansi.AnsiConsole;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class AdvancedBackupsCLI {

    private static String backupLocation;
    private static File serverDir = new File(new File("").toPath().toAbsolutePath().getParent().toString());
    private static String type;
    private static ArrayList<String> fileNames = new ArrayList<>();
    private static File worldFile;
    private static String worldPath;

    public static void main(String args[]) throws IllegalBackupException {
        if (System.console() != null) {
            AnsiConsole.systemInstall(); //this gets ansi escape codes working on windows. this was a FUCKING PAIN IN MY ASS
        }

        System.out.print("\033[H\033[2J");
        System.out.flush();


        CLIIOHelpers.info("Advanced Backups - Version " + AdvancedBackupsCLI.class.getPackage().getImplementationVersion());
        CLIIOHelpers.info("Note : this cannot restore backups made prior to the 3.0 release.");
        CLIIOHelpers.info("Searching for properties...", false);


        Properties props = new Properties();
        File file = new File(serverDir, "config/AdvancedBackups.properties");
        FileReader reader;
        try {
            reader = new FileReader(file);
            props.load(reader);

            backupLocation = props.getProperty("config.advancedbackups.path");
            type = props.getProperty("config.advancedbackups.type");
        } catch (Exception e) {
            CLIIOHelpers.error("ERROR LOADING PROPERTIES!");
            CLIIOHelpers.error(getStackTrace(e));
            CLIIOHelpers.error("");
            CLIIOHelpers.error("");
            CLIIOHelpers.error("Ensure you're running this from within the mods directory, and the config file is in the parent directory!");
            // Fatal, cannot proceed
            return;
        }

        if (backupLocation == null || type == null) {
            CLIIOHelpers.error("ERROR LOADING PROPERTIES!");
            CLIIOHelpers.error("Backup location : " + backupLocation);
            CLIIOHelpers.error("Type : " + type);
            // Fatal, cannot proceed
            return;
        }

        CLIIOHelpers.info("Config loaded!");

        //What's a good way to check between absolute and relative? Not all absolute paths will start with /..
        File backupDir = new File(serverDir, backupLocation.replaceAll(Pattern.quote("." + File.separator), ""));

        if (!backupDir.exists()) {
            //Is it absolute?
            backupDir = new File(backupLocation.replaceAll(Pattern.quote("." + File.separator), ""));
            if (!backupDir.exists()) {
                CLIIOHelpers.error("Could not find backup directory!");
                CLIIOHelpers.error(backupDir.getAbsolutePath());
                CLIIOHelpers.error("Have you made any backups before?");
                //Fatal, cannot continue
                return;
            }
        }


        //check for backups from "other mods"
        boolean flag = false;
        ArrayList<File> otherBackups = new ArrayList<>();
        for (File b : backupDir.listFiles()) {
            if (b.getName().endsWith("zip")) {
                flag = true;
                otherBackups.add(b);
            }

        }

        if (flag) {
            String result = CLIIOHelpers.getSelectionFromList(
                    "Backups from another mod have been found. These can be restored if you want.\nWould you want to work with these backups?",
                    Arrays.asList(new String[]{"Use backups from AdvancedBackups", "Use backups from other mod"})
            );
            if (result == "Use backups from other mod") {
                restoreOtherModZip(backupDir);
                return;
            }
        }

        type = CLIIOHelpers.getBackupType(type);
        if ("snapshot (command-made only)".equals(type)) type = "snapshots";

        /*/
        if (backupLocation.startsWith(Pattern.quote(File.separator)) || backupLocation.indexOf(":") == 1) {
            backupDir = new File(backupLocation, File.separator + type + File.separator);
        }
        else {
            backupDir = new File(serverDir, backupLocation.replaceAll(Pattern.quote("." + File.separator), "") + File.separator + type + File.separator);
        }
*/


        boolean exportMode = false;
        int backupDateIndex;


        CLIIOHelpers.info("Do you want to export a backup, restore the entire world state at this point, or a singular file?");

        String restore = CLIIOHelpers.getSelectionFromList(
                "Enter a number.",
                Arrays.asList(new String[]{"Export backup as zip", "Restore single file", "Restore entire world"})
        );

        if ("Export backup as zip".equals(restore)) {
            exportMode = true;
        }


        worldFile = CLIIOHelpers.getWorldFile(serverDir);
        worldPath = worldFile.getName().replace(" ", "_");

        backupDir = new File(backupDir, worldFile.getName() + "/" + type);

        try {
            backupDateIndex = getBackupDate(backupDir, exportMode);
        } catch (IOException e) {
            CLIIOHelpers.error("ERROR VIEWING BACKUPS!");
            e.printStackTrace();
            return;
        }

        if (exportMode) {
            worldFile = new File(serverDir, "AdvancedBackups.temp");
            worldFile.mkdirs();
        }


        if (!CLIIOHelpers.confirmWarningMessage()) {
            CLIIOHelpers.error("ABORTED - WILL NOT PROCEED.");
            return;
        }

        CLIIOHelpers.info("Preparing...");


        switch (restore) {
            case "Restore entire world": {
                //No going back now!
                CLIIOHelpers.info("Backing up current world state...");
                CLIIOHelpers.info("Backup saved to : " + deleteEntireWorld(worldFile, false));
                switch (type) {
                    case "snapshots":
                    case "zips": {
                        restoreFullZip(backupDateIndex, worldFile);
                        return;
                    }
                    case "differential": {
                        restoreFullDifferential(backupDateIndex, worldFile);
                        return;
                    }
                    case "incremental": {
                        restoreFullIncremental(backupDateIndex, worldFile);
                        return;
                    }
                }
            }
            case "Restore single file": {
                switch (type) {
                    case "snapshots":
                    case "zips": {
                        restorePartialZip(backupDateIndex, worldFile);
                        return;
                    }
                    case "differential": {
                        restorePartialDifferential(backupDateIndex, worldFile);
                        return;
                    }
                    case "incremental": {
                        restorePartialIncremental(backupDateIndex, worldFile);
                        return;
                    }
                }
            }
            case "Export backup as zip": {

                CLIIOHelpers.info("Restoring to temporary directory...");

                switch (type) {
                    case "snapshots":
                    case "zips": {
                        restoreFullZip(backupDateIndex, worldFile);
                        break;
                    }
                    case "differential": {
                        restoreFullDifferential(backupDateIndex, worldFile);
                        break;
                    }
                    case "incremental": {
                        restoreFullIncremental(backupDateIndex, worldFile);
                        break;
                    }
                }

                CLIIOHelpers.info("Done. Preparing to write to zip...");
                CLIIOHelpers.info("Export saved to : " + deleteEntireWorld(worldFile, true));
            }

        }
    }


    private static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }


    private static int getBackupDate(File backupDir, boolean exportMode) throws IOException {
        fileNames.clear();
        int inputType;

        CLIIOHelpers.info("Select a backup to restore.");

        String[] fileNameArray = backupDir.list();
        if (fileNameArray == null || fileNameArray.length <= 0) {
            throw new IOException(String.format("Selected backup directory %s is empty, or is a file!", backupDir.getAbsolutePath()));
        }
        ArrayList<String> fileNameList = new ArrayList<String>(Arrays.asList(fileNameArray)); //i need to do this. i hate this.
        fileNameList.removeIf(name -> (
            name.endsWith("json") ||
            name.contains("incomplete") ||
            name.contains("DS_Store")
        ));

        if (fileNameList.isEmpty()) {
            throw new IOException(String.format("Selected backup directory %s is empty, or is a file!", backupDir.getAbsolutePath()));
        }

        for (String fileName : CLIIOHelpers.sortStringsAlphabeticallyWithDirectoryPriority(fileNameList)) {
            if (exportMode) {
                if (fileName.endsWith("json")) continue;
                if (fileName.contains("incomplete")) continue;
                File file = new File(backupDir, fileName);
                fileNames.add(file.getAbsolutePath());
                String out = file.getName();
                String[] outs = out.split("\\_");
                if (outs.length >= 2) {
                    out = ". " + outs[outs.length - 2] + "_" + outs[outs.length - 1];
                } else {
                    out = ". " + out;
                }
                CLIIOHelpers.info(fileNames.size() + out);

            } else {
                if (fileName.endsWith("json")) continue;
                if (fileName.contains("incomplete")) continue;
                File file = new File(backupDir, fileName);
                fileNames.add(file.getAbsolutePath());
                String out = file.getName();
                out = out.replaceAll(".zip", "");
                //out = out.replaceAll(worldPath + "_", ": ");
                out = out.replaceAll("backup_", ": ");
                out = out.replaceAll("-partial", "\u001B[33m partial\u001B[0m");
                out = out.replaceAll("-full", "\u001B[32m full\u001B[0m");
                CLIIOHelpers.info(fileNames.size() + out);

            }
        }

        try {
            String line = CLIIOHelpers.input.nextLine();
            if (line == "") {
                CLIIOHelpers.warn("Please enter a number.");
                return getBackupDate(backupDir, exportMode);
            }
            inputType = Integer.parseInt(line);
        } catch (InputMismatchException | NumberFormatException e) {
            CLIIOHelpers.warn("That was not a number. Please enter a number.");
            return getBackupDate(backupDir, exportMode);
        }

        if (inputType < 1 || inputType > fileNames.size()) {
            CLIIOHelpers.warn("Please enter a number between " + fileNames.size() + ".");
            return getBackupDate(backupDir, exportMode);
        }

        return inputType - 1;
    }


    private static void restoreFullZip(int index, File worldFile) throws IllegalBackupException {
        byte[] buffer = new byte[1024];
        //The most basic of the bunch.
        ZipEntry entry;
        try {
            FileInputStream fileInputStream = new FileInputStream(fileNames.get(index));
            ZipInputStream zip = new ZipInputStream(fileInputStream);
            while ((entry = zip.getNextEntry()) != null) {
                File outputFile = new File(worldFile, entry.getName());

                if (!outputFile.toPath().normalize().startsWith(worldFile.toPath())) {
                    zip.close();
                    CLIIOHelpers.error("Found a potentially malicious zip file - cowardly exiting, restoration may be incomplete!");
                    throw new IllegalBackupException("Zip file is likely malicious! Found an erroneus path: " + entry.getName());
                }

                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }

                CLIIOHelpers.info("Restoring " + outputFile.getName());

                FileOutputStream outputStream = new FileOutputStream(outputFile);
                int length = 0;
                while ((length = zip.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
            }
            zip.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void restoreFullDifferential(int index, File worldFile) throws IllegalBackupException {
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
        for (int i = index; i >= 0; i--) {
            String name = fileNames.get(i);
            if (name.contains("-full")) {
                CLIIOHelpers.info("Restoring last full backup...");
                File file = new File(name);
                if (file.isFile()) {
                    restoreFullZip(i, worldFile);
                } else {
                    restoreFolder(i, worldFile);
                }
                break;
            }
        }
        CLIIOHelpers.info("\n\nRestoring selected backup...");
        if (backup.isFile()) {
            restoreFullZip(index, worldFile);
        } else {
            restoreFolder(index, worldFile);
        }
    }

    private static void restoreFullIncremental(int index, File worldFile) throws IllegalBackupException {
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
        while (i >= 0) {
            String name = fileNames.get(i);
            if (name.contains("-full")) {
                CLIIOHelpers.info("Restoring last full backup...");
                File file = new File(name);
                if (file.isFile()) {
                    restoreFullZip(i, worldFile);
                } else {
                    restoreFolder(i, worldFile);
                }
                break;
            }
            i--;
        }
        //restore backups up until the selected one
        while (i < index) {
            String name = fileNames.get(i);
            CLIIOHelpers.info("Restoring chained backup...");
            File file = new File(name);
            if (file.isFile()) {
                restoreFullZip(i, worldFile);
            } else {
                restoreFolder(i, worldFile);
            }
            i++;
        }

        CLIIOHelpers.info("\n\nRestoring selected backup...");
        if (backup.isFile()) {
            restoreFullZip(index, worldFile);
        } else {
            restoreFolder(index, worldFile);
        }
    }

    private static void restorePartialZip(int index, File worldFile) throws IllegalBackupException {
        HashMap<String, Object> filePaths = new HashMap<>();
        HashMap<String, String> dates = new HashMap<>();
        HashMap<String, ZipFile> entryOwners = new HashMap<>();

        try {
            File backup = new File(fileNames.get(index));
            addBackupNamesToLists(backup, entryOwners, filePaths, dates, "\u001B[32m");

            for (String string : filePaths.keySet()) {
                File outputFile = new File(worldFile, string);
                if (!outputFile.toPath().normalize().startsWith(worldFile.toPath())) {
                    CLIIOHelpers.error("Found a potentially malicious zip file - cowardly exiting, restoration may be incomplete!");
                    throw new IllegalBackupException("Zip file is likely malicious! Found an erroneus path: " + string);
                }
            }

            ZipEntry select = ((ZipEntry) CLIIOHelpers.getFileToRestore(filePaths, "", worldFile));

            File outputFile = new File(worldFile, select.toString());
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            CLIIOHelpers.info("Restoring " + select.toString() + "...");

            byte[] buffer = new byte[1028];
            InputStream inputSteam = entryOwners.get(select.toString()).getInputStream(select);
            int length;
            while ((length = inputSteam.read(buffer, 0, buffer.length)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            // TODO handle IOException
        }
    }

    private static void restorePartialDifferential(int index, File worldFile) throws IllegalBackupException {
        //Do we need to check for past backups? if selected is a full backup, we do not.
        HashMap<String, Object> filePaths = new HashMap<>();
        HashMap<String, String> dates = new HashMap<>();
        HashMap<String, ZipFile> entryOwners = new HashMap<>();
        try {
            File backup = new File(fileNames.get(index));
            if (!backup.getName().contains("-full")) {
                //find last FULL backup
                for (int i = index; i >= 0; i--) {
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

            for (String string : filePaths.keySet()) {
                File outputFile = new File(worldFile, string);
                if (!outputFile.toPath().normalize().startsWith(worldFile.toPath())) {
                    CLIIOHelpers.error("Found a potentially malicious zip file - cowardly exiting, restoration may be incomplete!");
                    throw new IllegalBackupException("Zip file is likely malicious! Found an erroneus path: " + string);
                }
            }

            Object select = CLIIOHelpers.getFileToRestore(properMapping, "", worldFile);
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

                if (!outputFile.toPath().normalize().startsWith(worldFile.toPath())) {
                    CLIIOHelpers.error("Found a potentially malicious zip file - cowardly exiting, restoration may be incomplete!");
                    throw new IllegalBackupException("Zip file is likely malicious! Found an erroneus path: " + select.toString());
                }

                CLIIOHelpers.info("\n\nRestoring file : " + select);
                Files.copy(input, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else if (select instanceof ZipEntry) {
                ZipEntry entry = (ZipEntry) select;

                File outputFile = new File(worldFile, entry.toString());

                if (!outputFile.toPath().normalize().startsWith(worldFile.toPath())) {
                    CLIIOHelpers.error("Found a potentially malicious zip file - cowardly exiting, restoration may be incomplete!");
                    throw new IllegalBackupException("Zip file is likely malicious! Found an erroneus path: " + select.toString());
                }

                FileOutputStream outputStream = new FileOutputStream(outputFile);

                CLIIOHelpers.info("Restoring " + entry.toString() + "...");

                byte[] buffer = new byte[1028];
                InputStream inputSteam = entryOwners.get(entry.toString()).getInputStream(entry);
                int length;
                while ((length = inputSteam.read(buffer, 0, buffer.length)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
            }


        } catch (IOException e) {
            //TODO : Scream at user
            e.printStackTrace();
        }
    }

    private static void restorePartialIncremental(int index, File worldFile) throws IllegalBackupException {
        //Do we need to check for past backups? if selected is a full backup, we do not.
        HashMap<String, Object> filePaths = new HashMap<>();
        HashMap<String, String> dates = new HashMap<>();
        HashMap<String, ZipFile> entryOwners = new HashMap<>();
        try {
            File backup = new File(fileNames.get(index));
            if (!backup.getName().contains("-full")) {
                int i;
                //find last FULL backup
                for (i = index; i >= 0; i--) {
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

            for (String string : filePaths.keySet()) {
                File outputFile = new File(worldFile, string);
                if (!outputFile.toPath().normalize().startsWith(worldFile.toPath())) {
                    CLIIOHelpers.error("Found a potentially malicious zip file - cowardly exiting, restoration may be incomplete!");
                    throw new IllegalBackupException("Zip file is likely malicious! Found an erroneus path: " + string);
                }
            }

            HashMap<String, Object> properMapping = new HashMap<>();
            for (String date : dates.keySet()) {
                properMapping.put(
                    date + " " + dates.get(date),
                    filePaths.get(date)
                );
            }

            Object select = CLIIOHelpers.getFileToRestore(properMapping, "", worldFile);
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
                if (!outputFile.toPath().normalize().startsWith(worldFile.toPath())) {
                    CLIIOHelpers.error("Found a potentially malicious zip file - cowardly exiting, restoration may be incomplete!");
                    throw new IllegalBackupException("Zip file is likely malicious! Found an erroneus path: " + select.toString());
                }

                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
                CLIIOHelpers.info("\n\nRestoring file : " + select);
                Files.copy(input, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else if (select instanceof ZipEntry) {
                ZipEntry entry = (ZipEntry) select;

                File outputFile = new File(worldFile, entry.toString());

                if (!outputFile.toPath().normalize().startsWith(worldFile.toPath())) {
                    CLIIOHelpers.error("Found a potentially malicious zip file - cowardly exiting, restoration may be incomplete!");
                    throw new IllegalBackupException("Zip file is likely malicious! Found an erroneus path: " + select.toString());
                }

                FileOutputStream outputStream = new FileOutputStream(outputFile);

                CLIIOHelpers.info("Restoring " + entry.toString() + "...");

                byte[] buffer = new byte[1028];
                InputStream inputSteam = entryOwners.get(entry.toString()).getInputStream(entry);
                int length;
                while ((length = inputSteam.read(buffer, 0, buffer.length)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
            }


        } catch (IOException e) {
            //TODO : Scream at user
            e.printStackTrace();
        }
    }


    private static void restoreFolder(int index, File worldFile) throws IllegalBackupException {
        File backup = new File(fileNames.get(index));

        try {
            Files.walkFileTree(backup.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    File source = backup.toPath().relativize(file).toFile();
                    File outputFile = new File(worldFile, source.getPath());

                    if (!outputFile.toPath().normalize().startsWith(worldFile.toPath())) {
                        CLIIOHelpers.error("Found a potentially malicious zip file - cowardly exiting, restoration may be incomplete!");
                        new IllegalBackupException("Zip file is likely malicious! Found an erroneus path: " + source.getPath()).printStackTrace();
                        return FileVisitResult.TERMINATE;
                    }

                    if (!outputFile.getParentFile().exists()) {
                        outputFile.getParentFile().mkdirs();
                    }

                    CLIIOHelpers.info("Restoring " + outputFile.getName());
                    Files.copy(file, outputFile.toPath());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private static void restoreOtherModZip(File backupDir) throws IllegalBackupException {
        worldFile = serverDir;
        Path file;
        HashMap<String, File> backups = new HashMap<>();
        HashMap<String, Path> entries = new HashMap<>();

        for (File b : backupDir.getParentFile().listFiles()) {
            if (b.getName().endsWith("zip")) {
                backups.put(b.getName(), b);
            }
        }

        String backupName = CLIIOHelpers.getSelectionFromList("Select a backup to restore from.", new ArrayList<String>(backups.keySet()));

        boolean fullWorld = "Whole world" == CLIIOHelpers.getSelectionFromList(
            "Do you want to restore the whole world or a singular file?",
            Arrays.asList(new String[]{"Whole world", "Single file"})
        );

        if (!fullWorld) {
            if (!CLIIOHelpers.confirmWarningMessage()) return;

            try {
                FileSystem zipFs = FileSystems.newFileSystem(backups.get(backupName).toPath(), AdvancedBackupsCLI.class.getClassLoader());
                Path root = zipFs.getPath("");
                Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                        entries.put(file.toString(), file);
                        return FileVisitResult.CONTINUE;
                    }
                });

                file = CLIIOHelpers.getFileToRestore(entries, "", worldFile);
                CLIIOHelpers.info("Restoring " + file.toString() + "...");
                Path outputFile = new File(worldFile, file.toString()).toPath();

                if (!outputFile.normalize().startsWith(worldFile.toPath())) {
                    CLIIOHelpers.error("Found a potentially malicious zip file - cowardly exiting, restoration may be incomplete!");
                    throw new IllegalBackupException("Zip file is likely malicious! Found an erroneus path: " + file.toString());
                }
                if (!outputFile.getParent().toFile().exists()) {
                    outputFile.getParent().toFile().mkdirs();
                }
                Files.copy(file, outputFile, StandardCopyOption.REPLACE_EXISTING);
                CLIIOHelpers.info("Done.");

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
        } else {
            if (!CLIIOHelpers.confirmWarningMessage()) return;

            Path levelDatPath;
            ArrayList<Path> levelDatPathWrapper = new ArrayList<>();

            try {
                FileSystem zipFs = FileSystems.newFileSystem(backups.get(backupName).toPath(), AdvancedBackupsCLI.class.getClassLoader());
                Path root = zipFs.getPath("");
                Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                        if ("level.dat".equals(file.getFileName().toString())) levelDatPathWrapper.add(file);
                        return FileVisitResult.CONTINUE;
                    }
                });

                levelDatPath = levelDatPathWrapper.get(0);
                CLIIOHelpers.info("Making backup of existing world...");
                CLIIOHelpers.info("Backup saved to : " + deleteEntireWorld(new File(worldFile, levelDatPath.getParent().toString()), false));
                byte[] buffer = new byte[1024];
                ZipEntry entry;
                FileInputStream fileInputStream = new FileInputStream(backups.get(backupName));
                ZipInputStream zip = new ZipInputStream(fileInputStream);
                while ((entry = zip.getNextEntry()) != null) {
                    File outputFile = new File(worldFile, entry.getName());

                    if (!outputFile.toPath().normalize().startsWith(worldFile.toPath())) {
                        zip.close();
                        CLIIOHelpers.error("Found a potentially malicious zip file - cowardly exiting, restoration may be incomplete!");
                        throw new IllegalBackupException("Zip file is likely malicious! Found an erroneus path: " + entry.getName());
                    }

                    if (!outputFile.getParentFile().exists()) {
                        outputFile.getParentFile().mkdirs();
                    }

                    CLIIOHelpers.info("Restoring " + outputFile.toString() + "...");

                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    int length = 0;
                    while ((length = zip.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    outputStream.close();
                }
                zip.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        CLIIOHelpers.info("Done.");

    }


    private static String deleteEntireWorld(File worldDir, boolean exportMode) {
        String ret = backupExistingWorld(worldDir, exportMode);
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
            CLIIOHelpers.warn("Failed to delete file :");
            e.printStackTrace();
        }
        return ret;
    }

    private static String backupExistingWorld(File worldDir, boolean export) {
        File out = new File(worldDir, "../cli" + serialiseBackupName(export ? "export" : "backup") + ".zip");
        try {
            FileOutputStream outputStream = new FileOutputStream(out);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            zipOutputStream.setLevel(4);
            byte[] bytes = new byte[1024];
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
                        FileInputStream is = new FileInputStream(file.toFile());
                        while (true) {
                            int i = is.read(bytes);
                            if (i < 0) break;
                            zipOutputStream.write(bytes, 0, i);
                        }
                        is.close();
                        //byte[] bytes = Files.readAllBytes(file);
                        //zipOutputStream.write(bytes, 0, bytes.length);
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

            CLIIOHelpers.info("Done.");
        } catch (Exception e) {
            // TODO Handle Exception
        }

        return out.getName();
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
                dates.put(
                    entry.toString().replace("\\", "/"),
                    "\u001b[31m"
                        + backupName
                        .substring(backupName.toString().lastIndexOf("/") + 1)
                        //.replace(worldPath + "_", "")
                        .replace("backup_", "")
                        .replace("-full.zip", "")
                        .replace("-partial.zip", "")
                        + "\u001B[0m"
                );
                entryOwners.put(entry.toString(), zipFile);
            }
        } else {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
                    String backupName = file.toString().replace("\\", "/");
                    filePaths.put(file.toPath().relativize(path).toString().replace("\\", "/"), path);
                    dates.put(
                        file.toPath().relativize(path).toString().replace("\\", "/"),
                        "\u001b[31m"
                            + backupName
                            .substring(backupName.toString().lastIndexOf("/") + 1)
                            //.replace(worldPath + "_", "")
                            .replace("backup_", "")
                            .replace("-full", "")
                            .replace("-partial", "")
                            + "\u001B[0m"
                    );
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }


    public static String serialiseBackupName(String in) {
        Date date = new Date();
        String pattern = "yyyy-MM-dd_HH-mm-ss";
        return in + "_" + new SimpleDateFormat(pattern).format(date);
    }

}