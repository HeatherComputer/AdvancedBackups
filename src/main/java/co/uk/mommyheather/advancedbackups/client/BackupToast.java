package co.uk.mommyheather.advancedbackups.client;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class BackupToast implements IToast{
    
    
    public static boolean starting;
    public static boolean started;
    public static boolean failed;
    public static boolean finished;

    public static int progress;
    public static int max;

    public static boolean exists = false;

    private static long time;
    private static boolean timeSet = false;

    public static final ItemStack stack = new ItemStack(Items.PAPER);


    @Override
    public Visibility draw(GuiToast toastGui, long delta) {
        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0, 0, 160, 32);
        RenderHelper.enableGUIStandardItemLighting();
        toastGui.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, 8, 8);

        if (!exists) {   
            toastGui.getMinecraft().fontRenderer.drawString(ChatFormatting.GREEN + I18n.format("advancedbackups.backup_finished"), 25, 12, -11534256);
            return Visibility.HIDE;
        }

        String title = "You shouldn't see this!";

        
        if (starting) {
            title = ChatFormatting.GREEN + I18n.format("advancedbackups.backup_starting");
        }
        else if (started) {
            float percent = (float) progress / (float) max;
            title = ChatFormatting.GREEN + I18n.format("advancedbackups.progress", round(percent * 100));
        }
        else if (failed) {
            title = ChatFormatting.RED + I18n.format("advancedbackups.backup_failed");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else if (finished) {
            title = ChatFormatting.GREEN + I18n.format("advancedbackups.backup_finished");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }

        toastGui.getMinecraft().fontRenderer.drawString(title, 25, 12, -11534256);

        if (timeSet && System.currentTimeMillis() >= time + 5000) {
            starting = false;
            started = false;
            failed = false;
            finished = false;
            progress = 0;
            max = 0;
            timeSet = false;
            exists = false;
            return Visibility.HIDE;
        }
        
        return Visibility.SHOW;
        
    }

    
    private static String round (float value) {
        return String.format("%.1f", value);
    }
    
}
