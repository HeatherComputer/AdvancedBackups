package co.uk.mommyheather.advancedbackups.interfaces;

import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.BackupStatusInstance;

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
                backupStarting(all);
                break;
            }
            case STARTED: {
                backupProgress(instance.getProgress(), instance.getMax(), all);
                break;
            }
            case COMPLETE: {
                backupComplete(all);
                break;
            }
            case FAILED: {
                backupFailed(all);
                break;
            }
            case CANCELLED: {
                backupCancelled(all);
                break;
            }

            case INVALID: {
                ABCore.errorLogger.accept("Backup state of INVALID was attempted to be sent to clients!");
                ABCore.logStackTrace(new Exception());
            }

        }

    }


}
