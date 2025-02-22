package computer.heather.advancedbackups.core.config;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

import computer.heather.advancedbackups.core.ABCore;
import computer.heather.advancedbackups.core.backups.BackupWrapper;
import computer.heather.advancedbackups.core.backups.ThreadedBackup;
import computer.heather.simpleconfig.exceptions.validation.BaseValidationException;
import computer.heather.simpleconfig.exceptions.validation.MissingOptionException;
import computer.heather.simpleconfig.exceptions.validation.MissingValueException;
import computer.heather.simpleconfig.managers.PremadePropertiesManager;
import computer.heather.simpleconfig.types.BaseConfigType;
import computer.heather.simpleconfig.types.BooleanValue;
import computer.heather.simpleconfig.types.FloatValue;
import computer.heather.simpleconfig.types.FreeStringValue;
import computer.heather.simpleconfig.types.LongValue;
import computer.heather.simpleconfig.types.StringArrayValue;
import computer.heather.simpleconfig.types.ValidatedStringValue;

public class ConfigManager {
    
    public static final BooleanValue enabled = new BooleanValue("config.advancedbackups.enabled", true);
    public static final BooleanValue save = new BooleanValue("config.advancedbackups.save", true);
    public static final BooleanValue toggleSave = new BooleanValue("config.advancedbackups.togglesave", true);
    public static final LongValue buffer = new LongValue("config.advancedbackups.buffer", 1048576, 1024, Integer.MAX_VALUE); //5mb
    public static final BooleanValue flush = new BooleanValue("config.advancedbackups.flush", false);
    public static final BooleanValue activity = new BooleanValue("config.advancedbackups.activity", true);
    public static final StringArrayValue blacklist = new StringArrayValue("config.advancedbackups.blacklist", new String[]{"session.lock", "*_old"});
    public static final ValidatedStringValue type = new ValidatedStringValue("config.advancedbackups.type", "differential", new String[]{"zip", "differential", "incremental"});
    public static final FreeStringValue path = new FreeStringValue("config.advancedbackups.path", "./backups");
    public static final FloatValue minFrequency = new FloatValue("config.advancedbackups.frequency.min", 0.25F, 0F, 500F);
    public static final FloatValue maxFrequency = new FloatValue("config.advancedbackups.frequency.max", 24F, 0.5F, 500F);
    public static final BooleanValue uptime = new BooleanValue("config.advancedbackups.frequency.uptime", true);
    public static final StringArrayValue timesArray = new StringArrayValue("config.advancedbackups.frequency.schedule", new String[]{"1:00"});
    public static final BooleanValue shutdown = new BooleanValue("config.advancedbackups.frequency.shutdown", false);
    public static final BooleanValue startup = new BooleanValue("config.advancedbackups.frequency.startup", false);
    public static final LongValue delay = new LongValue("config.advancedbackups.frequency.delay", 30, 5, 1000);
    /*
     * New logging options!
     * Clients = OPS, ALL, NONE. OPS is default, means operator permission is required. ALL is all clients with the mod. NONE disabled.
     * Client frequency - how often progress is sent to clients. Only the latest one is sent, and the client toasts persist until a complete, failed or cancelled notification is recieved.
     * Console - enable or disable console logging for backup progress. Start / finish are always logged.
     * Console frequency - how often progress is logged in console.
     */
    public static final ValidatedStringValue clients = new ValidatedStringValue("config.advancedbackups.logging.clients", "ops", new String[]{"ops", "all", "none"});
    public static final LongValue clientFrequency = new LongValue("config.advancedbackups.logging.clientfrequency", 500L, 0L, Long.MAX_VALUE);
    public static final BooleanValue console = new BooleanValue("config.advancedbackups.logging.console", true);
    public static final LongValue consoleFrequency = new LongValue("config.advancedbackups.logging.consolefrequency", 5000L, 0L, Long.MAX_VALUE);
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

    
    //Make the manager. Chaining!
    private static final PremadePropertiesManager MANAGER = new PremadePropertiesManager()
        .setConfigLocation(Paths.get("config/Advancedbackups.properties"))
        .setPremadeLocation("advancedbackups-properties.txt")
        .register(
            enabled, save, toggleSave,
            buffer, flush, activity,
            blacklist, type, path,
            minFrequency, maxFrequency, uptime,
            timesArray, shutdown, startup,
            delay, clients, clientFrequency,
            console, consoleFrequency, compression,
            length, compressChains, smartChains,
            chainsPercent, size, daysToKeep,
            backupsToKeep, purgeIncrementals, incrementalChains
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

        if (flag) ABCore.warningLogger.accept("Config file has been regenerated! Existing config values have been preserved.");




        //And now for post-load shenanigans.
        BackupWrapper.configuredPlaytime = new ArrayList<>();
        for (String time : timesArray.get()) {
            String[] hm = time.split(":");
            long hours = Long.parseLong(hm[0]) * 3600000L;
            long mins = Long.parseLong(hm[1]) * 60000;
            BackupWrapper.configuredPlaytime.add(hours + mins);
        }

        ABCore.backupPath = path.get() + "/" + (ABCore.worldDir.getParent().toFile().getName());

        ThreadedBackup.blacklist.clear();

        for (String string : blacklist.get()) {

            string = string.replace("\\", "/");
            string = string.replaceAll("[^a-zA-Z0-9*]", "\\\\$0");
            string = "^" + string.replace("*", ".*") + "$";

            ThreadedBackup.blacklist.add(Pattern.compile(string, Pattern.CASE_INSENSITIVE));
        }
    }



    private static void handleMigration(BaseConfigType<?> configType, String value) {
        //For now, do nothing! Migratory code from the past is unneeded now.
        ABCore.warningLogger.accept("Discarding unused config option: " + configType.getKey() + " with value: " + value);
    }


}
