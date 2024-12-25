package computer.heather.advancedbackups;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import computer.heather.advancedbackups.client.ClientContactor;
import computer.heather.advancedbackups.client.ClientWrapper;
import computer.heather.advancedbackups.core.ABCore;
import computer.heather.advancedbackups.core.backups.BackupTimer;
import computer.heather.advancedbackups.core.backups.BackupWrapper;
import computer.heather.advancedbackups.core.config.ConfigManager;
import computer.heather.advancedbackups.network.NetworkHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@Mod("advancedbackups")
public class AdvancedBackups
{

    private static final Logger LOGGER = LogUtils.getLogger();   

    public static final Consumer<String> infoLogger = LOGGER::info;
    public static final Consumer<String> warningLogger = LOGGER::warn;
    public static final Consumer<String> errorLogger = LOGGER::error;

    public static final ArrayList<String> players = new ArrayList<>();


    public AdvancedBackups(IEventBus modEventBus)
    {
        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(NetworkHandler::onRegisterPayloadHandler);
        modEventBus.addListener(this::clientSetup);

        ABCore.infoLogger = infoLogger;
        ABCore.warningLogger = warningLogger;
        ABCore.errorLogger = errorLogger;
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

        ABCore.resetActivity = AdvancedBackups::resetActivity;

        ABCore.clientContactor = new ClientContactor();
        ABCore.modJar = ModList.get().getModFileById("advancedbackups").getFile().getFilePath().toFile();

        
        ConfigManager.loadOrCreateConfig();
        LOGGER.info("Config loaded!!");
        
    }

    public void clientSetup(FMLClientSetupEvent e) {
        ClientWrapper.init(e);
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
        ABCore.setActivity(true);
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
        warningLogger.accept(savesDisabledMessage);
    }

    public static void enableSaving() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        for (ServerLevel level : server.getAllLevels()) {
            if (level != null && level.noSave) {
                level.noSave = false;
            }
        }
        warningLogger.accept(savesEnabledMessage);
    }

    public static void saveOnce(boolean flush) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.saveEverything(true, flush, true);
        warningLogger.accept(saveCompleteMessage);
    }


    public static void resetActivity() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        ABCore.setActivity(!players.isEmpty());
    }

}
