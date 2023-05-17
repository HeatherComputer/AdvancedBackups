package co.uk.mommyheather.advancedbackups.core.backups.gson;

import java.util.ArrayList;
import java.util.List;

public class DifferentialManifest {
    private List<Long> complete;
    private List<Long> partial;
    private int chain;
    private long lastFull;
    private long lastPartial;


    public List<Long> getComplete() {
        return complete;
    }


    public void setComplete(List<Long> complete) {
        this.complete = complete;
    }


    public List<Long> getPartial() {
        return partial;
    }


    public void setPartial(List<Long> partial) {
        this.partial = partial;
    }


    public int getChain() {
        return chain;
    }


    public void setChain(int chain) {
        this.chain = chain;
    }


    public long getLastFull() {
        return lastFull;
    }


    public void setLastFull(long lastFull) {
        this.lastFull = lastFull;
    }


    public long getLastPartial() {
        return Math.max(lastFull, lastPartial);
    }


    public void setLastPartial(long lastPartial) {
        this.lastPartial = lastPartial;
    }


    public static DifferentialManifest defaultValues() {
        DifferentialManifest manifest = new DifferentialManifest();
        manifest.complete = new ArrayList<>();
        manifest.partial = new ArrayList<>();
        manifest.chain = 0;
        manifest.lastFull = 0;
        manifest.lastPartial = 0;
        return manifest;
    }
}
