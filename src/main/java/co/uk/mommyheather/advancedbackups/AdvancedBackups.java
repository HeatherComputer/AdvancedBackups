package co.uk.mommyheather.advancedbackups;

import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.apache.logging.log4j.Logger;

import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.config.AVConfig;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.function.Consumer;


@Mod(modid = AdvancedBackups.MODID, name = AdvancedBackups.NAME, version = AdvancedBackups.VERSION, acceptableRemoteVersions = "*")
@EventBusSubscriber
public class AdvancedBackups
{
    public static final String MODID = "advancedbackups";
    public static final String NAME = "Advanced Backups";
    public static final String VERSION = "0.3";

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
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event)
    {
        // Do something when the server starts
        AVConfig.loadOrCreateConfig();
        LOGGER.info("Config loaded!!");
        PlatformMethodWrapper.worldName = event.getServer().worlds[0].getWorldInfo().getWorldName();
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
    public void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event) {
        PlatformMethodWrapper.activity = true;
    }

}
