package co.uk.mommyheather.advancedbackups.core;

import java.nio.file.Path;
import java.util.function.Consumer;

public class ABCore {
    
    public static String worldName;
    public static Path worldDir;
    public static Boolean activity = false;
    
    public static Consumer<String> infoLogger;
    public static Consumer<String> warningLogger;
    public static Consumer<String> errorLogger;

    public static Runnable disableSaving;
    public static Runnable enableSaving;
    public static Runnable saveOnce;

    
    public static void disableSaving() {
        disableSaving.run();
    }

    public static void enableSaving() {
        enableSaving.run();
    }

    public static void saveOnce() {
        saveOnce.run();
    }

    public static void setActivity() {
        activity = true;
    }
}
