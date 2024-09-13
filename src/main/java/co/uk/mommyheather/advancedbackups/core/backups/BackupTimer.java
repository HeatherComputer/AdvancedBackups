package co.uk.mommyheather.advancedbackups.core.backups;

import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.BackupStatusInstance.State;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;


public class BackupTimer {
    private static int loops = 0;
    private static int index = 0;
    private static long prev = 0;

    private static long lastConsole = 0;
    private static long lastClient = 0;

    private static long nextBackup = System.currentTimeMillis() + calculateNextBackupTime();

    public static void check() {
        checkLogging();
        if (ThreadedBackup.running) return;
        long currentTime = System.currentTimeMillis();
        if (ThreadedBackup.wasRunning) {
            ThreadedBackup.wasRunning = false;
            ABCore.enableSaving();
            nextBackup = calculateNextBackupTime() + currentTime;
            return;
        }
        if (currentTime < nextBackup) return;
        //make the backup

        if (BackupWrapper.checkBackups().equals(BackupCheckEnum.SUCCESS)) {
            BackupWrapper.makeSingleBackup(0, false);
        } else {
            //We can just wait here if the backup check fails.
            //It'll only be a 5m wait if a backup was meant to happen - or if the next time should be extended, say, a manual backup was made, we can go back to idle rather than running checks every tick.
            nextBackup = calculateNextBackupTime() + currentTime;
        }

    }


    private static long calculateNextBackupTime() {
        long forcedMillis = BackupWrapper.mostRecentBackupTime() + (long) (ConfigManager.maxFrequency.get() * 3600000L);
        if (forcedMillis <= System.currentTimeMillis()) {
            forcedMillis = 300000; //sets it to 5m if no backup exists or the timer is already execeeded to get the chain going
        }
        else forcedMillis -= System.currentTimeMillis();
        long ret = Long.MAX_VALUE;
        if (ConfigManager.uptime.get() && !BackupWrapper.configuredPlaytime.isEmpty()) {
            ArrayList<Long> timings = new ArrayList<Long>(BackupWrapper.configuredPlaytime);
            if (index >= timings.size()) {
                index = 0;
                loops++;
            }
            ret = (timings.get(index) + (timings.get(timings.size() - 1) * loops));
            ret -= prev;
            prev += ret;
            index++;
        } else if (!BackupWrapper.configuredPlaytime.isEmpty()) {
            long nextTime = 0;
            long currentTime = System.currentTimeMillis();
            long startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;
            ArrayList<Long> timings = new ArrayList<Long>(BackupWrapper.configuredPlaytime);
            for (long time : timings) {
                time += startTime;
                if (time >= currentTime) {
                    nextTime = time;
                    break;
                }
            }
            ret = nextTime >= currentTime ? nextTime - currentTime : 86640000 - currentTime;
        }

        return Math.min(forcedMillis, ret);

    }


    private static void checkLogging() {
        if (BackupStatusInstance.getInstanceCopy() == null) {
            return;
        }
        BackupStatusInstance instance = BackupStatusInstance.getInstanceCopy();

        boolean console = false;
        boolean clients = false;

        long time = System.currentTimeMillis();
        if (time - ConfigManager.consoleFrequency.get() >= lastConsole) {
            console = true;
            if (instance.getAge() > lastConsole && ConfigManager.console.get()) {
                lastConsole = instance.getAge();
                if (instance.getState() == State.STARTED) {
                    //No reason to care for the others, are they're all logged by default!
                    int percent = (int) ((((float) instance.getProgress()) / ((float) instance.getMax())) * 100F);
                    ABCore.infoLogger.accept("Backup in progress : " + percent + "%");
                }
            }
        }

        if (time - ConfigManager.clientFrequency.get() >= lastClient) {
            clients = true;
            if (instance.getAge() > lastClient && !ConfigManager.clients.get().equals("none")) {
                ABCore.clientContactor.handle(instance, ConfigManager.clients.get().equals("all"));
            }
        }

        if (instance.getState() == State.COMPLETE || instance.getState() == State.CANCELLED || instance.getState() == State.FAILED) {
            if (console && clients) {
                BackupStatusInstance.setInstance(null);
            }
        }

    }

}
