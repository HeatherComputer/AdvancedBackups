package computer.heather.advancedbackups.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import computer.heather.advancedbackups.core.config.ClientConfigManager;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ColorHelper;

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

    private int textColour;
    private String title = "You shouldn't see this!";


    @Override
    public Visibility render(MatrixStack matrix, ToastGui toastGui, long delta) {
        toastGui.getMinecraft().getTextureManager().bind(TEXTURE);
        RenderSystem.color3f(1.0F, 1.0F, 1.0F);
        toastGui.blit(matrix, 0, 0, 0, ClientConfigManager.darkMode.get() ? 0 : this.height(), this.width(), this.height());
        toastGui.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(stack, 8, 8);

        float percent = finished ? 100 : (float) progress / (float) max;
        
        AbstractGui.fill(matrix, 4, 28, 156, 29, ColorHelper.PackedColor.color
            (255, (int) ClientConfigManager.progressBackgroundRed.get(), (int) ClientConfigManager.progressBackgroundGreen.get(), (int) ClientConfigManager.progressBackgroundBlue.get()));
        float f = Math.min(156, (
            156 * percent
        ));

        if (!exists) {
            if (title.equals(I18n.get("advancedbackups.backup_finished"))){
                textColour = ColorHelper.PackedColor.color(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
                toastGui.getMinecraft().font.draw(matrix, I18n.get(title), 25, 11, textColour);
                AbstractGui.fill(matrix, 3, 28, 156, 29, ColorHelper.PackedColor.color
                    (255, (int) ClientConfigManager.progressBarRed.get(), (int) ClientConfigManager.progressBarGreen.get(), (int) ClientConfigManager.progressBarBlue.get()));
            }
            else {
                textColour = ColorHelper.PackedColor.color(255, (int) ClientConfigManager.errorTextRed.get(), (int) ClientConfigManager.errorTextGreen.get(), (int) ClientConfigManager.errorTextBlue.get());
                toastGui.getMinecraft().font.draw(matrix, I18n.get(title), 25, 11, textColour);
            }
            return Visibility.HIDE;
        }


        
        if (starting) {
            textColour = ColorHelper.PackedColor.color(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
            title = I18n.get("advancedbackups.backup_starting");
        }
        else if (started) {
            textColour = ColorHelper.PackedColor.color(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
            title = I18n.get("advancedbackups.progress", round(percent * 100));
        }
        else if (failed) {
            textColour = ColorHelper.PackedColor.color(255, (int) ClientConfigManager.errorTextRed.get(), (int) ClientConfigManager.errorTextGreen.get(), (int) ClientConfigManager.errorTextBlue.get());
            title = I18n.get("advancedbackups.backup_failed");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else if (finished) {
            textColour = ColorHelper.PackedColor.color(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
            title = I18n.get("advancedbackups.backup_finished");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else if (cancelled) {            
            textColour = ColorHelper.PackedColor.color(255, (int) ClientConfigManager.errorTextRed.get(), (int) ClientConfigManager.errorTextGreen.get(), (int) ClientConfigManager.errorTextBlue.get());
            title = I18n.get("advancedbackups.backup_cancelled");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }

        }

        else {
            title = "You shouldn't see this!";
        }

        toastGui.getMinecraft().font.draw(matrix, title, 25, 11, textColour);

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

        AbstractGui.fill(matrix, 4, 28, Math.max(3, (int) f), 29, ColorHelper.PackedColor.color
            (255, (int) ClientConfigManager.progressBarRed.get(), (int) ClientConfigManager.progressBarGreen.get(), (int) ClientConfigManager.progressBarBlue.get()));
        
        return Visibility.SHOW;
        
    }

    
    private static String round (float value) {
        return String.format("%.1f", value);
    }

}
