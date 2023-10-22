package co.uk.mommyheather.advancedbackups.client;

import com.mojang.realmsclient.gui.ChatFormatting;

import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class ABClientRenderer {

    
    public static boolean starting;
    public static boolean started;
    public static boolean failed;
    public static boolean finished;

    public static int progress;
    public static int max;


    private static long time;
    private static boolean timeSet = false;


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


        if (timeSet && System.currentTimeMillis() >= time + 5000) {
            starting = false;
            started = false;
            failed = false;
            finished = false;
            progress = 0;
            max = 0;
            timeSet = false;
        }
    }


    private static String round (float value) {
        return String.format("%.1f", value);
    }
    
}
