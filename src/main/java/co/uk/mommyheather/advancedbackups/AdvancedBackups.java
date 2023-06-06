package co.uk.mommyheather.advancedbackups;


import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.config.AVConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

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
        AVConfig.loadOrCreateConfig();
        LOGGER.info("Config loaded!!");
        PlatformMethodWrapper.worldName = event.getServer().getWorldData().getLevelName();
        PlatformMethodWrapper.worldDir = event.getServer().storageSource.getWorldDir();
        
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
        PlatformMethodWrapper.activity = true;
    }

}
