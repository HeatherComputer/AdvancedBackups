package co.uk.mommyheather.advancedbackups.core.backups;

public enum BackupCheckEnum {
    SUCCESS,
    DISABLED,
    NOACTIVITY,
    TOORECENT;

    public boolean success() {
        switch (this) {
            case SUCCESS:
                return true;
            default:
                return false;
        }
    }


    public String getCheckMessage() {
        switch (this) {
            case SUCCESS:
                return "Checks successful!";
            case DISABLED:
                return "Backups are disabled!";
            case NOACTIVITY:
                return "Player activity is required, but none have been active!";
            case TOORECENT:
                return "The time since the last backup is less than the minimum time specified in config!";
            default:
                return "You should not see this message, report a bug!";
        }
    }
}
