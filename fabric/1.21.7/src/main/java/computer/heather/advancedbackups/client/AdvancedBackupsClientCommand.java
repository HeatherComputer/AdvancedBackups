package computer.heather.advancedbackups.client;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;

import computer.heather.advancedbackups.core.CoreCommandSystem;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.Text;

public class AdvancedBackupsClientCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("backup").requires((runner) -> {
            return true;
        }).then(ClientCommandManager.literal("start").executes((runner) -> {
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket("backup start"));
            return 1;
         }))

         .then(ClientCommandManager.literal("reload-config").executes((runner) -> {
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket("backup reload-config"));
            return 1;
         }))
         
         .then(ClientCommandManager.literal("reset-chain").executes((runner) -> {
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket("backup reset-chain"));
            return 1;
         }))
         
         .then(ClientCommandManager.literal("snapshot").executes((runner) -> {
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket("backup snapshot"));
            return 1;
         })
         
         .then(ClientCommandManager.argument("name", StringArgumentType.greedyString()).executes((runner) -> {
            ParseResults<FabricClientCommandSource> parseResults = dispatcher.parse(StringArgumentType.getString(runner, "name"), runner.getSource());
            String snapshotName = parseResults.getReader().getString();
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket("backup snapshot " + snapshotName));
            return 1;
         })))
         
         .then(ClientCommandManager.literal("cancel").executes((runner) -> {
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket("backup cancel"));
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