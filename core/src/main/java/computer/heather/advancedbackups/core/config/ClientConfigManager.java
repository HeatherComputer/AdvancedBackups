package computer.heather.advancedbackups.core.config;

import computer.heather.advancedbackups.core.ABCore;
import computer.heather.simpleconfig.exceptions.validation.BaseValidationException;
import computer.heather.simpleconfig.exceptions.validation.MissingOptionException;
import computer.heather.simpleconfig.exceptions.validation.MissingValueException;
import computer.heather.simpleconfig.managers.PremadePropertiesManager;
import computer.heather.simpleconfig.types.BaseConfigType;
import computer.heather.simpleconfig.types.BooleanValue;
import computer.heather.simpleconfig.types.LongValue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ClientConfigManager {


    public static final BooleanValue showProgress = new BooleanValue("config.advancedbackups.showProgress", true);

    public static final BooleanValue darkMode = new BooleanValue("config.advancedbackups.darkToasts", true);

    public static final LongValue progressTextRed = new LongValue("config.advancedbackups.colours.progress.red", 82, 0, 255);
    public static final LongValue progressTextGreen = new LongValue("config.advancedbackups.colours.progress.green", 255, 0, 255);
    public static final LongValue progressTextBlue = new LongValue("config.advancedbackups.colours.progress.blue", 82, 0, 255);

    public static final LongValue errorTextRed = new LongValue("config.advancedbackups.colours.error.red", 255, 0, 255);
    public static final LongValue errorTextGreen = new LongValue("config.advancedbackups.colours.error.green", 50, 0, 255);
    public static final LongValue errorTextBlue = new LongValue("config.advancedbackups.colours.error.blue", 50, 0, 255);

    public static final LongValue progressBarRed = new LongValue("config.advancedbackups.colours.bar.red", 88, 0, 255);
    public static final LongValue progressBarGreen = new LongValue("config.advancedbackups.colours.bar.green", 242, 0, 255);
    public static final LongValue progressBarBlue = new LongValue("config.advancedbackups.colours.bar.blue", 82, 0, 255);

    public static final LongValue progressBackgroundRed = new LongValue("config.advancedbackups.colours.background.red", 255, 0, 255);
    public static final LongValue progressBackgroundGreen = new LongValue("config.advancedbackups.colours.background.green", 255, 0, 255);
    public static final LongValue progressBackgroundBlue = new LongValue("config.advancedbackups.colours.background.blue", 255, 0, 255);

    
    //Make the manager. Chaining!
    private static final PremadePropertiesManager MANAGER = new PremadePropertiesManager()
        .setConfigLocation(Paths.get("config/Advancedbackups-client.properties"))
        .setPremadeLocation("advancedbackups-client-properties.txt")
        .register(
            showProgress, darkMode,
            progressTextRed, progressTextGreen, progressTextBlue,
            errorTextRed, errorTextGreen, errorTextBlue,
            progressBarRed, progressBarGreen, progressBarBlue,
            progressBackgroundRed, progressBackgroundGreen, progressBackgroundBlue
        );


    public static void loadOrCreateConfig() throws IOException {
        ArrayList<String> missingProperties = new ArrayList<>();
        ArrayList<String> erroringProperties = new ArrayList<>();
        try {
            MANAGER.loadOrCreate((configType, value, exception) -> {
                //MissingOptionException is used for config types that don't exist in code. This means we can migrate stuff. 
                if (exception instanceof MissingOptionException) {
                    handleMigration(configType, value);
                }
                else if (exception instanceof MissingValueException) {
                    missingProperties.add(configType.getKey());
                }
                else {
                    erroringProperties.add(configType.getKey() + exception);
                }
            });
        } catch (IOException e) {            
            ABCore.errorLogger.accept("Failed to load config! Cannot proceed due to IO error.");
            throw e;
        } catch (BaseValidationException e) {
            // Nothing to do here. This catch will never trigger.
        }


        //We use this flat just so we can ensure the final error message is only logged once.
        boolean flag = false;

        //Now, we have loaded and created with our error handler. But let's make sure we log missing options properly!
        if (!missingProperties.isEmpty()) {
            flag = true;
            ABCore.warningLogger.accept("The following options were missing from the loaded file :");
            for (String string : missingProperties) {
                ABCore.warningLogger.accept(string);
            }
        }

        //And now the same for erroring options.
        if (!erroringProperties.isEmpty()) {
            ABCore.warningLogger.accept("The following options failed to validate :");
            for (String string : erroringProperties) {
                ABCore.warningLogger.accept(string);
            }
        }

        if (flag) ABCore.warningLogger.accept("Client config file has been regenerated! Existing config values have been preserved.");
    }



    private static void handleMigration(BaseConfigType<?> configType, String value) {
        //For now, do nothing! Migratory code from the past is unneeded now.
        ABCore.warningLogger.accept("Discarding unused config option: " + configType.getKey() + " with value: " + value);
    }
}