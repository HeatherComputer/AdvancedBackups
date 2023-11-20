package co.uk.mommyheather.advancedbackups;

import com.mojang.brigadier.CommandDispatcher;

import co.uk.mommyheather.advancedbackups.core.CoreCommandSystem;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class AdvancedBackupsCommand {
    public static void register(CommandDispatcher<CommandSource> stack) {
        stack.register(Commands.literal("backup").requires((runner) -> {
            return !ServerLifecycleHooks.getCurrentServer().isDedicatedServer() || runner.hasPermission(3);
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

         .then(Commands.literal("reset-chain").executes((runner) -> {
            CoreCommandSystem.resetChainLength((response) -> {
                runner.getSource().sendSuccess(new StringTextComponent(response), true);
            });
            return 1;
         }))

         .then(Commands.literal("snapshot").executes((runner) -> {
            CoreCommandSystem.snapshot((response) -> {
                runner.getSource().sendSuccess(new StringTextComponent(response), true);
            });
            return 1;
         }))
    
        );
    }


}
