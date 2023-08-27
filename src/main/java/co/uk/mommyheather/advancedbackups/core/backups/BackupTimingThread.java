package co.uk.mommyheather.advancedbackups.core.backups;

import java.time.LocalDateTime;
import java.util.ArrayList;

import co.uk.mommyheather.advancedbackups.core.config.AVConfig;

public class BackupTimingThread extends Thread {
    private int loops = 0;
    private int index = 0;
    private long prev = 0;

    public BackupTimingThread() {
        setName("AB Backup Timing Daemon");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                long time = calculateNextBackupTime();
                sleep(time);
                BackupWrapper.checkAndMakeBackups();
            } catch (InterruptedException e) {
                // TODO Scream at user
                e.printStackTrace();
                break;
            }
        }
    }


    private long calculateNextBackupTime() {
        long forcedMillis = BackupWrapper.mostRecentBackupTime() + (long) (AVConfig.config.getMaxTimer() * 3600000L);
        if (forcedMillis == AVConfig.config.getMaxTimer() * 3600000L || forcedMillis <= System.currentTimeMillis()) forcedMillis = 300000; //sets it to 5m if no backup exists or the timer is already execeeded to get the chain going
        else forcedMillis -= System.currentTimeMillis();
        long ret = Long.MAX_VALUE;
        if (AVConfig.config.getUptimeSchedule() && !BackupWrapper.configuredPlaytime.isEmpty()) {
            ArrayList<Long> timings = new ArrayList<Long>(BackupWrapper.configuredPlaytime);
            if (index >= timings.size()) {
                index = 0;
                loops++;
            }
            ret = (timings.get(index) + (timings.get(timings.size() - 1) * loops));
            ret -= prev; 
            prev += ret;
            index++;
        }

        else if (!BackupWrapper.configuredPlaytime.isEmpty()) {
            long nextTime = 0;
            long currentTime = System.currentTimeMillis();
            ArrayList<Long> timings = new ArrayList<Long>(BackupWrapper.configuredPlaytime);
            for (long time : timings) {
                if (time >= currentTime) {
                    nextTime = time;
                    break;
                }
            }
            ret = nextTime >= currentTime ? nextTime - currentTime : 86640000 - currentTime;
        }

        return Math.min(forcedMillis, ret);



        


    }

    
}
