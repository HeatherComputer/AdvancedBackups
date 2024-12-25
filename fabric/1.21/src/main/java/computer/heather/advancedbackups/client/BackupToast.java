package computer.heather.advancedbackups.client;


import computer.heather.advancedbackups.core.config.ClientConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

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
    private static final Identifier TEXTURE = Identifier.of("toast/advancement"); 

    public static String title = "You shouldn't see this!";
    public static int textColour = 0;

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        context.drawGuiTexture(TEXTURE, 0, ClientConfigManager.darkMode.get() ? 0 : this.getHeight(), this.getWidth(), this.getHeight());

        context.drawItemWithoutEntity(stack, 8, 8);;
        
        float percent = finished ? 100 : (float) progress / (float) max;
        
        context.fill(4, 28, 156, 29, ColourHelper.colour
        (255, (int) ClientConfigManager.progressBackgroundRed.get(), (int) ClientConfigManager.progressBackgroundGreen.get(), (int) ClientConfigManager.progressBackgroundBlue.get()));

        float f = Math.min(156, (
            156 * percent
        ));

        if (!exists) {
            if (title.equals(I18n.translate("advancedbackups.backup_finished"))){
                textColour = ColourHelper.colour(255, (int) ClientConfigManager.progressTextRed.get(), (int) ClientConfigManager.progressTextGreen.get(), (int) ClientConfigManager.progressTextBlue.get());
                context.drawText(manager.getClient().textRenderer, I18n.translate(title), 25, 11, textColour, false);
                context.fill(4, 28, 156, 29, ColourHelper.colour
                    (255, (int) ClientConfigManager.progressBarRed.get(), (int) ClientConfigManager.progressBarGreen.get(), (int) ClientConfigManager.progressBarBlue.get()));
            }
            else {
                textColour = ColourHelper.colour(255, (int) ClientConfigManager.errorTextRed.get(), (int) ClientConfigManager.errorTextGreen.get(), (int) ClientConfigManager.errorTextBlue.get());
                context.drawText(manager.getClient().textRenderer, I18n.translate(title), 25, 11, textColour, false);
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

        context.drawText(manager.getClient().textRenderer, title, 25, 11, textColour, false);

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

        context.fill(4, 28, Math.max(4, (int) f), 29, ColourHelper.colour
        (255, (int) ClientConfigManager.progressBarRed.get(), (int) ClientConfigManager.progressBarGreen.get(), (int) ClientConfigManager.progressBarBlue.get()));
        
        return Visibility.SHOW;
    }
    
    
    private static String round (float value) {
        return String.format("%.1f", value);
    }

}
