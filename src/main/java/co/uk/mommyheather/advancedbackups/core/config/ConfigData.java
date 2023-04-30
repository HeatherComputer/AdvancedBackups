package co.uk.mommyheather.advancedbackups.core.config;

public class ConfigData {
    
    private Boolean enabled;
    //Allows outright disabling of backups.
    //OPTIONS = "TRUE", "FALSE"

    private Boolean requireActivity;
    //Whether to require player activity between backups.
    //OPTIONS = "TRUE", "FALSE"

    private String backupType;
    //The type of backup to make. Each has a separate file structure, but only the currently selected backup type is scanned for.
    //OPTIONS = "ZIP", "DIFFERENTIAL", "INCREMENTAL"

    private int maxSize;
    //Maximum size of backups. With zips, deletes the absolute oldest file if size is exceeded after making a backup, and repeats until below max size.
    //RANGE = 5GB - 9999GB

    private float maxTimer;
    //How often to make backups, in hours.
    //RANGE = 0.5 - 500

    private float minTimer;
    //Ensure this amount of time is waited between backups, in hours.
    //RANGE = 0.5 - 500

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


    //BELOW ONLY APPLIES TO ZIP FILES! (affects export command)
    private int compressionLevel;
    //The compression level to be passed to zip streams.
    // RANGE = 1 - 9


    //BELOW ONLY APPLIES TO INCREMENTAL AND DIFFERENTIAL BACKUPS!
    private int maxDepth;
    //The maximum "depth" of partial backups to create before creating a full backup. Higher numbers are reccomended if using incremental. Lower numbers increase storage usage but reduce restoration times.
    // RANGE = 5 - 500

    private boolean compressChains;
    //Whether to compress chains. Not suggested for incremental backups, but is useful with differential for size reduction and manual restoration.
    // TRUE OR FALSE

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = Boolean.parseBoolean(enabled);
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

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(String maxSize) {
        this.maxSize = Integer.parseInt(maxSize);
    }

    public float getMaxTimer() {
        return maxTimer;
    }

    public void setMaxTimer(String maxTimer) {
        this.maxTimer = Float.parseFloat(maxTimer);
    }

    public float getMinTimer() {
        return minTimer;
    }

    public void setMinTimer(String minTimer) {
        this.minTimer = Float.parseFloat(minTimer);
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
   


    //default config output - can be ignored
    public static final String defaults = """

#Enable or disable automatic backups.
#Options : true, false   #Default : true
config.advancedbackups.enabled=true

#Whether to require player activity between backups.
#Options : true, false    #Default : false
config.advancedbackups.activity=false

#The type of backups to use.
#Options : zip, differential, incremental    #Default : zip
config.advancedbackups.type=zip

#The absolute or relative path to the backup location.
#Options : any file path. Default : ./backups
config.advancedbackups.path=./backups

#The maximum size to keep, in GB. Keep relatively high for zips, tighter space requirements should instead use differential or incremental backups.
#Range : 5 - 9999   #Default : 50
config.advancedbackups.size=50

#Minimum time between backups, in hours. This can prevent a shutdown backup from triggering immediately after a scheduled backup or similar situations.
#Range : 0.5 - 500    #Default : 0.5
config.advancedbackups.frequency.min=0.5

#Maximum time between backups, in hours. Follows system time and will make a backup whenever this counter is hit.
#Range : 0.5 - 500    #Default : 0.5
config.advancedbackups.frequency.max=0.5

#Whether to force a backup on server shutdown. Respects min frequency.
#Options : true, false    #Default : false
config.advancedbackups.frequency.shutdown=false

#Whether to force a backup on server startup. Respects min frequency.
#Options : true, false    #Default : false
config.advancedbackups.frequency.startup=false

#Whether to disable console and chat logging. Does not affect debug.log, does not affect error messages.
#Options : true, false    #Default : false
config.advancedbackups.logging.silent=false



#--------------------------------------------------------------------------------------------------------------------
##The following options only affect zip files, whether that's for zip backups, export commands or some other option.
#--------------------------------------------------------------------------------------------------------------------

#The compression level to use for zip files. Higher numbers space usage, but decrease performance.
#Range : 1-9    #Default : 4
config.advancedbackups.zips.compression=4



#--------------------------------------------------------------------------------------------------------------------
##The following options only affect differential and incremental backups.
#--------------------------------------------------------------------------------------------------------------------

#The maximum 'chain' length to keep.
#Range : 5-500    #Default : 50
config.advancedbackups.chains.length=50

#Whether to compress 'chains'. This compresses the base backup and all sequential backups. Reduces space usage, but decreases performance.
#Options : true, false    #Default : false
config.advancedbackups.chains.compress=false


    """;

}
