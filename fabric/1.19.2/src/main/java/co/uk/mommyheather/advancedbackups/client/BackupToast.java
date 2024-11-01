package co.uk.mommyheather.advancedbackups.client;

import com.mojang.blaze3d.systems.RenderSystem;

import co.uk.mommyheather.advancedbackups.core.config.ClientConfigManager;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class BackupToast implements Toast {
        
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

    public static String title = "You shouldn't see this!";
    public static int textColour = 0;

    @Override
    public Visibility draw(MatrixStack matrix, ToastManager manager, long startTime) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        manager.drawTexture(matrix, 0, 0, 0, ClientConfigManager.darkMode.get() ? 0 : this.getHeight(), this.getWidth(), this.getHeight());
        manager.getClient().getItemRenderer().renderGuiItemIcon(stack, 8, 8);
        
        float percent = finished ? 100 : (float) progress / (float) max;
        
        DrawableHelper.fill(matrix, 4, 28, 156, 29, ColourHelper.colour
            (255, (int) ClientConfigManager.progressBackgroundRed.get(), (int) ClientConfigManager.progressBackgroundGreen.get(), (int) ClientConfigManager.progressBackgroundBlue.get()));

        float f = Math.min(156, (
            156 * percent
        ));

        if (!exists) {
            if (title.equals(I18n.translate("advancedbackups.backup_finished"))){
                textColour = ColourHelper.colour(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
                manager.getClient().textRenderer.draw(matrix, I18n.translate(title), 25, 11, textColour);
                DrawableHelper.fill(matrix, 4, 28, 156, 29, ColourHelper.colour
                    (255, (int) ClientConfigManager.progressBarRed.get(), (int) ClientConfigManager.progressBarGreen.get(), (int) ClientConfigManager.progressBarBlue.get()));
            }
            else {
                textColour = ColourHelper.colour(255, (int) ClientConfigManager.errorTextRed.get(), (int) ClientConfigManager.errorTextGreen.get(), (int) ClientConfigManager.errorTextBlue.get());
                manager.getClient().textRenderer.draw(matrix, I18n.translate(title), 25, 11, textColour);
            }
            return Visibility.HIDE;
        }

        title = "You shouldn't see this!";

        
        if (starting) {
            textColour = ColourHelper.colour(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
            title = I18n.translate("advancedbackups.backup_starting");
        }
        else if (started) {
            textColour = ColourHelper.colour(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
            title = I18n.translate("advancedbackups.progress", round(percent * 100));
        }
        else if (failed) {
            textColour = ColourHelper.colour(255, (int) ClientConfigManager.errorTextRed.get(), (int) ClientConfigManager.errorTextGreen.get(), (int) ClientConfigManager.errorTextBlue.get());
            title = I18n.translate("advancedbackups.backup_failed");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else if (finished) {
            textColour = ColourHelper.colour(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
            title = I18n.translate("advancedbackups.backup_finished");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else if (cancelled) {
            textColour = ColourHelper.colour(255, (int) ClientConfigManager.errorTextRed.get(), (int) ClientConfigManager.errorTextGreen.get(), (int) ClientConfigManager.errorTextBlue.get());
            title = I18n.translate("advancedbackups.backup_cancelled");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }

        manager.getClient().textRenderer.draw(matrix, title, 25, 11, textColour);

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

        DrawableHelper.fill(matrix, 4, 28, Math.max(3, (int) f), 29, ColourHelper.colour
            (255, (int) ClientConfigManager.progressBarRed.get(), (int) ClientConfigManager.progressBarGreen.get(), (int) ClientConfigManager.progressBarBlue.get()));
        
        return Visibility.SHOW;
    }
    
    
    private static String round (float value) {
        return String.format("%.1f", value);
    }

}
