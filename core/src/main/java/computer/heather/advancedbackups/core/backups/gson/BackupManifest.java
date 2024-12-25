package computer.heather.advancedbackups.core.backups.gson;

public class BackupManifest {

    public static class General {
        //holds general information such as whether players have been active since the last backup
        public boolean activity;

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

        public HashList hashList;

        public HashList getHashList() {
            return hashList;
        }

        public void setHashList(HashList hashList) {
            this.hashList = hashList;
        }

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
        public long lastBackup;

        public HashList hashList;

        public HashList getHashList() {
            return hashList;
        }

        public void setHashList(HashList hashList) {
            this.hashList = hashList;
        }

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


    public static BackupManifest defaults() {
        BackupManifest manifest = new BackupManifest();

        General general = new General();
        general.setActivity(true);

        manifest.setGeneral(general);

        Differential differential = new Differential();
        differential.setChainLength(0);
        differential.setLastBackup(0);
        differential.setHashList(new HashList());

        manifest.setDifferential(differential);

        Incremental incremental = new Incremental();
        incremental.setChainLength(0);
        incremental.setLastBackup(0);
        incremental.setHashList(new HashList());

        manifest.setIncremental(incremental);

        return manifest;
    }
}
