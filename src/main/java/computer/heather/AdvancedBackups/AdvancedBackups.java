package computer.heather.AdvancedBackups;

import java.util.function.Consumer;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.BackupTimer;
import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;

public class AdvancedBackups extends JavaPlugin implements Listener {
    
    private static boolean enabled = false;
    private static Server server = null;
    
    public static Consumer<String> infoLogger;
    public static Consumer<String> warningLogger;
    public static Consumer<String> errorLogger;
    
    @Override
    public void onEnable() {
        enabled = true;

        ABCore.disableSaving = AdvancedBackups::disableSaving;
        ABCore.enableSaving = AdvancedBackups::enableSaving;
        ABCore.saveOnce = AdvancedBackups::saveOnce;
        
        AdvancedBackups.server = getServer();
        
        ABCore.worldName = getServer().getWorlds().get(0).getName();
        ABCore.worldDir = getServer().getWorlds().get(0).getWorldFolder().toPath();
        ABCore.modJar = getFile();
        

        infoLogger = getLogger()::info;
        warningLogger = getLogger()::warning;
        errorLogger = getLogger()::severe;

        ABCore.infoLogger = infoLogger;
        ABCore.warningLogger = warningLogger;
        ABCore.errorLogger = errorLogger;

        this.getCommand("backup").setExecutor(new AdvancedBackupsCommand());
        getServer().getPluginManager().registerEvents(this, this);

        ConfigManager.loadOrCreateConfig();
        

        //NYI
        //ABCore.clientContactor = new ABClientContactor();
        ABCore.resetActivity = AdvancedBackups::resetActivity;

        new BukkitRunnable() {
            @Override
            public void run() {
                AdvancedBackups.this.onTickEnd();
            }
        }.runTaskTimer(this, 0, 0);
    }
    
    @Override
    public void onDisable() {
        enabled = false;
        BackupWrapper.checkShutdownBackups();
    }
    
    public boolean getEnabled() {
        return enabled;
    }
    
    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        BackupWrapper.checkStartupBackups();
    }

    
    /* handled in onDisable
    @EventHandler
    public void onServerStop( event) {
        BackupWrapper.checkShutdownBackups();;
    }*/
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ABCore.setActivity(true);
    }

    public void onTickEnd() {
        BackupTimer.check();
    }


    public static final String savesDisabledMessage = "\n\n\n***************************************\nSAVING DISABLED - PREPARING FOR BACKUP!\n***************************************";
    public static final String savesEnabledMessage = "\n\n\n*********************************\nSAVING ENABLED - BACKUP COMPLETE!\n*********************************";
    public static final String saveCompleteMessage = "\n\n\n*************************************\nSAVE COMPLETE - PREPARING FOR BACKUP!\n*************************************";

    public static void disableSaving() {
        for (World level : server.getWorlds()) {
            level.setAutoSave(false);
        }
        warningLogger.accept(savesDisabledMessage);
    }

    public static void enableSaving() {
        for (World level : server.getWorlds()) {
            level.setAutoSave(true);
        }
        warningLogger.accept(savesEnabledMessage);
    }

    public static void saveOnce(boolean unused) {

        server.savePlayers();
        boolean flag;
        
        for (World level : server.getWorlds()) {
            flag = level.isAutoSave();
            level.setAutoSave(false);
            level.save();
            level.setAutoSave(flag);
        }
        
        warningLogger.accept(saveCompleteMessage);
    }


    public static void resetActivity() {
        ABCore.setActivity(!server.getOnlinePlayers().isEmpty());
    }

    
    
    
    
}