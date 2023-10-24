package co.uk.mommyheather.advancedbackups.client;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;

public class BackupToast implements Toast {
        
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
    public Visibility draw(MatrixStack matrix, ToastManager manager, long startTime) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        DrawableHelper.drawTexture(matrix, 0, 0, 0, 0, this.getWidth(), this.getHeight());
        manager.getClient().getItemRenderer().renderInGui(matrix, stack, 8, 8);
        
        float percent = finished ? 100 : (float) progress / (float) max;
        
        DrawableHelper.fill(matrix, 3, 28, 156, 29, -1);

        float f = Math.min(156, (
            156 * percent
        ));

        if (!exists) {   
            manager.getClient().textRenderer.draw(matrix, Formatting.GREEN + I18n.translate("advancedbackups.backup_finished"), 25, 11, -11534256);
            DrawableHelper.fill(matrix, 3, 28, 156, 29, -10948014);
            return Visibility.HIDE;
        }

        String title = "You shouldn't see this!";

        
        if (starting) {
            title = Formatting.GREEN + I18n.translate("advancedbackups.backup_starting");
        }
        else if (started) {
            title = Formatting.GREEN + I18n.translate("advancedbackups.progress", round(percent * 100));
        }
        else if (failed) {
            title = Formatting.RED + I18n.translate("advancedbackups.backup_failed");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else if (finished) {
            title = Formatting.GREEN + I18n.translate("advancedbackups.backup_finished");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }

        manager.getClient().textRenderer.draw(matrix, title, 25, 11, -11534256);

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

        DrawableHelper.fill(matrix, 3, 28, Math.max(3, (int) f), 29, -10948014);
        
        return Visibility.SHOW;
    }
    
    
    private static String round (float value) {
        return String.format("%.1f", value);
    }

}
