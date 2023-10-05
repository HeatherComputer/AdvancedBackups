package co.uk.mommyheather.advancedbackups;

import com.mojang.brigadier.CommandDispatcher;

import co.uk.mommyheather.advancedbackups.core.CoreCommandSystem;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class AdvancedBackupsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> stack) {
        stack.register(CommandManager.literal("advancedbackups").requires((runner) -> {
            return runner.hasPermissionLevel(3);
        }).then(CommandManager.literal("start").executes((runner) -> {
            CoreCommandSystem.startBackup((response) -> {
                runner.getSource().sendFeedback(Text.of(response), true);
            });
            return 1;
         }))

         .then(CommandManager.literal("reload-config").executes((runner) -> {
            CoreCommandSystem.reloadConfig((response) -> {
                runner.getSource().sendFeedback(Text.of(response), true);
            });
            return 1;
         }))
         
         .then(CommandManager.literal("reset-chain").executes((runner) -> {
            CoreCommandSystem.resetChainLength((response) -> {
                runner.getSource().sendFeedback(Text.of(response), true);
            });
            return 1;
         }))
         
         .then(CommandManager.literal("snapshot").executes((runner) -> {
            CoreCommandSystem.snapshot((response) -> {
                runner.getSource().sendFeedback(Text.of(response), true);
            });
            return 1;
         }))
    
        );
    }


}
