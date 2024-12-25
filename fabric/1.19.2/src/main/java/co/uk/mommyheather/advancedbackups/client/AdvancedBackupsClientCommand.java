package computer.heather.advancedbackups.client;


import java.time.Instant;

import com.mojang.brigadier.CommandDispatcher;

import computer.heather.advancedbackups.core.CoreCommandSystem;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessageList.Acknowledgment;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.Text;

public class AdvancedBackupsClientCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("backup").requires((runner) -> {
            //Originally checked for permission level 0, no point checking if that actually caused problems
            return true;
        }).then(ClientCommandManager.literal("start").executes((runner) -> {
            Acknowledgment acknowledgment = MinecraftClient.getInstance().player.networkHandler.consumeAcknowledgment();
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket("backup start", Instant.now(), 0L, ArgumentSignatureDataMap.EMPTY, false, acknowledgment));
            return 1;
         }))

         .then(ClientCommandManager.literal("reload-config").executes((runner) -> {
            Acknowledgment acknowledgment = MinecraftClient.getInstance().player.networkHandler.consumeAcknowledgment();
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket("backup reload-config", Instant.now(), 0L, ArgumentSignatureDataMap.EMPTY, false, acknowledgment));
            return 1;
         }))
         
         .then(ClientCommandManager.literal("reset-chain").executes((runner) -> {
            Acknowledgment acknowledgment = MinecraftClient.getInstance().player.networkHandler.consumeAcknowledgment();
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket("backup reset-chain", Instant.now(), 0L, ArgumentSignatureDataMap.EMPTY, false, acknowledgment));
            return 1;
         }))
         
         .then(ClientCommandManager.literal("snapshot").executes((runner) -> {
            Acknowledgment acknowledgment = MinecraftClient.getInstance().player.networkHandler.consumeAcknowledgment();
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket("backup snapshot", Instant.now(), 0L, ArgumentSignatureDataMap.EMPTY, false, acknowledgment));
            return 1;
         }))
         
         .then(ClientCommandManager.literal("cancel").executes((runner) -> {
            Acknowledgment acknowledgment = MinecraftClient.getInstance().player.networkHandler.consumeAcknowledgment();
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new CommandExecutionC2SPacket("backup cancel", Instant.now(), 0L, ArgumentSignatureDataMap.EMPTY, false, acknowledgment));
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