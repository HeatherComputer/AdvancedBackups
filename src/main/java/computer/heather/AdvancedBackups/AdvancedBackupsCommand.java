package computer.heather.AdvancedBackups;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AdvancedBackupsCommand extends Command {

    public AdvancedBackupsCommand() {
        super("backup", "/backup start | reload-config | reload-client-config | snapshot | reset-chain", "Commands for the Advanced Backups mod", Arrays.asList(new String[] {"advancedbackups"}));
    }

    @Override
    public boolean execute(CommandSender arg0, String arg1, String[] arg2) {
        return false;
    }
    
}
