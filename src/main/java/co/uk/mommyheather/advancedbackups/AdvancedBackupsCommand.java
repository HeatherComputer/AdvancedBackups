package co.uk.mommyheather.advancedbackups;

import co.uk.mommyheather.advancedbackups.core.CoreCommandSystem;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

public class AdvancedBackupsCommand extends CommandBase
{
    public AdvancedBackupsCommand()
    {
        /*addSubcommand(new Check());
        addSubcommand(new Start());
        addSubcommand(new Force());
        addSubcommand(new Reload());*/
    }

    @Override
    public String getCommandName()
    {
        return "advancedbackups";
    }


    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/advancedbackups (start|reload-config|reset-chain|snapshot)";
    }


    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
        {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        else if ("start".equals(args[0]))
        {
            Start.execute(sender);
        }
        else if ("reload-config".equals(args[0]))
        {
            Reload.execute(sender);
        }
        else if ("reset-chain".equals(args[0])) {
            ResetChain.execute(sender);
        }
        else if ("snapshot".equals(args[0])) {
            Snapshot.execute(sender);
        }
        else
        {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    public static class Reload {
        public static void execute(ICommandSender sender) {
            CoreCommandSystem.reloadConfig((response) -> {
                sender.addChatMessage(new ChatComponentText(response));
            });
        }
    }    
    
    public static class Start {
        public static void execute(ICommandSender sender) {
            CoreCommandSystem.startBackup((response) -> {
                sender.addChatMessage(new ChatComponentText(response));
            });
        }
    } 
    
    public static class ResetChain {
        public static void execute(ICommandSender sender) {
            CoreCommandSystem.resetChainLength((response) -> {
                sender.addChatMessage(new ChatComponentText(response));
            });
        }
    }

    public static class Snapshot {
        public static void execute(ICommandSender sender) {
            CoreCommandSystem.snapshot((response) -> {
                sender.addChatMessage(new ChatComponentText(response));
            });
        }

    }


}
