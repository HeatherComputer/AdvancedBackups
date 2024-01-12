package co.uk.mommyheather.advancedbackups.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class CLIIOHelpers {

    
    public static Scanner input = new Scanner(System.in);
    

    //HELPER METHODS!
    public static void info(String out, boolean line) {
        if (line) System.out.println(out);
        else System.out.print(out);
        
    }

    public static void warn(String out, boolean line) {
        if (line) System.out.println("\u001B[33m" + out + "\u001B[0m");
        else System.out.print("\u001B[33m" + out + "\u001B[0m");
        
    }

    public static void error(String out, boolean line) {
        if (line) System.out.println("\u001B[31m" + out + "\u001B[0m");
        else System.out.print("\u001B[31m" + out + "\u001B[0m");
        
    }
  
    public static void info(String out) {
        info(out, true);
    }

    public static void warn(String out) {
        warn(out, true);
    }

    public static void error(String out) {
        error(out, true);
    }


    public static String getSelectionFromList(String message, List<String> options) {
        info(message);
        int maxSpacer = String.valueOf(options.size()).length();
        int index = 1;
        for (String string : options) {
            String spacer = ":";
            int numDigits = maxSpacer - (String.valueOf(index).length() - 1);
            for (int i=0; i<numDigits; i++) {
                spacer = spacer + " ";
            }

            info(index + spacer + string);

            index++;
            
        }
        
        int userInput;
        try {
            String line = input.nextLine();
            if (line == "") {
                warn("Please enter a number!");
                return getSelectionFromList(message, options);
            }
            userInput = Integer.parseInt(line);
        } catch (InputMismatchException | NumberFormatException e) {
            warn("That was not a number. Please enter a number.");
            return getSelectionFromList(message, options);
        }
        if (userInput > options.size()) {
            warn("Please select a number in the specified range!");
            return getSelectionFromList(message, options);
        }

        return options.get(userInput - 1);
        
    }

    

    public static <T> T getFileToRestore(HashMap<String, T> files, String directory, File worldFile) {
        ArrayList<String> toDisplay = new ArrayList<>();
        HashMap<String, String> keyMap = new HashMap<>();
        int index = 1;
        for (String name : files.keySet()) {
            String name2 = name.replace("\\", "/"); // this replacement can fix some problems with wsl, and helps keep code neat.
            if (name2.startsWith(directory)) {
                name2 = name2.replaceFirst("^"+directory, "");
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
        CLIIOHelpers.info("Choose a file to restore.\n");
        for (String name : toDisplay) {
            if (index < 10) {
                CLIIOHelpers.info(index + ":  " + name);
            }
            else {
                CLIIOHelpers.info(index + ": " + name);
            }
            index++;
        }
        if (!directory.equals("")) {
            CLIIOHelpers.info(index + ": ../");
        }


        int userInput;
        try {
            String line = input.nextLine();
            if (line == "") {
                CLIIOHelpers.warn("Please enter a number!");
                return getFileToRestore(files, directory, worldFile);
            }
            userInput = Integer.parseInt(line);
        } catch (InputMismatchException | NumberFormatException e) {
            CLIIOHelpers.warn("That was not a number. Please enter a number.");
            return getFileToRestore(files, directory, worldFile);
        }

        if (userInput <= 0 || userInput > (directory.equals("") ? toDisplay.size() : toDisplay.size() + 1)) {
            CLIIOHelpers.warn("Please enter a number in the specified range!");
            return getFileToRestore(files, directory, worldFile);
        }
        else if (userInput > toDisplay.size()) {
            return null;
        }
        else if (!toDisplay.get(userInput - 1).contains("\u001B[33mdirectory\u001B[0m   ")) {
            return files.get(keyMap.get(toDisplay.get(userInput -1)));
        }

        T result = getFileToRestore(files, directory + toDisplay.get(userInput -1).replace("\u001B[33mdirectory\u001B[0m   ", "") + "/", worldFile);
        if (result != null) {
            return result;
        }
        else {
            return getFileToRestore(files, directory, worldFile);
        }
    }


    public static ArrayList<String> sortStringsAlphabeticallyWithDirectoryPriority(List<String> in) {
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

    
    public static String getBackupType(String type) {
        // Select a type of backup to restore
        CLIIOHelpers.info("Select a backup type to restore. Your server is currently set to use " + type + " backups.");
        
        List<String> options = Arrays.asList(new String[]{"zips", "differential", "incremental", "snapshot (command-made only)"});

        return getSelectionFromList("Enter a number.", options);
    }

    

    public static File getWorldFile(File serverDir) {
        CLIIOHelpers.info("Are you on a client or server?");
        CLIIOHelpers.info("1: Client\n2: Server");
        CLIIOHelpers.info("Enter a number.");

        int inputType;
        File ret = new File(serverDir.getAbsolutePath());
        
        try {
            String line = input.nextLine();
            if (line == "") {
                CLIIOHelpers.warn("Please enter a number.");
                return getWorldFile(serverDir);
            }
            inputType = Integer.parseInt(line);
        } catch (InputMismatchException | NumberFormatException e) {
            CLIIOHelpers.warn("That was not a number. Please enter a number.");
            return getWorldFile(serverDir);
        }

        if (inputType < 1 || inputType > 2) {
            CLIIOHelpers.warn("Please enter 1 or 2.");
            return getWorldFile(serverDir);
        }

        if (inputType == 1) {
            ret = new File(ret, "/saves/");
        }

        ret = new File(ret, getWorldName(ret));
        return ret;

    }


    


    public static String getWorldName(File dir) {
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
        CLIIOHelpers.info("Please select your world. Default for servers is \"world\".");
        int index = 1;
        for (String world : worlds) {
            CLIIOHelpers.info(index + ". " + world);
            index++;
        }        
        try {
            String line = input.nextLine();
            if (line == "") {
                CLIIOHelpers.warn("Please enter a number.");
                return getWorldName(dir);
            }
            worldIndex = Integer.parseInt(line);
        } catch (InputMismatchException | NumberFormatException e) {
            CLIIOHelpers.warn("That was not a number. Please enter a number.");
            return getWorldName(dir);
        }
        if (worldIndex < 1 || worldIndex > worlds.size()) {
            CLIIOHelpers.warn("Please enter a number between " + worlds.size() + ".");
            return getWorldName(dir);
        }

        //worldPath = worlds.get(worldIndex -1).replaceAll(" ", "_");
        return worlds.get(worldIndex - 1);

    }


    

    public static boolean confirmWarningMessage() {
        CLIIOHelpers.warn("");
        CLIIOHelpers.warn("");
        CLIIOHelpers.warn("WARNING! DOING THIS WHILST THE SERVER IS RUNNING CAN CAUSE SEVERE CORRUPTION, PARTIAL RESTORATION, AND OTHER ISSUES.");
        CLIIOHelpers.warn("TYPE \"continue\" IF YOU WISH TO CONTINUE...", false);

        
        String line = input.nextLine();
        if (line.equals("")) {
            return confirmWarningMessage();
        }
        return line.equals("continue");
    }

}
