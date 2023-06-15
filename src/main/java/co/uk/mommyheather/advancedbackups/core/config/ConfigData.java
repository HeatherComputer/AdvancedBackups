package co.uk.mommyheather.advancedbackups.core.config;

public class ConfigData {
    
    private Boolean enabled;
    //Allows outright disabling of backups.
    //OPTIONS = "TRUE", "FALSE"

    private Boolean save;
    //Whether to save before a backup.
    //OPTIONS = "TRUE", "FALSE"

    private Boolean requireActivity;
    //Whether to require player activity between backups.
    //OPTIONS = "TRUE", "FALSE"

    private String backupType;
    //The type of backup to make. Each has a separate file structure, but only the currently selected backup type is scanned for.
    //OPTIONS = "ZIP", "DIFFERENTIAL", "INCREMENTAL"

    private long maxSize;
    //Maximum size of backups. With zips, deletes the absolute oldest file if size is exceeded after making a backup, and repeats until below max size.
    //RANGE = 5GB - 9999GB

    private float minTimer;
    //Ensure this amount of time is waited between backups, in hours.
    //RANGE = 0.5 - 500

    private float maxTimer;
    //Triggers a backup if none has already happened within this time. Can be combined with an uptime-based schedule.
    //RANGE = 0.5 - 500

    private boolean uptimeSchedule;
    //Whether the schedule is based off of server uptime (true) or world-time (false).

    private String schedule;
    //A comma seperated schedule. About it.

    private String path;
    //The path for backups. Defaults to ./backups. Can be absolute or relative.
    //ANY STRING IS TESTED, WILL CREATE DIRECTORIES IF MISSING

    private Boolean silent;
    //Whether to supress all information. Does not affect debug.log.
    // TRUE OR FALSE

    private Boolean forceOnShutdown;
    //Whether to build a backup when the server shuts down.
    // TRUE OR FALSE

    private Boolean forceOnStartup;
    //Whether to build a backup when the server starts up.
    // TRUE OR FALSE

    //Startup backup delay, in seconds.
    private long startupDelay;


    //BELOW ONLY APPLIES TO ZIP FILES! (affects export command)
    private int compressionLevel;
    //The compression level to be passed to zip streams.
    // RANGE = 1 - 9


    //BELOW ONLY APPLIES TO INCREMENTAL AND DIFFERENTIAL BACKUPS!
    private int maxDepth;
    //The maximum "depth" of partial backups to create before creating a full backup. Higher numbers are reccomended if using incremental. Lower numbers increase storage usage but reduce restoration times.
    // RANGE = 5 - 500

    private boolean compressChains;
    //Whether to compress chains. Useful for size reduction and manual restoration.
    // TRUE OR FALSE

    private boolean smartChains;
    // Smart chain resetting.
    // TRUE OR FALSE

    private int maxSizePercent;
    // Resets chain length if what gets backed up is over the defined % size.
    // 1-100 range.

    private boolean purgeIncrementals;
    // Whether to purge incremental backups if over the limit.
    // TRUE OR FALSE

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = Boolean.parseBoolean(enabled);
    }

    public Boolean getSave() {
        return save;
    }

    public void setSave(String save) {
        this.save = Boolean.parseBoolean(save);
    }

    public Boolean getRequireActivity() {
        return requireActivity;
    }

    public void setRequireActivity(String requireActivity) {
        this.requireActivity = Boolean.parseBoolean(requireActivity);
    }

    public String getBackupType() {
        return backupType;
    }

    public void setBackupType(String backupType) {
        this.backupType = backupType;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(String maxSize) {
        this.maxSize = Long.parseLong(maxSize);
    }

    public float getMinTimer() {
        return minTimer;
    }

    public void setMinTimer(String minTimer) {
        this.minTimer = Float.parseFloat(minTimer);
    }


    public float getMaxTimer() {
        return maxTimer;
    }

    public void setUptimeSchedule(String uptimeSchedule) {
        this.uptimeSchedule = Boolean.parseBoolean(uptimeSchedule);
    }

    public boolean getUptimeSchedule() {
        return uptimeSchedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setMaxTimer(String maxTimer) {
        this.maxTimer = Float.parseFloat(maxTimer);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getSilent() {
        return silent;
    }

    public void setSilent(String silent) {
        this.silent = Boolean.parseBoolean(silent);
    }

    public Boolean getForceOnShutdown() {
        return forceOnShutdown;
    }

    public void setForceOnShutdown(String forceOnShutdown) {
        this.forceOnShutdown = Boolean.parseBoolean(forceOnShutdown);
    }

    public Boolean getForceOnStartup() {
        return forceOnStartup;
    }

    public void setForceOnStartup(String forceOnStartup) {
        this.forceOnStartup = Boolean.parseBoolean(forceOnStartup);
    }

    public void setStartupDelay(String delay) {
        this.startupDelay = Long.parseLong(delay);
    }

    public long getStartupDelay() {
        return startupDelay;
    }

    public int getCompressionLevel() {
        return compressionLevel;
    }

    public void setCompressionLevel(String compressionLevel) {
        this.compressionLevel = Integer.parseInt(compressionLevel);
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(String maxDepth) {
        this.maxDepth = Integer.parseInt(maxDepth);
    }

    public boolean getCompressChains() {
        return compressChains;
    }

    public void setCompressChains(String compressChains) {
        this.compressChains = Boolean.parseBoolean(compressChains);
    }

    public boolean getSmartChains() {
        return smartChains;
    }

    public void setSmartChains(String smartChains) {
        this.smartChains = Boolean.parseBoolean(smartChains);
    }

    
    public boolean getPurgeIncrementals() {
        return purgeIncrementals;
    }

    public void setPurgeIncrementals(String purgeIncrementals) {
        this.purgeIncrementals = Boolean.parseBoolean(purgeIncrementals);
    }
    
    public int getMaxSizePercent() {
        return maxSizePercent;
    }

    public void setMaxSizePercent(String maxSizePercent) {
        this.maxSizePercent = Integer.parseInt(maxSizePercent);
    }

   


    //default config output - can be ignored
    public static final String plainConfig = String.join("\n",

"#Enable or disable automatic backups.",
"#Options : true, false   #Default : true,",
"config.advancedbackups.enabled=%s",
"",
"#Whether to save before making a backup.",
"#Options : true, false    #Default : false",
"config.advancedbackups.save=%s",
"",
"#Whether to require player activity between backups.",
"#Options : true, false    #Default : false",
"config.advancedbackups.activity=%s",
"",
"#The type of backups to use.",
"#Options : zip, differential, incremental    #Default : differential",
"config.advancedbackups.type=%s",
"",
"#The absolute or relative path to the backup location.",
"#Options : any file path. Default : ./backups",
"config.advancedbackups.path=%s",
"",
"#The maximum size to keep, in GB. Keep relatively high for zips, tighter space requirements should instead use differential or incremental backups.",
"#Range : 5 - 9999   #Default : 50",
"config.advancedbackups.size=%s",
"",
"#Minimum time between backups, in hours. This can prevent a shutdown backup from triggering immediately after a scheduled backup or similar situations.",
"#Range : 0.5 - 500    #Default : 0.5",
"config.advancedbackups.frequency.min=%s",
"",
"#Triggers a backup if none has already happened within this time. Can be combined with an uptime-based schedule.",
"#Range : 0.5 - 500    #Default : 24",
"config.advancedbackups.frequency.max=%s",
"",
"#Whether the schedule below uses uptime (true) or real-world time (false).",
"#Default : true",
"config.advancedbackups.frequency.uptime=%s",
"",
"#When using server uptime:",
"    #A looping comma-separated backup schedule, based off of server uptime, hours:minutes. Examples:",
"    #4:00 - Makes a backup every four hours.",
"    #4:00,7:00 - Makes a backup after four hours, then three, then four, and so on.",
"    #1:00 - Makes a backup every hour.",
"    #4:00,8:00,12:00,16:00,17:00,18:00,19:00,20:00,21:00,24:00 - Makes a backup following a strict schedule.",
"",
"#When using real-world time:",
"    #A strict schedule, using hours:minutes to follow real-world time. Examples:",
"    #4:00 - Makes a backup at 4am each day.",
"    #4:00,8:00,12:00,16:00,17:00,18:00,19:00,20:00,21:00,24:00 - Makes a backup at specific times of day.",
"",
"#Default : 12:00",
"config.advancedbackups.frequency.schedule=%s",
"",
"#Whether to force a backup on server shutdown. Respects min frequency.",
"#Options : true, false    #Default : false",
"config.advancedbackups.frequency.shutdown=%s",
"",
"#Whether to force a backup on server startup. Respects min frequency.",
"#Options : true, false    #Default : false",
"config.advancedbackups.frequency.startup=%s",
"",
"#Delay to use after startup, in seconds. Is always at least 5 seconds.",
"#Range : 5-9999999999",
"config.advancedbackups.frequency.delay=%s",
"",
"#Whether to disable console and chat logging. Does not affect debug.log, does not affect error messages.",
"#Options : true, false    #Default : false",
"config.advancedbackups.logging.silent=%s",
"",
"",
"",
"#--------------------------------------------------------------------------------------------------------------------",
"##The following options only affect zip files, whether that's for zip backups, export commands or some other option.",
"#--------------------------------------------------------------------------------------------------------------------",
"",
"#The compression level to use for zip files. Higher numbers space usage, but decrease performance.",
"#Range : 1-9    #Default : 4",
"config.advancedbackups.zips.compression=%s",
"",
"",
"",
"#--------------------------------------------------------------------------------------------------------------------",
"##The following options only affect differential and incremental backups.",
"#--------------------------------------------------------------------------------------------------------------------",
"",
"#The maximum 'chain' length to keep.",
"#Range : 5-500    #Default : 50",
"config.advancedbackups.chains.length=%s",
"",
"#Whether to compress 'chains'. This compresses the base backup and all sequential backups. Reduces space usage, but decreases performance.",
"#Options : true, false    #Default : true",
"config.advancedbackups.chains.compress=%s",
"",
"#Whether to enable \"smart\" reset for chains - if every file is being backed up, mark the backup as complete and reset chain length regardless of intended backup type.",
"#Options : true, false    #Default : true",
"config.advancedbackups.chains.smart=%s",
"",
"#What % of a full backup is allowed to be contained in a partial before forcing it into a full backup. Useful for reducing partial backup size.",
"config.advancedbackups.chains.maxpercent=%s",
"",
"#Whether to delete incremental backup chains if max size is exceeded. If not, incremental backups do not respect the max size config and never delete.",
"#Options : true, false    #Default : false",
"config.advancedbackups.purge.incrementals=%s"


    );

    public static final String defaults = String.format(plainConfig, 
    "true", "false", "false",
    "differential", "./backups", "50",
    "0.5", "24", "true",
    "12:00", "false", "false",
    "5", "false", "4",
    "50", "true", "true",
    "75","false");

}
