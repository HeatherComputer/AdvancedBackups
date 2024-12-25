package computer.heather.advancedbackups;

import computer.heather.advancedbackups.core.CoreCommandSystem;
import computer.heather.advancedbackups.network.NetworkHandler;
import computer.heather.advancedbackups.network.PacketClientReload;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
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
        return "backup";
    }


    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/backup (start|reload-config|reload-client-config|reset-chain|snapshot|cancel)";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return !AdvancedBackups.server.isDedicatedServer() || super.canCommandSenderUseCommand(sender);
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

    public static class ReloadClient {
        public static void execute(ICommandSender sender) {
            if (sender instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) sender;
                NetworkHandler.HANDLER.sendTo(new PacketClientReload(), player);
                return;
            }
            sender.addChatMessage(new ChatComponentText("This can only be ran on the client!"));
            /*
            CoreCommandSystem.reloadClientConfig((response) -> {
                sender.addChatMessage(new ChatComponentText(response));
            });
            */
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

    public static class Cancel {
        public static void execute(ICommandSender sender) {
            CoreCommandSystem.cancelBackup((response) -> {
                sender.addChatMessage(new ChatComponentText(response));
            });
        }

    }


}
