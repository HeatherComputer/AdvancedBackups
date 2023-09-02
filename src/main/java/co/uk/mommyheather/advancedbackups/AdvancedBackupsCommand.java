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
        return "/advancedbackups (check|start|force-backup|reload|reset-chain)";
    }


    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0)
        {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        else if ("check".equals(args[0]))
        {
            Check.execute(sender);
        }
        else if ("start".equals(args[0]))
        {
            Start.execute(sender);
        }
        else if ("force-backup".equals(args[0]))
        {
            Force.execute(sender);
        }
        else if ("reload".equals(args[0]))
        {
            Reload.execute(sender);
        }
        else if ("reset-chain".equals(args[0])) {
            ResetChain.execute(sender);
        }
        else
        {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    public static class Check {
        static public void execute(ICommandSender sender) {
            CoreCommandSystem.checkBackups((response) -> {
                sender.addChatMessage(new ChatComponentText(response));
            });
        }
    }
    public static class Force {
        public static void execute(ICommandSender sender) {
            CoreCommandSystem.forceBackup((response) -> {
                sender.addChatMessage(new ChatComponentText(response));
            });
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


}
