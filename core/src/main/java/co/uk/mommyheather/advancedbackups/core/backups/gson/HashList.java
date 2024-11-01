package co.uk.mommyheather.advancedbackups.core.backups.gson;

import java.util.HashMap;
import java.util.Map;

public class HashList {
    private Map<String, String> hashes;

    public HashList() {
        hashes = new HashMap<>();
    }

    public Map<String, String> getHashes() {
        return hashes;
    }

    public void setHashes(Map<String, String> hashes) {
        this.hashes = hashes;
    }
}
