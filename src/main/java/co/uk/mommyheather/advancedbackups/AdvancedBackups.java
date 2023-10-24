package co.uk.mommyheather.advancedbackups;

import com.mojang.logging.LogUtils;

import co.uk.mommyheather.advancedbackups.client.ClientContactor;
import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.BackupTimer;
import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;
import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

import java.util.function.Consumer;

import org.slf4j.Logger;

@Mod("advancedbackups")
public class AdvancedBackups
{

    private static final Logger LOGGER = LogUtils.getLogger();   

    public static final Consumer<String> infoLogger = LOGGER::info;
    public static final Consumer<String> warningLogger = LOGGER::warn;
    public static final Consumer<String> errorLogger = LOGGER::error;


    public AdvancedBackups()
    {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        NetworkHandler.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        ABCore.worldName = event.getServer().getWorldData().getLevelName();
        ABCore.worldDir = event.getServer().getWorldPath(LevelResource.ROOT);


        ABCore.disableSaving = AdvancedBackups::disableSaving;
        ABCore.enableSaving = AdvancedBackups::enableSaving;
        ABCore.saveOnce = AdvancedBackups::saveOnce;

        ABCore.infoLogger = infoLogger;
        ABCore.warningLogger = warningLogger;
        ABCore.errorLogger = errorLogger;

        ABCore.clientContactor = new ClientContactor();
        
        ABCore.modJar = ModList.get().getModFileById("advancedbackups").getFile().getFilePath().toFile();

        
        ConfigManager.loadOrCreateConfig();
        LOGGER.info("Config loaded!!");
        
        
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        BackupWrapper.checkStartupBackups();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        BackupWrapper.checkShutdownBackups();
    }

    
    @SubscribeEvent
    public void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
        ABCore.activity = true;
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event){
        AdvancedBackupsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onTickEnd(TickEvent.ServerTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END)) return;
        BackupTimer.check();
    }

        
    public static final String savesDisabledMessage = """


***************************************
SAVING DISABLED - PREPARING FOR BACKUP!
***************************************
""";
    public static final String savesEnabledMessage = """


*********************************
SAVING ENABLED - BACKUP COMPLETE!
*********************************
""";
    public static final String saveCompleteMessage = """


*************************************
SAVE COMPLETE - PREPARING FOR BACKUP!
*************************************
""";


    public static void disableSaving() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (ServerLevel level : server.getAllLevels()) {
            if (level != null && !level.noSave) {
                level.noSave = true;
            }
        }
        if (ConfigManager.silent.get()) return;
        warningLogger.accept(savesDisabledMessage);
    }

    public static void enableSaving() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (ServerLevel level : server.getAllLevels()) {
            if (level != null && level.noSave) {
                level.noSave = false;
            }
        }
        if (ConfigManager.silent.get()) return;
        warningLogger.accept(savesEnabledMessage);
    }

    public static void saveOnce(boolean flush) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.saveEverything(true, flush, true);
        if (ConfigManager.silent.get()) return;
        warningLogger.accept(saveCompleteMessage);
    }

}
