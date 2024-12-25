package computer.heather.advancedbackups.interfaces;

import computer.heather.advancedbackups.core.ABCore;
import computer.heather.advancedbackups.core.backups.BackupStatusInstance;

public interface IClientContactor {

    /*
     * This shouldn't be called anymore!
     * Would be private, but that's J9+ and this is J8.
     */
    @Deprecated
    public void backupStarting(boolean all);

    /*
     * This shouldn't be called anymore!
     * Would be private, but that's J9+ and this is J8.
     */
    @Deprecated
    public void backupProgress(int current, int max, boolean all);

    /*
     * This shouldn't be called anymore!
     * Would be private, but that's J9+ and this is J8.
     */
    @Deprecated
    public void backupComplete(boolean all);

    /*
     * This shouldn't be called anymore!
     * Would be private, but that's J9+ and this is J8.
     */
    @Deprecated
    public void backupFailed(boolean all);

    /*
     * This shouldn't be called anymore!
     * Would be private, but that's J9+ and this is J8.
     */
    @Deprecated
    public void backupCancelled(boolean all);


    public default void handle(BackupStatusInstance instance, boolean all) {
        switch (instance.getState()) {
            case STARTING: {
                this.backupStarting(all);
                break;
            }
            case STARTED: {
                this.backupProgress(instance.getProgress(), instance.getMax(), all);
                break;
            }
            case COMPLETE: {
                this.backupComplete(all);
                break;
            }
            case FAILED: {
                this.backupFailed(all);
                break;
            }
            case CANCELLED: {
                this.backupCancelled(all);
                break;
            }

            case INVALID: {
                ABCore.errorLogger.accept("Backup state of INVALID was attempted to be sent to clients!");
                ABCore.logStackTrace(new Exception());
            }

        }

    }


}