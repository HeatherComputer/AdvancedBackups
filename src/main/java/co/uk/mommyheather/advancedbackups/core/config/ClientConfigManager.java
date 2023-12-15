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
import co.uk.mommyheather.advancedbackups.core.config.ConfigTypes.*;

public class ClientConfigManager {

    private static HashMap<String, ConfigTypes> entries = new HashMap<>();

    
    public static void register(String key, ConfigTypes configType) {
        entries.put(key, configType);
    }



    public static final BooleanValue showProgress = new BooleanValue("config.advancedbackups.showProgress", true, ClientConfigManager::register);

    public static final BooleanValue darkMode = new BooleanValue("config.advancedbackups.darkToasts", true, ClientConfigManager::register);

    public static final LongValue progressTextRed = new LongValue("config.advancedbackups.colours.progress.red", 82, 0, 255, ClientConfigManager::register);
    public static final LongValue progressTextGreen = new LongValue("config.advancedbackups.colours.progress.green", 255, 0, 255, ClientConfigManager::register);
    public static final LongValue progressTextBlue = new LongValue("config.advancedbackups.colours.progress.blue", 82, 0, 255, ClientConfigManager::register);

    public static final LongValue errorTextRed = new LongValue("config.advancedbackups.colours.error.red", 255, 0, 255, ClientConfigManager::register);
    public static final LongValue errorTextGreen = new LongValue("config.advancedbackups.colours.error.green", 50, 0, 255, ClientConfigManager::register);
    public static final LongValue errorTextBlue = new LongValue("config.advancedbackups.colours.error.blue", 50, 0, 255, ClientConfigManager::register);

    public static final LongValue progressBarRed = new LongValue("config.advancedbackups.colours.bar.red", 88, 0, 255, ClientConfigManager::register);
    public static final LongValue progressBarGreen = new LongValue("config.advancedbackups.colours.bar.green", 242, 0, 255, ClientConfigManager::register);
    public static final LongValue progressBarBlue = new LongValue("config.advancedbackups.colours.bar.blue", 82, 0, 255, ClientConfigManager::register);

    public static final LongValue progressBackgroundRed = new LongValue("config.advancedbackups.colours.background.red", 255, 0, 255, ClientConfigManager::register);
    public static final LongValue progressBackgroundGreen = new LongValue("config.advancedbackups.colours.background.green", 255, 0, 255, ClientConfigManager::register);
    public static final LongValue progressBackgroundBlue = new LongValue("config.advancedbackups.colours.background.blue", 255, 0, 255, ClientConfigManager::register);





    public static void loadOrCreateConfig() {
        // Called when the config needs to be loaded, but one may not exist.
        // Creates a new config it one doesn't exist, then loads it.
        File dir = new File("./config");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "AdvancedBackups-client.properties");
        if (!file.exists()) {
            writeConfig();
        }
        loadConfig();
  
    }

    private static void writeConfig() {
        // Called to write to a config file.
        // Create a complete properties file in the cwd, including any existing changes
        ABCore.infoLogger.accept("Preparing to write to client properties file...");
        File file = new File("./config/AdvancedBackups-client.properties");
        try {
            file.createNewFile();
            file.setWritable(true); 
            InputStream is = ClientConfigManager.class.getClassLoader().getResourceAsStream("advancedbackups-client-properties.txt");

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
        File file = new File("./config/AdvancedBackups-client.properties");
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

        if (!missingProps.isEmpty()) {
            ABCore.warningLogger.accept("The following properties were missing from the loaded file :");
            for (String string : missingProps) {
                ABCore.warningLogger.accept(string);
            }
            ABCore.warningLogger.accept("Client properties file will be regenerated! Existing config values will be preserved.");

            writeConfig();
        }
    }
}
