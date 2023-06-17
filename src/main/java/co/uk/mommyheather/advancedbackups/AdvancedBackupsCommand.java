package co.uk.mommyheather.advancedbackups;

import co.uk.mommyheather.advancedbackups.core.CoreCommandSystem;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.command.CommandTreeBase;

public class AdvancedBackupsCommand extends CommandTreeBase
{
    public AdvancedBackupsCommand()
    {
        addSubcommand(new Check());
        addSubcommand(new Start());
        addSubcommand(new Force());
        addSubcommand(new Reload());
        addSubcommand(new ResetChain());
    }

    @Override
    public String getName()
    {
        return "advancedbackups";
    }


    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    @Override
    public String getUsage(ICommandSender icommandsender)
    {
        return "/advancedbackups (check|start|force-backup|reload)";
    }

    public static class Check extends CommandTreeBase {
        public Check(){}
        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            CoreCommandSystem.checkBackups((response) -> {
                sender.sendMessage(new TextComponentString(response));
            });
        }    
        @Override
        public String getName()
        {
            return "check";
        }
        @Override
        public String getUsage(ICommandSender sender) {
            return "commands.advancedbackups.check.usage";
        }
    }

    public static class Force extends CommandTreeBase {
        public Force(){}
        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            CoreCommandSystem.forceBackup((response) -> {
                sender.sendMessage(new TextComponentString(response));
            });
        }    
        @Override
        public String getName()
        {
            return "force-backup";
        }
        @Override
        public String getUsage(ICommandSender sender) {
            return "commands.advancedbackups.force.usage";
        }
    }

    public static class Reload extends CommandTreeBase {
        public Reload(){}
        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            CoreCommandSystem.reloadConfig((response) -> {
                sender.sendMessage(new TextComponentString(response));
            });
        }    
        @Override
        public String getName()
        {
            return "reload";
        }
        @Override
        public String getUsage(ICommandSender sender) {
            return "commands.advancedbackups.reload.usage";
        }
    }    
    
    public static class Start extends CommandTreeBase {
        public Start(){}
        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            CoreCommandSystem.startBackup((response) -> {
                sender.sendMessage(new TextComponentString(response));
            });
        }    
        @Override
        public String getName()
        {
            return "start";
        }
        @Override
        public String getUsage(ICommandSender sender) {
            return "commands.advancedbackups.start.usage";
        }
    }
    
    public static class ResetChain extends CommandTreeBase {
        public ResetChain(){}
        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            CoreCommandSystem.resetChainLength((response) -> {
                sender.sendMessage(new TextComponentString(response));
            });
        }    
        @Override
        public String getName()
        {
            return "reset-chain";
        }
        @Override
        public String getUsage(ICommandSender sender) {
            return "commands.advancedbackups.resetchain.usage";
        }
    }
}
