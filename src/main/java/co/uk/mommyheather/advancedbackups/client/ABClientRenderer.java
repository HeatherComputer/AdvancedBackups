package co.uk.mommyheather.advancedbackups.client;

import com.mojang.realmsclient.gui.ChatFormatting;

import co.uk.mommyheather.advancedbackups.core.config.ClientConfigManager;
import co.uk.mommyheather.advancedbackups.network.NetworkHandler;
import co.uk.mommyheather.advancedbackups.network.PacketToastSubscribe;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class ABClientRenderer {

    
    public static boolean starting;
    public static boolean started;
    public static boolean failed;
    public static boolean finished;
    public static boolean cancelled;

    public static int progress;
    public static int max;


    private static long time;
    private static boolean timeSet = false;


    public static final ABClientRenderer INSTANCE = new ABClientRenderer();


    @SubscribeEvent
    public void onRenderEvent(RenderGameOverlayEvent.Text event) {
        if (starting) {
            event.left.add(ChatFormatting.GREEN + I18n.format("advancedbackups.backup_starting"));
        }
        else if (started) {
            float percent = (float) progress / (float) max;
            event.left.add(ChatFormatting.GREEN + I18n.format("advancedbackups.progress", round(percent * 100)));
        }
        else if (failed) {
            event.left.add(ChatFormatting.RED + I18n.format("advancedbackups.backup_failed"));
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else if (finished) {
            event.left.add(ChatFormatting.GREEN + I18n.format("advancedbackups.backup_finished"));
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else if (cancelled) {
            event.left.add(ChatFormatting.RED + I18n.format("advancedbackups.backup_cancelled"));
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }


        if (timeSet && System.currentTimeMillis() >= time + 5000) {
            starting = false;
            started = false;
            failed = false;
            finished = false;
            cancelled = false;
            progress = 0;
            max = 0;
            timeSet = false;
        }
    }


    private static String round (float value) {
        return String.format("%.1f", value);
    }


    @SubscribeEvent
    public void onServerConnected(ClientConnectedToServerEvent event) {
        //may aswell just do it here. 1.7 is so horribly documented
        ClientConfigManager.loadOrCreateConfig();


        //NetworkHandler.HANDLER.sendToServer(new PacketToastSubscribe(ClientConfigManager.showProgress.get()));

        //You serious? If I use the above line, the packet is just never received.
        //TODO : rework this in a nicer way. Use something other than a fucking threaded one second delay.
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
            NetworkHandler.HANDLER.sendToServer(new PacketToastSubscribe(ClientConfigManager.showProgress.get()));
        }).start();
    }
    
}
