package co.uk.mommyheather.advancedbackups;

import net.minecraft.command.server.CommandSaveAll;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;

import org.apache.logging.log4j.Logger;

import co.uk.mommyheather.advancedbackups.client.ABClientContactor;
import co.uk.mommyheather.advancedbackups.client.ABClientRenderer;
import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.backups.BackupTimer;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;
import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

@Mod(modid = AdvancedBackups.MODID, name = AdvancedBackups.NAME, version = AdvancedBackups.VERSION, acceptableRemoteVersions = "*")
public class AdvancedBackups
{
    public static final String MODID = "advancedbackups";
    public static final String NAME = "Advanced Backups";
    public static final String VERSION = "2.0";

    private static Logger LOGGER;
    public static Consumer<String> infoLogger;
    public static Consumer<String> warningLogger;
    public static Consumer<String> errorLogger;

    public static MinecraftServer server;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();
        infoLogger = LOGGER::info;
        warningLogger = LOGGER::warn;
        errorLogger =  LOGGER::error;
    }



    public AdvancedBackups()
    {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ABClientRenderer());
        FMLCommonHandler.instance().bus().register(this);
		NetworkHandler.init();
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event)
    {
        // Do something when the server starts
        ABCore.worldName = event.getServer().worldServers[0].getWorldInfo().getWorldName();

        //Yes, this works. Yes, it feels FUCKING ILLEGAL
        if (event.getSide() == Side.SERVER) {
            ABCore.worldDir = new File(event.getServer().getFolderName(), "./").toPath();
        }
        else {
            ABCore.worldDir = new File("saves/" + event.getServer().getFolderName(), "./").toPath();
        }
        // the extra ./ is because some of the code in core calls a getParent as it was required when devving in my forge 1.18 instance, but versions earlier than 1.16 do not have this requirement

        server = event.getServer();

        ABCore.disableSaving = AdvancedBackups::disableSaving;
        ABCore.enableSaving = AdvancedBackups::enableSaving;
        ABCore.saveOnce = AdvancedBackups::saveOnce;

        ABCore.infoLogger = infoLogger;
        ABCore.warningLogger = warningLogger;
        ABCore.errorLogger = errorLogger;

        ABCore.clientContactor = new ABClientContactor();
        ABCore.resetActivity = AdvancedBackups::resetActivity;

        event.registerServerCommand(new AdvancedBackupsCommand());

        ABCore.modJar = Loader.instance().getIndexedModList().get("advancedbackups").getSource(); 

        ConfigManager.loadOrCreateConfig();
        LOGGER.info("Config loaded!!");
        
    }


    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        BackupWrapper.checkStartupBackups();
    }

    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        BackupWrapper.checkShutdownBackups();
    }

    @SubscribeEvent
    public void onPlayerConnected(PlayerEvent.PlayerLoggedInEvent event) {
        ABCore.setActivity(true);
    }

    @SubscribeEvent
    public void onTickEnd(TickEvent.ServerTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END)) return;
        BackupTimer.check();
    }
    
    
    public static final String savesDisabledMessage = "\n\n\n***************************************\nSAVING DISABLED - PREPARING FOR BACKUP!\n***************************************";
    public static final String savesEnabledMessage = "\n\n\n*********************************\nSAVING ENABLED - BACKUP COMPLETE!\n*********************************";
    public static final String saveCompleteMessage = "\n\n\n*************************************\nSAVE COMPLETE - PREPARING FOR BACKUP!\n*************************************";


    //fun fact : this boolean is named wrong in MCP mappings!
    //reference : net.minecraft.command.server.CommandSaveOff and CommandSaveOn
    //notice how off sets the boolean to true, and on sets it to false!
    public static void disableSaving() {
        MinecraftServer server = AdvancedBackups.server;
        for (WorldServer level : server.worldServers) {
            if (level != null && !level.levelSaving) {
                level.levelSaving = true;
            }
        }
        if (ConfigManager.silent.get()) return;
        warningLogger.accept(savesDisabledMessage);
    }

    public static void enableSaving() {
        MinecraftServer server = AdvancedBackups.server;
        for (WorldServer level : server.worldServers) {
            if (level != null && level.levelSaving) {
                level.levelSaving = false;
            }
        }
        if (ConfigManager.silent.get()) return;
        warningLogger.accept(savesEnabledMessage);
    }

    public static void saveOnce(boolean unused) { //flush doesn't seem to be an option in 1.7.10
        try {
            MinecraftServer server = AdvancedBackups.server;
            if (server.getConfigurationManager() != null)
            {
                server.getConfigurationManager().saveAllPlayerData();
            }
            
            int i;
            WorldServer worldserver;
            boolean flag;
            
            for (i = 0; i < server.worldServers.length; ++i)
            {
                if (server.worldServers[i] != null)
                {
                    worldserver = server.worldServers[i];
                    flag = worldserver.levelSaving;
                    worldserver.levelSaving = false;
                    worldserver.saveAllChunks(true, (IProgressUpdate)null);
                    worldserver.levelSaving = flag;
                }
            }
            

            if (ConfigManager.silent.get()) return;
            warningLogger.accept(saveCompleteMessage);
        } catch (MinecraftException e) {
            // TODO Scream at user
            errorLogger.accept("FAILED TO SAVE WORLD!");
            e.printStackTrace();
        }
    }


    public static void resetActivity() {
        ServerConfigurationManager configurationManager = server.getConfigurationManager();
        ABCore.setActivity(!configurationManager.playerEntityList.isEmpty());
    }

}
