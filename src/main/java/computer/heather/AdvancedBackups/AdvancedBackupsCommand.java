package computer.heather.AdvancedBackups;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import co.uk.mommyheather.advancedbackups.core.CoreCommandSystem;


// /backup start | reload-config | reload-client-config | snapshot | reset-chain
public class AdvancedBackupsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
        {
            return false;
        }
        else if ("start".equals(args[0]))
        {
            Start.execute(sender);
        }
        else if ("reload-config".equals(args[0]))
        {
            Reload.execute(sender);
        }
        else if ("reload-client-config".equals(args[0]))
        {
            ReloadClient.execute(sender);
        }
        else if ("reset-chain".equals(args[0])) {
            ResetChain.execute(sender);
        }
        else if ("snapshot".equals(args[0])) {
            Snapshot.execute(sender);
        }
        else if ("cancel".equals(args[0])) {
            Cancel.execute(sender);
        }
        else
        {
            return false;
        }
        return true;
    }
    
    public static class Reload {
        public static void execute(CommandSender sender) {
            CoreCommandSystem.reloadConfig((response) -> {
                sender.sendMessage(response);
            });
        }
    }   

    public static class ReloadClient {
        public static void execute(CommandSender sender) {
            sender.sendMessage("This can only be ran on the client!");
        }
    }    
    
    public static class Start {
        public static void execute(CommandSender sender) {
            CoreCommandSystem.startBackup((response) -> {
                sender.sendMessage(response);
            });
        }
    } 
    
    public static class ResetChain {
        public static void execute(CommandSender sender) {
            CoreCommandSystem.resetChainLength((response) -> {
                sender.sendMessage(response);
            });
        }
    }

    public static class Snapshot {
        public static void execute(CommandSender sender) {
            CoreCommandSystem.snapshot((response) -> {
                sender.sendMessage(response);
            });
        }

    }

    public static class Cancel {
        public static void execute(CommandSender sender) {
            CoreCommandSystem.cancelBackup((response) -> {
                sender.sendMessage(response);
            });
        }

    }


}
