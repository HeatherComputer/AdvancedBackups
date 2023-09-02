package co.uk.mommyheather.advancedbackups.core;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

public class ABCore {
    
    public static String worldName;
    public static Path worldDir;
    public static Boolean activity = false;

    public static File modJar;
    //version specific stuff will locate the mod's jar
    
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

    

    public static String serialiseBackupName(String in) {
        Date date = new Date();
        String pattern = "yyyy-MM-dd_hh-mm-ss";
        
        return in + "_" + new SimpleDateFormat(pattern).format(date);
    }
}
