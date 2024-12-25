package computer.heather.advancedbackups.client;


import computer.heather.advancedbackups.core.CoreCommandSystem;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;

public class AdvancedBackupsClientCommand {
    public static void register() {
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("backup").requires((runner) -> {
            return true;
            //Originally checked for permission level 0. No point checking whether that caused the issue when just a return true here works
        }).then(ClientCommandManager.literal("start").executes((runner) -> {
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new ChatMessageC2SPacket("/backup start"));
            return 1;
         }))

         .then(ClientCommandManager.literal("reload-config").executes((runner) -> {
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new ChatMessageC2SPacket("/backup reload-config"));
            return 1;
         }))
         
         .then(ClientCommandManager.literal("reset-chain").executes((runner) -> {
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new ChatMessageC2SPacket("/backup reset-chain"));
            return 1;
         }))
         
         .then(ClientCommandManager.literal("snapshot").executes((runner) -> {
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new ChatMessageC2SPacket("/backup snapshot"));
            return 1;
         }))
         
         .then(ClientCommandManager.literal("cancel").executes((runner) -> {
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new ChatMessageC2SPacket("/backup cancel"));
            return 1;
         }))
         
         .then(ClientCommandManager.literal("reload-client-config").executes((runner) -> {
            CoreCommandSystem.reloadClientConfig((response) -> {
                runner.getSource().sendFeedback(Text.of(response));
            });
            return 1;
         }))
    
        );
    }


}
