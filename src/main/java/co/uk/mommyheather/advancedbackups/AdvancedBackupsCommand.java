package co.uk.mommyheather.advancedbackups;

import co.uk.mommyheather.advancedbackups.core.CoreCommandSystem;
import net.minecraft.command.CommandException;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.server.command.CommandTreeBase;

public class AdvancedBackupsCommand extends CommandTreeBase
{
    public AdvancedBackupsCommand()
    {
        addSubcommand(new Start());
        addSubcommand(new Reload());
        addSubcommand(new ResetChain());
        addSubcommand(new Snapshot());
        addSubcommand(new Cancel());
    }
    
    @Override
    public String getName()
    {
        return "backup";
    }
    
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 3;
    }
    
    @Override
    public String getUsage(ICommandSender icommandsender)
    {
        return "/backup (check|start|reload-config|snapshot|cancel)";
    }
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return !AdvancedBackups.server.isDedicatedServer() || super.checkPermission(server, sender);
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
            return "reload-config";
        }
        @Override
        public String getUsage(ICommandSender sender) {
            return "commands.backup.reload-config.usage";
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return !AdvancedBackups.server.isDedicatedServer() || super.checkPermission(server, sender);
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
            return "commands.backup.start.usage";
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return !AdvancedBackups.server.isDedicatedServer() || super.checkPermission(server, sender);
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
            return "commands.backup.resetchain.usage";
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return !AdvancedBackups.server.isDedicatedServer() || super.checkPermission(server, sender);
        }
    }
    
    public static class Snapshot extends CommandTreeBase {
        public Snapshot(){}
        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            CoreCommandSystem.snapshot((response) -> {
                sender.sendMessage(new TextComponentString(response));
            });
        }    
        @Override
        public String getName()
        {
            return "snapshot";
        }
        @Override
        public String getUsage(ICommandSender sender) {
            return "commands.backup.snapshot.usage";
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return !AdvancedBackups.server.isDedicatedServer() || super.checkPermission(server, sender);
        }
    }
    
    public static class Cancel extends CommandTreeBase {
        public Cancel(){}
        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            CoreCommandSystem.cancelBackup((response) -> {
                sender.sendMessage(new TextComponentString(response));
            });
        }    
        @Override
        public String getName()
        {
            return "cancel";
        }
        @Override
        public String getUsage(ICommandSender sender) {
            return "commands.backup.cancel.usage";
        }
        
        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender)
        {
            return !AdvancedBackups.server.isDedicatedServer() || super.checkPermission(server, sender);
        }
    }
}
