package co.uk.mommyheather.advancedbackups;

import com.mojang.logging.LogUtils;

import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.config.AVConfig;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        AVConfig.loadOrCreateConfig();
        LOGGER.info("Config loaded!!");
        PlatformMethodWrapper.worldName = event.getServer().getWorldData().getLevelName();
        PlatformMethodWrapper.worldDir = event.getServer().getWorldPath(LevelResource.ROOT);
        
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
        PlatformMethodWrapper.activity = true;
    }

}
