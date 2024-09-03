package co.uk.mommyheather.advancedbackups;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;

import co.uk.mommyheather.advancedbackups.client.ClientBridge;
import co.uk.mommyheather.advancedbackups.client.ClientContactor;
import co.uk.mommyheather.advancedbackups.core.ABCore;
import co.uk.mommyheather.advancedbackups.core.backups.BackupTimer;
import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.config.ConfigManager;
import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import co.uk.mommyheather.advancedbackups.network.PacketToastTest;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;


@Mod(modid = AdvancedBackups.MODID, name = AdvancedBackups.NAME, acceptableRemoteVersions = "*")
@EventBusSubscriber
public class AdvancedBackups
{
    public static final String MODID = "advancedbackups";
    public static final String NAME = "Advanced Backups";

    private static Logger LOGGER;
    public static Consumer<String> infoLogger;
    public static Consumer<String> warningLogger;
    public static Consumer<String> errorLogger;

    public static final ArrayList<String> players = new ArrayList<>();

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
        NetworkHandler.registerMessages();
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event)
    {
        // Do something when the server starts
        ABCore.worldName = event.getServer().worlds[0].getWorldInfo().getWorldName();

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

        ABCore.resetActivity = AdvancedBackups::resetActivity;

        ABCore.clientContactor = new ClientContactor();
        
        ABCore.modJar = Loader.instance().getIndexedModList().get("advancedbackups").getSource(); 
        //ModList.get().getModFileById("advancedbackups").getFile().getFilePath().toFile();
        ConfigManager.loadOrCreateConfig();
        LOGGER.info("Config loaded!!");



        event.registerServerCommand(new AdvancedBackupsCommand());
        
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
        ABCore.setActivity(true);
    }

    @SubscribeEvent
    public void onTickEnd(TickEvent.ServerTickEvent event) {
        if (!event.phase.equals(TickEvent.Phase.END)) return;
        BackupTimer.check();
    }

    @SubscribeEvent
    public void onConnectedToServer(PlayerLoggedInEvent event) {

        //Here's our answer.
        //Player logs in. Server sends them a packet. Client responds with a different packet to indicate their wish to subscribe to toasts.
        //It means a single packet at login is sent to clients without the mod, but it should be dropped fine.
        if (event.player instanceof EntityPlayerMP) {
            NetworkHandler.HANDLER.sendTo(new PacketToastTest(), (EntityPlayerMP) event.player);
        }
        
        
        /*ClientConfigManager.loadOrCreateConfig();
        
        //NetworkHandler.HANDLER.sendToServer(new PacketToastSubscribe(ClientConfigManager.showProgress.get()));


        //You serious? If I use the above line, the packet is just never received.
        //DONE : rework this in a nicer way. Use something other than a fucking threaded five second delay.
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {

            }
            NetworkHandler.HANDLER.sendToServer(new PacketToastSubscribe(ClientConfigManager.showProgress.get()));
        }).start();*/
    }

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        ClientBridge.onClientChat(event);
    }

    
    
    public static final String savesDisabledMessage = "\n\n\n***************************************\nSAVING DISABLED - PREPARING FOR BACKUP!\n***************************************";
    public static final String savesEnabledMessage = "\n\n\n*********************************\nSAVING ENABLED - BACKUP COMPLETE!\n*********************************";
    public static final String saveCompleteMessage = "\n\n\n*************************************\nSAVE COMPLETE - PREPARING FOR BACKUP!\n*************************************";


    public static void disableSaving() {
        for (WorldServer level : server.worlds) {
            if (level != null && !level.disableLevelSaving) {
                level.disableLevelSaving = true;
            }
        }
        warningLogger.accept(savesDisabledMessage);
    }

    public static void enableSaving() {
        for (WorldServer level : server.worlds) {
            if (level != null && level.disableLevelSaving) {
                level.disableLevelSaving = false;
            }
        }
        warningLogger.accept(savesEnabledMessage);
    }

    public static void saveOnce(boolean unused) { //no flush bool in 1.12 either
        server.saveAllWorlds(false);
        warningLogger.accept(saveCompleteMessage);
    }

    public static void resetActivity() {
        List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
        ABCore.setActivity(!players.isEmpty());
    }

}
