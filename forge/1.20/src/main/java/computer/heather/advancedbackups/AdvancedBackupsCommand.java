package computer.heather.advancedbackups;

import java.io.IOException;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import computer.heather.advancedbackups.core.ABCore;
import computer.heather.advancedbackups.core.CoreCommandSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.server.ServerLifecycleHooks;

public class AdvancedBackupsCommand {
    public static void register(CommandDispatcher<CommandSourceStack> stack) {
        stack.register(Commands.literal("backup").requires((runner) -> {
            return !ServerLifecycleHooks.getCurrentServer().isDedicatedServer() || runner.hasPermission(2);
        }).then(Commands.literal("start").executes((runner) -> {
            CoreCommandSystem.startBackup((response) -> {
                runner.getSource().sendSuccess(() -> { 
                    return Component.literal(response) ;
                }, true);
            });
            return 1;
         }))

         .then(Commands.literal("reload-config").executes((runner) -> {      
            try {
                CoreCommandSystem.reloadConfig((response) -> {
                    runner.getSource().sendSuccess(() -> Component.literal(response), true);
                });
            } catch (IOException e) {
                runner.getSource().sendFailure(Component.literal("Command failed to execute! Check log for error"));
                ABCore.errorLogger.accept("Error reloading config :");
                ABCore.logStackTrace(e);
            }
            return 1;
         }))

         .then(Commands.literal("reset-chain").executes((runner) -> {
            CoreCommandSystem.resetChainLength((response) -> {
                runner.getSource().sendSuccess(() -> { 
                    return Component.literal(response) ;
                }, true);
            });
            return 1;
         }))

         .then(Commands.literal("snapshot").executes((runner) -> {
            CoreCommandSystem.snapshot((response) -> {
                runner.getSource().sendSuccess(() -> { 
                    return Component.literal(response) ;
                }, true);
            }, "snapshot");
            return 1;
         })
            .then(Commands.argument("name", StringArgumentType.greedyString()).executes((runner) -> {
                String name = StringArgumentType.getString(runner, "name");
                CoreCommandSystem.snapshot((response) -> {
                    runner.getSource().sendSuccess(() -> { 
                        return Component.literal(response) ;
                    }, true);
                }, name);
                return 1;
            })))

         .then(Commands.literal("cancel").executes((runner) -> {
            CoreCommandSystem.cancelBackup((response) -> {
                runner.getSource().sendSuccess(() -> { 
                    return Component.literal(response) ;
                }, true);
            });
            return 1;
         }))

         .then(Commands.literal("reload-client-config").executes((runner) -> {
                runner.getSource().sendSuccess(() -> { 
                    return Component.literal("This command can only be ran on the client!") ;
                }, true);
            return 1;
         }))
    
        );
    }


}
