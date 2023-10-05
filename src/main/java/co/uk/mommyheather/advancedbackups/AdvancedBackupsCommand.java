package co.uk.mommyheather.advancedbackups;

import com.mojang.brigadier.CommandDispatcher;

import co.uk.mommyheather.advancedbackups.core.CoreCommandSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class AdvancedBackupsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> stack) {
        stack.register(Commands.literal("backup").requires((runner) -> {
            return runner.hasPermission(3);
        }).then(Commands.literal("start").executes((runner) -> {
            CoreCommandSystem.startBackup((response) -> {
                runner.getSource().sendSuccess(Component.literal(response), true);
            });
            return 1;
         }))

         .then(Commands.literal("reload").executes((runner) -> {
            CoreCommandSystem.reloadConfig((response) -> {
                runner.getSource().sendSuccess(Component.literal(response), true);
            });
            return 1;
         }))

         .then(Commands.literal("reset-chain").executes((runner) -> {
            CoreCommandSystem.resetChainLength((response) -> {
                runner.getSource().sendSuccess(Component.literal(response), true);
            });
            return 1;
         }))

         .then(Commands.literal("snapshot").executes((runner) -> {
            CoreCommandSystem.snapshot((response) -> {
                runner.getSource().sendSuccess(Component.literal(response), true);
            });
            return 1;
         }))
    
        );
    }


}
