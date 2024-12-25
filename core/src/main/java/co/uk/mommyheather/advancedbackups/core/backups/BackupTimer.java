package computer.heather.advancedbackups.core.backups;

import computer.heather.advancedbackups.core.ABCore;
import computer.heather.advancedbackups.core.backups.BackupStatusInstance.State;
import computer.heather.advancedbackups.core.config.ConfigManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;


public class BackupTimer {
    private static int loops = 0;
    private static int index = 0;
    private static long prev = 0;

    private static long lastConsole = 0;
    private static long lastClient = 0;

    private static long nextBackup = System.currentTimeMillis() + BackupTimer.calculateNextBackupTime();

    public static void check() {
        BackupTimer.checkLogging();
        if (ThreadedBackup.running) return;
        long currentTime = System.currentTimeMillis();
        if (ThreadedBackup.wasRunning) {
            ThreadedBackup.wasRunning = false;
            ABCore.enableSaving();
            BackupTimer.nextBackup = BackupTimer.calculateNextBackupTime() + currentTime;
            return;
        }
        if (currentTime < BackupTimer.nextBackup) return;
        //make the backup

        if (BackupCheckEnum.SUCCESS.equals(BackupWrapper.checkBackups())) {
            BackupWrapper.makeSingleBackup(0, false);
        } else {
            //We can just wait here if the backup check fails.
            //It'll only be a 5m wait if a backup was meant to happen - or if the next time should be extended, say, a manual backup was made, we can go back to idle rather than running checks every tick.
            BackupTimer.nextBackup = BackupTimer.calculateNextBackupTime() + currentTime;
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
            if (BackupTimer.index >= timings.size()) {
                BackupTimer.index = 0;
                BackupTimer.loops++;
            }
            ret = (timings.get(BackupTimer.index) + (timings.get(timings.size() - 1) * BackupTimer.loops));
            ret -= BackupTimer.prev;
            BackupTimer.prev += ret;
            BackupTimer.index++;
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
            //Temporary fix for making backups too frequently after the last backup of the day has been made
            //TODO tidy this all up (prolly after uberbranch)
            if (nextTime < currentTime) {
                startTime += 86400000L; //goes from start of today to start of tomorrow
                for (long time : timings) {
                    time += startTime;
                    if (time >= currentTime) {
                        nextTime = time;
                        break;
                    }
                }
            }

            ret = nextTime - currentTime;
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
        if (time - ConfigManager.consoleFrequency.get() >= BackupTimer.lastConsole) {
            console = true;
            if (instance.getAge() > BackupTimer.lastConsole && ConfigManager.console.get()) {
                BackupTimer.lastConsole = instance.getAge();
                if (instance.getState() == State.STARTED) {
                    //No reason to care for the others, are they're all logged by default!
                    int percent = (int) ((((float) instance.getProgress()) / ((float) instance.getMax())) * 100F);
                    ABCore.infoLogger.accept("Backup in progress : " + percent + "%");
                }
            }
        }

        if (time - ConfigManager.clientFrequency.get() >= BackupTimer.lastClient) {
            clients = true;
            if (instance.getAge() > BackupTimer.lastClient && !"none".equals(ConfigManager.clients.get())) {
                ABCore.clientContactor.handle(instance, "all".equals(ConfigManager.clients.get()));
            }
        }

        if (instance.getState() == State.COMPLETE || instance.getState() == State.CANCELLED || instance.getState() == State.FAILED) {
            if (console && clients) {
                BackupStatusInstance.setInstance(null);
            }
        }

    }

}