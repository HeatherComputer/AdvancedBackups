package computer.heather.advancedbackups;

import com.mojang.brigadier.CommandDispatcher;

import computer.heather.advancedbackups.core.CoreCommandSystem;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class AdvancedBackupsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> stack) {
        stack.register(CommandManager.literal("backup").requires((runner) -> {
            return !AdvancedBackups.server.isDedicated() || runner.hasPermissionLevel(3);
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
         
         .then(CommandManager.literal("cancel").executes((runner) -> {
            CoreCommandSystem.cancelBackup((response) -> {
                runner.getSource().sendFeedback(Text.of(response), true);
            });
            return 1;
         }))
         
         .then(CommandManager.literal("reload-client-config").executes((runner) -> {
            runner.getSource().sendFeedback(Text.of("This command can only be ran on the client!"), false);
            return 1;
         }))
    
        );
    }


}
