package co.uk.mommyheather.advancedbackups.core.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;

public class AVConfig {


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

        config.setEnabled(props.getProperty("config.advancedbackups.enabled"));
        config.setRequireActivity(props.getProperty("config.advancedbackups.activity"));
        config.setBackupType(props.getProperty("config.advancedbackups.type"));
        config.setPath(props.getProperty("config.advancedbackups.path"));
        config.setMaxSize(props.getProperty("config.advancedbackups.size"));
        config.setMinTimer(props.getProperty("config.advancedbackups.frequency.min"));
        config.setMaxTimer(props.getProperty("config.advancedbackups.frequency.max"));
        config.setSchedule(props.getProperty("config.advancedbackups.frequency.schedule"));
        config.setForceOnShutdown(props.getProperty("config.advancedbackups.frequency.shutdown"));
        config.setForceOnStartup(props.getProperty("config.advancedbackups.frequency.startup"));
        config.setSilent(props.getProperty("config.advancedbackups.logging.silent"));

        config.setCompressionLevel(props.getProperty("config.advancedbackups.zips.compression"));

        config.setMaxDepth(props.getProperty("config.advancedbackups.chains.length"));
        config.setCompressChains(props.getProperty("config.advancedbackups.chains.compress"));


        
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

    }

}
