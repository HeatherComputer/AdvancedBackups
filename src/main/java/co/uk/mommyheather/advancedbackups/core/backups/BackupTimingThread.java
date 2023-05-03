package co.uk.mommyheather.advancedbackups.core.backups;

import java.util.ArrayList;

import co.uk.mommyheather.advancedbackups.core.config.AVConfig;

public class BackupTimingThread extends Thread {
    private int loops = 0;
    private int index = 0;
    private long startTime = 0;
    @Override
    public void run() {
        startTime = System.currentTimeMillis();
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

        ArrayList<Long> timings = new ArrayList<Long>(BackupWrapper.configuredPlaytime);
        if (index >= timings.size()) {
            index = 0;
            loops++;
        }
        

        long nextScheduled = timings.size() >= 1 ? (timings.get(index) + (timings.get(timings.size()-1) * loops)) : Long.MAX_VALUE;
        index++;
        return Math.min(forcedMillis, nextScheduled);
    }

    
}
