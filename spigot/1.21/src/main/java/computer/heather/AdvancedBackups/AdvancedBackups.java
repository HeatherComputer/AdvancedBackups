package computer.heather.AdvancedBackups;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import computer.heather.advancedbackups.core.ABCore;
import computer.heather.advancedbackups.core.backups.BackupTimer;
import computer.heather.advancedbackups.core.backups.BackupWrapper;
import computer.heather.advancedbackups.core.config.ConfigManager;
import computer.heather.AdvancedBackups.network.ClientContactor;
import computer.heather.AdvancedBackups.network.PacketListener;

public class AdvancedBackups extends JavaPlugin implements Listener {
    
    private static boolean enabled = false;
    public static Server server = null;
    
    public static Consumer<String> infoLogger;
    public static Consumer<String> warningLogger;
    public static Consumer<String> errorLogger;

    //List of players that have asked to see packets
    public static final ArrayList<String> players = new ArrayList<>();
    
    @Override
    public void onEnable() {
        enabled = true;

        ABCore.disableSaving = AdvancedBackups::disableSaving;
        ABCore.enableSaving = AdvancedBackups::enableSaving;
        ABCore.saveOnce = AdvancedBackups::saveOnce;
        
        AdvancedBackups.server = getServer();
        
        ABCore.worldName = getServer().getWorlds().get(0).getName();
        ABCore.worldDir = new File(getServer().getWorlds().get(0).getWorldFolder(), "./").toPath(); //need to have this parent because of some forge compatability bullshit.. if i knew how much I'd need to do this I really would've just called getParent on the offending versions
        ABCore.modJar = getFile().getAbsoluteFile();

        infoLogger = getLogger()::info;
        warningLogger = getLogger()::warning;
        errorLogger = getLogger()::severe;

        ABCore.infoLogger = infoLogger;
        ABCore.warningLogger = warningLogger;
        ABCore.errorLogger = errorLogger;

        this.getCommand("backup").setExecutor(new AdvancedBackupsCommand());
        getServer().getPluginManager().registerEvents(this, this);

        ConfigManager.loadOrCreateConfig();

        server.getMessenger().registerIncomingPluginChannel(this, "advancedbackups:toast_subscribe", new PacketListener());
        server.getMessenger().registerOutgoingPluginChannel(this, "advancedbackups:backup_status");
        

        ABCore.clientContactor = new ClientContactor();

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
        /*Might not need to do this
        //Gets us the actual player class, not bukkit's strange reimpl
        ServerPlayer player = MinecraftServer.getServer().getPlayerList().getPlayer(event.getPlayer().getUniqueId());
        player.connection.networkmanager*/
        
        ABCore.setActivity(true);
    }

    public void onTickEnd() {
        BackupTimer.check();
    }



    public static void disableSaving() {
        for (World level : server.getWorlds()) {
            level.setAutoSave(false);
        }
    }

    public static void enableSaving() {
        for (World level : server.getWorlds()) {
            level.setAutoSave(true);
        }
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
        
    }


    public static void resetActivity() {
        ABCore.setActivity(!server.getOnlinePlayers().isEmpty());
    }

    
    
    
    
}