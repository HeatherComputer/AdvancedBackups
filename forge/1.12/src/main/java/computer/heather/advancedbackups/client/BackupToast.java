package computer.heather.advancedbackups.client;

import computer.heather.advancedbackups.core.config.ClientConfigManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class BackupToast implements IToast {
    
    
    public static boolean starting;
    public static boolean started;
    public static boolean failed;
    public static boolean finished;
    public static boolean cancelled;

    public static int progress;
    public static int max;

    public static boolean exists = false;

    private static long time;
    private static boolean timeSet = false;

    public static final ItemStack stack = new ItemStack(Items.PAPER);

    private int textColour = 0;
    private static String title = "You shouldn't see this!";
    
    private int progressBarColor = ColourHelper.colour(255, ClientConfigManager.progressBarRed.get(), ClientConfigManager.progressBarGreen.get(), ClientConfigManager.progressBarBlue.get());


    @Override
    public Visibility draw(GuiToast toastGui, long delta) {
        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0,ClientConfigManager.darkMode.get() ? 0 : 32, 160, 32);
        RenderHelper.enableGUIStandardItemLighting();
        toastGui.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, 8, 8);

        float percent = finished ? 100 : (float) progress / (float) max;
        
        Gui.drawRect(4, 28, 156, 29, ColourHelper.colour
            (255, ClientConfigManager.progressBackgroundRed.get(), ClientConfigManager.progressBackgroundGreen.get(), ClientConfigManager.progressBackgroundBlue.get()));
        float f = Math.min(156, (
            156 * percent
        ));

        if (!exists) {   
            toastGui.getMinecraft().fontRenderer.drawString(title, 25, 12, textColour);
            if (title.equals(I18n.format("advancedbackups.backup_finished"))) Gui.drawRect(4, 28, 156, 29, progressBarColor);
            return Visibility.HIDE;
        }



        
        if (starting) {
            title = I18n.format("advancedbackups.backup_starting");
            textColour = ColourHelper.colour(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
        }
        else if (started) {
            title = I18n.format("advancedbackups.progress", round(percent * 100));
            textColour = ColourHelper.colour(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
        }
        else if (failed) {
            title = I18n.format("advancedbackups.backup_failed");
            textColour = ColourHelper.colour(255, (int) ClientConfigManager.errorTextRed.get(), (int) ClientConfigManager.errorTextGreen.get(), (int) ClientConfigManager.errorTextBlue.get());
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else if (cancelled) {
            title = I18n.format("advancedbackups.backup_cancelled");
            textColour = ColourHelper.colour(255, (int) ClientConfigManager.errorTextRed.get(), (int) ClientConfigManager.errorTextGreen.get(), (int) ClientConfigManager.errorTextBlue.get());
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else if (finished) {
            title = I18n.format("advancedbackups.backup_finished");
            textColour = ColourHelper.colour(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else title = "You shouldn't see this!";

        toastGui.getMinecraft().fontRenderer.drawString(title, 25, 12, textColour);

        if (timeSet && System.currentTimeMillis() >= time + 5000) {
            starting = false;
            started = false;
            failed = false;
            finished = false;
            cancelled = false;
            progress = 0;
            max = 0;
            timeSet = false;
            exists = false;
            return Visibility.HIDE;
        }


        if (progress > 0 || finished) Gui.drawRect(4, 28, (int) f, 29, progressBarColor);

        
        return Visibility.SHOW;
        
    }

    
    private static String round (float value) {
        return String.format("%.1f", value);
    }
    
}
