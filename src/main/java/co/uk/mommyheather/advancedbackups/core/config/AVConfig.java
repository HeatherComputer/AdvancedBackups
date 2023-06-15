package co.uk.mommyheather.advancedbackups.core.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import co.uk.mommyheather.advancedbackups.PlatformMethodWrapper;
import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;

public class AVConfig {

    private static final String[] supportedProps = {
        "config.advancedbackups.enabled",
        "config.advancedbackups.save",
        "config.advancedbackups.activity",
        "config.advancedbackups.type",
        "config.advancedbackups.path",
        "config.advancedbackups.size",
        "config.advancedbackups.frequency.min",
        "config.advancedbackups.frequency.max",
        "config.advancedbackups.frequency.uptime",
        "config.advancedbackups.frequency.schedule",
        "config.advancedbackups.frequency.shutdown",
        "config.advancedbackups.frequency.startup",
        "config.advancedbackups.frequency.delay",
        "config.advancedbackups.logging.silent",

        "config.advancedbackups.zips.compression",

        "config.advancedbackups.chains.length",
        "config.advancedbackups.chains.compress",
        "config.advancedbackups.chains.smart"

    };

    public static ConfigData config;

    public static void loadOrCreateConfig() {
        // Called when the config needs to be loaded, but one may not exist.
        // Creates a new config it one doesn't exist, then loads it.
        File file = new File("./AdvancedBackups.properties");
        if (!file.exists()) {
            initConfig();
        }
        loadConfig();
    }

    public static void initConfig() {
        // Called when no config file is present.
        // Create a complete properties file in the cwd
        File file = new File("./AdvancedBackups.properties");
        try {
            if (!file.exists()) {
                file.createNewFile();
                file.setWritable(true); 
            }
            FileWriter writer = new FileWriter(file);
            writer.write(ConfigData.defaults);
            writer.close();
        } catch (IOException e) {
            // TODO : Scream to user
            e.printStackTrace();
        }

        
    }

    public static void loadConfig() {
        // Called when the config needs to be loaded.
        // Populates all values of ConfigData.class

        Properties props = new Properties();
        File file = new File("./AdvancedBackups.properties");
        FileReader reader;
        try {
            reader = new FileReader(file);   
            props.load(reader);
        } catch (IOException e) {
            // TODO : Scream to user
            e.printStackTrace();
        }

        config = new ConfigData();

        config.setEnabled(props.getProperty("config.advancedbackups.enabled", "true"));
        config.setSave(props.getProperty("config.advancedbackups.save", "false"));
        config.setRequireActivity(props.getProperty("config.advancedbackups.activity", "false"));
        config.setBackupType(props.getProperty("config.advancedbackups.type", "differential"));
        config.setPath(props.getProperty("config.advancedbackups.path", "./backups"));
        config.setMaxSize(props.getProperty("config.advancedbackups.size", "50"));
        config.setMinTimer(props.getProperty("config.advancedbackups.frequency.min", "0.5"));
        config.setMaxTimer(props.getProperty("config.advancedbackups.frequency.max", "24"));
        config.setUptimeSchedule(props.getProperty("config.advancedbackups.frequency.uptime", "true"));
        config.setSchedule(props.getProperty("config.advancedbackups.frequency.schedule", "12:00"));
        config.setForceOnShutdown(props.getProperty("config.advancedbackups.frequency.shutdown", "false"));
        config.setForceOnStartup(props.getProperty("config.advancedbackups.frequency.startup", "false"));
        config.setStartupDelay(props.getProperty("config.advancedbackups.frequency.delay", "5"));
        config.setSilent(props.getProperty("config.advancedbackups.logging.silent", "false"));

        config.setCompressionLevel(props.getProperty("config.advancedbackups.zips.compression", "5"));

        config.setMaxDepth(props.getProperty("config.advancedbackups.chains.length", "50"));
        config.setCompressChains(props.getProperty("config.advancedbackups.chains.compress", "true"));
        config.setSmartChains(props.getProperty("config.advancedbackups.chains.smart", "true"));


        
        String timingsString = config.getSchedule();
        if (timingsString.length() != 0) {
            BackupWrapper.configuredPlaytime = new ArrayList<>();
            for (String time : timingsString.split(",")) {
                String[] hm = time.split(":");
                long hours = Long.parseLong(hm[0]) * 3600000L; 
                long mins = Long.parseLong(hm[1]) * 60000;
                BackupWrapper.configuredPlaytime.add(hours + mins);
            }
        }

        boolean flag = false;

        for (String prop : supportedProps) {
            if (!props.containsKey(prop)) {
                flag = true;
                break;
            }
        }

        if (flag) {
            PlatformMethodWrapper.warningLogger.accept("Broken, incomplete or misising config found! Generating new file whilst preserving any existing config values...");

            File newPropsFile = new File("./AdvancedBackups.properties");
            try {
                if (!newPropsFile.exists()) {
                    newPropsFile.createNewFile();
                }
                FileWriter writer = new FileWriter(newPropsFile);
                writer.write(String.format(ConfigData.plainConfig, config.getEnabled(),
                config.getSave(), config.getRequireActivity(), config.getBackupType(),
                config.getPath(), config.getMaxSize(), config.getMinTimer(),
                config.getMaxTimer(), config.getUptimeSchedule(), config.getSchedule(),
                config.getForceOnShutdown(), config.getStartupDelay(), config.getStartupDelay(),
                config.getSilent(), config.getCompressionLevel(), config.getMaxDepth(),
                config.getCompressChains(), config.getSmartChains()
                    ));
                writer.close();
            } catch (IOException e) {
                // TODO : Scream to user
                e.printStackTrace();
            
            }
            loadConfig();
        }

    }

}
