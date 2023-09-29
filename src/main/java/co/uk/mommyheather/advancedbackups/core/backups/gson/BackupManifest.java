package co.uk.mommyheather.advancedbackups.core.backups.gson;

public class BackupManifest {

    public static class General {
        //holds general information such as whether players have been active since the last backup
        public boolean activity;
        public boolean introSeen;

        public boolean isIntroSeen() {
            return introSeen;
        }

        public void setIntroSeen(boolean introSeen) {
            this.introSeen = introSeen;
        }

        public boolean isActivity() {
            return activity;
        }

        public void setActivity(boolean activity) {
            this.activity = activity;
        }

    }

    public static class Differential {
        //holds information specific to differential backups - mainly, the last full backup and the chain length
        public int chainLength;

        public long lastBackup;

        public int getChainLength() {
            return chainLength;
        }

        public void setChainLength(int chainLength) {
            this.chainLength = chainLength;
        }

        public long getLastBackup() {
            return lastBackup;
        }

        public void setLastBackup(long lastBackup) {
            this.lastBackup = lastBackup;
        }
        
    }

    public static class Incremental {
        //holds information specific to incremental backups - mainly, the last backup and the chain length
        public int chainLength;

        public int getChainLength() {
            return chainLength;
        }

        public void setChainLength(int chainLength) {
            this.chainLength = chainLength;
        }

        public long getLastBackup() {
            return lastBackup;
        }

        public void setLastBackup(long lastBackup) {
            this.lastBackup = lastBackup;
        }

        public long lastBackup;
        
    }



    public General general;
    public Differential differential;
    public Incremental incremental;

    public General getGeneral() {
        return general;
    }
    public void setGeneral(General general) {
        this.general = general;
    }
    public Differential getDifferential() {
        return differential;
    }
    public void setDifferential(Differential differential) {
        this.differential = differential;
    }
    public Incremental getIncremental() {
        return incremental;
    }
    public void setIncremental(Incremental incremental) {
        this.incremental = incremental;
    }


    public BackupManifest() {

        this.general = new General();
        this.general.setActivity(true);
        this.general.setIntroSeen(false);

        this.differential = new Differential();
        this.differential.setChainLength(0);
        this.differential.setLastBackup(0);

        this.incremental = new Incremental();
        this.incremental.setChainLength(0);
        this.incremental.setLastBackup(0);


    }
}
