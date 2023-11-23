package co.uk.mommyheather.advancedbackups.core.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Collectors;

import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.config.ConfigTypes.*;

public class ConfigManager {

    private static HashMap<String, ConfigTypes> entries = new HashMap<>();

    
    public static void register(String key, ConfigTypes configType) {
        entries.put(key, configType);
    }



    public static final BooleanValue enabled = new BooleanValue("config.advancedbackups.enabled", true);
    public static final BooleanValue save = new BooleanValue("config.advancedbackups.save", true);
    public static final BooleanValue flush  = new BooleanValue("config.advancedbackups.flush", false);
    public static final BooleanValue activity = new BooleanValue("config.advancedbackups.activity", true);
    public static final ValidatedStringValue type = new ValidatedStringValue("config.advancedbackups.type", "differential", new String[]{"zip", "differential", "incremental"});
    public static final FreeStringValue path = new FreeStringValue("config.advancedbackups.path", "./backups");
    public static final FloatValue minFrequency = new FloatValue("config.advancedbackups.frequency.min", 0.5F, 0.25F, 500F);
    public static final FloatValue maxFrequency = new FloatValue("config.advancedbackups.frequency.max", 24F, 0.5F, 500F);
    public static final BooleanValue uptime = new BooleanValue("config.advancedbackups.frequency.uptime", true);
    public static final StringArrayValue timesArray = new StringArrayValue("config.advancedbackups.frequency.schedule", new String[] {"1:00"});
    public static final BooleanValue shutdown = new BooleanValue("config.advancedbackups.frequency.shutdown", false);
    public static final BooleanValue startup = new BooleanValue("config.advancedbackups.frequency.startup", false);
    public static final LongValue delay = new LongValue("config.advancedbackups.frequency.delay", 30, 5, 1000);
    public static final BooleanValue silent = new BooleanValue("config.advancedbackups.logging.silent", false);
    public static final LongValue compression = new LongValue("config.advancedbackups.zips.compression", 4, 1, 9);
    public static final LongValue length = new LongValue("config.advancedbackups.chains.length", 50, 5, 500);
    public static final BooleanValue compressChains = new BooleanValue("config.advancedbackups.chains.compress", true);
    public static final BooleanValue smartChains = new BooleanValue("config.advancedbackups.chains.smart", true);
    public static final FloatValue chainsPercent = new FloatValue("config.advancedbackups.chains.maxpercent", 50F, 1F, 100F);
    public static final FloatValue size = new FloatValue("config.advancedbackups.purge.size", 50F, 0F, Float.MAX_VALUE);
    public static final LongValue daysToKeep = new LongValue("config.advancedbackups.purge.days", 0L, 0L, Long.MAX_VALUE);
    public static final LongValue backupsToKeep = new LongValue("config.advancedbackups.purge.count", 0L, 0L, Long.MAX_VALUE);
    public static final BooleanValue purgeIncrementals = new BooleanValue("config.advancedbackups.purge.incrementals", true);
    public static final LongValue incrementalChains = new LongValue("config.advancedbackups.purge.incrementalchains", 1, 1, Long.MAX_VALUE);





    public static void loadOrCreateConfig() {
        // Called when the config needs to be loaded, but one may not exist.
        // Creates a new config it one doesn't exist, then loads it.
        File dir = new File("./config");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File("./AdvancedBackups.properties");
        if (file.exists()) {
            migrateConfig();
        }
        file = new File(dir, "AdvancedBackups.properties");
        if (!file.exists()) {
            writeConfig();
        }
        loadConfig();
  
    }

    private static void writeConfig() {
        // Called to write to a config file.
        // Create a complete properties file in the cwd, including any existing changes
        ABCore.infoLogger.accept("Preparing to write to properties file...");
        File file = new File("./config/AdvancedBackups.properties");
        try {
            file.createNewFile();
            file.setWritable(true); 
            InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream("advancedbackups-properties.txt");

            String text = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))
                  .lines()
                  .collect(Collectors.joining("\n"));

            for (String key : entries.keySet()) {
                text = text.replace(key, key + "=" + entries.get(key).save());
            }

            FileWriter writer = new FileWriter(file);
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            // TODO : Scream to user
            e.printStackTrace();
        }
    }


    private static void loadConfig() {
        //Load the config file.
        
        Properties props = new Properties();
        File file = new File("./config/AdvancedBackups.properties");
        FileReader reader;
        try {
            reader = new FileReader(file);   
            props.load(reader);
            reader.close();
        } catch (IOException e) {
            // TODO : Scream to user
            e.printStackTrace();
            return;
        }

        ArrayList<String> missingProps = new ArrayList<>();

        for (String key : entries.keySet()) {
            if (!props.containsKey(key)) {
                missingProps.add(key);
                ABCore.warningLogger.accept("Missing key : " + key);
                continue;
            }
            ConfigValidationEnum valid = entries.get(key).validate(props.getProperty(key));
            if (valid != ConfigValidationEnum.VALID) {
                missingProps.add(key);
                ABCore.warningLogger.accept(valid.getError() + " : " + key);
                continue;

            }
            entries.get(key).load(props.getProperty(key));
        }

        if (props.containsKey("config.advancedbackups.size")) {
            ABCore.warningLogger.accept("Migrating old config value :");
            ABCore.warningLogger.accept("config.advancedbackups.size -> config.advancedbackups.purge.size");

            size.load(props.getProperty("config.advancedbackups.size"));

        }

        if (!missingProps.isEmpty()) {
            ABCore.warningLogger.accept("The following properties were missing from the loaded file :");
            for (String string : missingProps) {
                ABCore.warningLogger.accept(string);
            }
            ABCore.warningLogger.accept("Properties file will be regenerated! Existing config values will be preserved.");

            writeConfig();
        }

        
        BackupWrapper.configuredPlaytime = new ArrayList<>();
        for (String time : timesArray.get()) {
            String[] hm = time.split(":");
            long hours = Long.parseLong(hm[0]) * 3600000L; 
            long mins = Long.parseLong(hm[1]) * 60000;
            BackupWrapper.configuredPlaytime.add(hours + mins);
        }

        ABCore.backupPath = path.get() + "/" + (ABCore.worldDir.getParent().toFile().getName());
    }

    
    private static void migrateConfig() {
        //Load the config file.
        
        Properties props = new Properties();
        File file = new File("./AdvancedBackups.properties");
        FileReader reader;
        try {
            reader = new FileReader(file);   
            props.load(reader);
            reader.close();
            file.delete();
        } catch (IOException e) {
            // TODO : Scream to user
            e.printStackTrace();
            return;
        }


        for (String key : entries.keySet()) {
            if (!props.containsKey(key)) {
                continue;
            }
            ConfigValidationEnum valid = entries.get(key).validate(props.getProperty(key));
            if (valid != ConfigValidationEnum.VALID) {
                continue;

            }
            entries.get(key).load(props.getProperty(key));
        }

        ABCore.warningLogger.accept("Config in old location detected! Migrating.");
        writeConfig();
    }
}
