package co.uk.mommyheather.advancedbackups;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;


import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.config.AVConfig;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.function.Consumer;


@Mod(modid = AdvancedBackups.MODID, name = AdvancedBackups.NAME, version = AdvancedBackups.VERSION, acceptableRemoteVersions = "*")
public class AdvancedBackups
{
    public static final String MODID = "advancedbackups";
    public static final String NAME = "Advanced Backups";
    public static final String VERSION = "0.3";

    private static Logger LOGGER = LogManager.getLogger("AdvancedBackups");
    public static Consumer<String> infoLogger;
    public static Consumer<String> warningLogger;
    public static Consumer<String> errorLogger;

    public static MinecraftServer server;


    @EventHandler
    @SuppressWarnings("unused")
    public void init(FMLInitializationEvent ev) {
        infoLogger = LOGGER::info;
        warningLogger = LOGGER::warn;
        errorLogger = LOGGER::error;
        AVConfig.loadOrCreateConfig(); //doing this in init is better
    }



    public AdvancedBackups()
    {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event)
    {
        // Do something when the server starts
        AVConfig.loadConfig(); //and a reload upon server start
        LOGGER.info("Config loaded!!");
        PlatformMethodWrapper.worldName = event.getServer().worldServers[0].getWorldInfo().getWorldName();
        if (event.getSide() == Side.SERVER) {
            PlatformMethodWrapper.worldDir = new File(event.getServer().getFolderName()).toPath();
        }
        else {
            PlatformMethodWrapper.worldDir = new File("saves/" + event.getServer().getFolderName()).toPath();
        }

        server = event.getServer();
        
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
        PlatformMethodWrapper.activity = true;
    }

}
