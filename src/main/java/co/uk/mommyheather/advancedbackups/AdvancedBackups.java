package co.uk.mommyheather.advancedbackups;


import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.BackupTimer;
import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("advancedbackups")
public class AdvancedBackups
{

    private static final Logger LOGGER = LogManager.getLogger();

    public static final Consumer<String> infoLogger = LOGGER::info;
    public static final Consumer<String> warningLogger = LOGGER::warn;
    public static final Consumer<String> errorLogger = LOGGER::error;


    public AdvancedBackups()
    {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event)
    {
        // Do something when the server starts
        ABCore.worldName = event.getServer().getWorldData().getLevelName();
        ABCore.worldDir = event.getServer().getWorldPath(FolderName.ROOT);


        ABCore.disableSaving = AdvancedBackups::disableSaving;
        ABCore.enableSaving = AdvancedBackups::enableSaving;
        ABCore.saveOnce = AdvancedBackups::saveOnce;

        ABCore.infoLogger = infoLogger;
        ABCore.warningLogger = warningLogger;
        ABCore.errorLogger = errorLogger;

        ABCore.modJar = ModList.get().getModFileById("advancedbackups").getFile().getFilePath().toFile();

        
        ConfigManager.loadOrCreateConfig();
        LOGGER.info("Config loaded!!");
        
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        BackupWrapper.checkStartupBackups();
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        BackupWrapper.checkShutdownBackups();
    }

    @SubscribeEvent
    public void onPlayerConneccted(PlayerLoggedInEvent event) {
        ABCore.activity = true;
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event){
        AdvancedBackupsCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPostTick(TickEvent.ServerTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END)) return;
        BackupTimer.check();
    }


        
    public static final String savesDisabledMessage = "\n\n\n***************************************\nSAVING DISABLED - PREPARING FOR BACKUP!\n***************************************";
    public static final String savesEnabledMessage = "\n\n\n*********************************\nSAVING ENABLED - BACKUP COMPLETE!\n*********************************";
    public static final String saveCompleteMessage = "\n\n\n*************************************\nSAVE COMPLETE - PREPARING FOR BACKUP!\n*************************************";


    public static void disableSaving() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (ServerWorld level : server.getAllLevels()) {
            if (level != null && !level.noSave) {
                level.noSave = true;
            }
        }
        if (ConfigManager.silent.get()) return;
        warningLogger.accept(savesDisabledMessage);
    }

    public static void enableSaving() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (ServerWorld level : server.getAllLevels()) {
            if (level != null && level.noSave) {
                level.noSave = false;
            }
        }
        if (ConfigManager.silent.get()) return;
        warningLogger.accept(savesEnabledMessage);
    }

    public static void saveOnce(boolean flush) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.saveAllChunks(true, flush, true);
        if (ConfigManager.silent.get()) return;
        warningLogger.accept(saveCompleteMessage);
    }

}
