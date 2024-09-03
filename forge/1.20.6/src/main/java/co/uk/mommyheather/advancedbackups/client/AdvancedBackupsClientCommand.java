package co.uk.mommyheather.advancedbackups.client;

import java.time.Instant;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import co.uk.mommyheather.advancedbackups.core.CoreCommandSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages.Update;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraftforge.client.ClientCommandSourceStack;
import net.minecraftforge.server.ServerLifecycleHooks;

public class AdvancedBackupsClientCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(literal("backup").requires((runner) -> {
            return true;
        }).then(literal("start").executes((runner) -> {
            Minecraft.getInstance().player.connection.send(new ServerboundChatCommandPacket("backup start"));
            return 1;
         }))

         .then(literal("reload-config").executes((runner) -> {            
            Minecraft.getInstance().player.connection.send(new ServerboundChatCommandPacket("backup reload-config"));
            return 1;
         }))

         .then(literal("reset-chain").executes((runner) -> {
            Minecraft.getInstance().player.connection.send(new ServerboundChatCommandPacket("backup reset-chain"));
            return 1;
         }))

         .then(literal("snapshot").executes((runner) -> {
            Minecraft.getInstance().player.connection.send(new ServerboundChatCommandPacket("backup snapshot"));
            return 1;
         }))

         .then(literal("cancel").executes((runner) -> {
            Minecraft.getInstance().player.connection.send(new ServerboundChatCommandPacket("backup cancel"));
            return 1;
         }))

         .then(literal("reload-client-config").executes((runner) -> {
            CoreCommandSystem.reloadClientConfig((response) -> {
                runner.getSource().sendSuccess(() -> {
                    return Component.literal(response);
                }, true);
            });
            return 1;
         }))
    
        );
    }

    //I'm no fan of having this here but it seems required
    public static LiteralArgumentBuilder<CommandSourceStack> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }


}
