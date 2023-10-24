package co.uk.mommyheather.advancedbackups.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
    private static final ResourceLocation TEXTURE = new ResourceLocation("toast/advancement");
    


    @Override
    public Visibility render(GuiGraphics graphics, ToastComponent toastGui, long delta) {
        graphics.blitSprite(TEXTURE, 0, 0, this.width(), this.height());
        graphics.renderFakeItem(stack, 8, 8);

        
        float percent = finished ? 100 : (float) progress / (float) max;
        
        graphics.fill(3, 28, 156, 29, -1);
        float f = Math.min(156, (
            156 * percent
        ));


        if (!exists) {   
            graphics.drawString(toastGui.getMinecraft().font, ChatFormatting.GREEN + I18n.get("advancedbackups.backup_finished"), 25, 11, -11534256);
            graphics.fill(3, 28, 156, 29, -10948014);
            return Visibility.HIDE;
        }

        String title = "You shouldn't see this!";

        
        if (starting) {
            title = ChatFormatting.GREEN + I18n.get("advancedbackups.backup_starting");
        }
        else if (started) {
            title = ChatFormatting.GREEN + I18n.get("advancedbackups.progress", round(percent * 100));
        }
        else if (failed) {
            title = ChatFormatting.RED + I18n.get("advancedbackups.backup_failed");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }
        else if (finished) {
            title = ChatFormatting.GREEN + I18n.get("advancedbackups.backup_finished");
            if (!timeSet) {
                time = System.currentTimeMillis();
                timeSet = true;
            }
        }

        graphics.drawString(toastGui.getMinecraft().font, title, 25, 11, -11534256);

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
        
        graphics.fill(3, 28, Math.max(3, (int) f), 29, -10948014);
        
        return Visibility.SHOW;
        
    }

    
    private static String round (float value) {
        return String.format("%.1f", value);
    }

}
