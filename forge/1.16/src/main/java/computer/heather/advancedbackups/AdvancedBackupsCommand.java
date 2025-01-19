package computer.heather.advancedbackups;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import computer.heather.advancedbackups.core.CoreCommandSystem;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class AdvancedBackupsCommand {
    public static void register(CommandDispatcher<CommandSource> stack) {
        stack.register(Commands.literal("backup").requires((runner) -> {
            return !ServerLifecycleHooks.getCurrentServer().isDedicatedServer() || runner.hasPermission(2);
        }).then(Commands.literal("start").executes((runner) -> {
            CoreCommandSystem.startBackup((response) -> {
                runner.getSource().sendSuccess(new StringTextComponent(response), true);
            });
            return 1;
         }))

         .then(Commands.literal("reload-config").executes((runner) -> {
            CoreCommandSystem.reloadConfig((response) -> {
                runner.getSource().sendSuccess(new StringTextComponent(response), true);
            });
            return 1;
         }))

         .then(Commands.literal("reload-client-config").executes((runner) -> {
            runner.getSource().sendSuccess(new StringTextComponent("This command can only be ran on a client!"), true);
            return 1;
         }))

         .then(Commands.literal("reset-chain").executes((runner) -> {
            CoreCommandSystem.resetChainLength((response) -> {
                runner.getSource().sendSuccess(new StringTextComponent(response), true);
            });
            return 1;
         }))

         .then(Commands.literal("snapshot").executes((runner) -> {
            CoreCommandSystem.snapshot((response) -> {
                runner.getSource().sendSuccess(new StringTextComponent(response), true);
            }, "snapshot");
            return 1;
         })
            .then(Commands.argument("name", StringArgumentType.greedyString()).executes((runner) -> {
                CoreCommandSystem.snapshot((response) -> {
                    runner.getSource().sendSuccess(new StringTextComponent(response), true);
                }, StringArgumentType.getString(runner, "name"));
                return 1;
            })))

         .then(Commands.literal("cancel").executes((runner) -> {
            CoreCommandSystem.cancelBackup((response) -> {
                runner.getSource().sendSuccess(new StringTextComponent(response), true);
            });
            return 1;
         }))
    
        );
    }


}
