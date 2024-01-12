package co.uk.mommyheather.advancedbackups.interfaces;

public interface IClientContactor {
    public void backupStarting();
    public void backupProgress(int current, int max);
    public void backupComplete();
    public void backupFailed();
    public void backupCancelled();
}
