package co.uk.mommyheather.advancedbackups.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
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


    @Override
    public Visibility render(PoseStack matrix, ToastComponent toastGui, long delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ToastComponent.blit(matrix, 0, 0, 0, 0, this.width(), this.height());
        toastGui.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(matrix, stack, 8, 8);

        
        float percent = finished ? 100 : (float) progress / (float) max;
        
        Gui.fill(matrix, 3, 28, 156, 29, -1);
        float f = Math.min(156, (
            156 * percent
        ));


        if (!exists) {   
            toastGui.getMinecraft().font.draw(matrix, ChatFormatting.GREEN + I18n.get("advancedbackups.backup_finished"), 25, 11, -11534256);
            Gui.fill(matrix, 3, 28, 156, 29, -10948014);
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

        toastGui.getMinecraft().font.draw(matrix, title, 25, 11, -11534256);

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
        
        Gui.fill(matrix, 3, 28, Math.max(3, (int) f), 29, -10948014);
        
        return Visibility.SHOW;
        
    }

    
    private static String round (float value) {
        return String.format("%.1f", value);
    }

}
