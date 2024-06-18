package computer.heather.AdvancedBackups;

import org.bukkit.plugin.java.JavaPlugin;

public class AdvancedBackups extends JavaPlugin {

    private static boolean enabled = false;

    @Override
    public void onEnable() {
        enabled = true;
        this.getCommand("backup").setExecutor(new AdvancedBackupsCommand());
    }

    @Override
    public void onDisable() {
        enabled = false;
    }

    public boolean getEnabled() {
        return enabled;
    }



}