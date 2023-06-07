package co.uk.mommyheather.advancedbackups.core;

import java.util.function.Consumer;

import co.uk.mommyheather.advancedbackups.core.backups.BackupCheckEnum;
import co.uk.mommyheather.advancedbackups.core.backups.BackupWrapper;
import co.uk.mommyheather.advancedbackups.core.config.AVConfig;

public class CoreCommandSystem {

    //These methods are all called by relevant command classes in version specific code
    public static void checkBackups(Consumer<String> chat) {
        BackupCheckEnum check = BackupWrapper.checkBackups();
        chat.accept(check.getCheckMessage());
    }

    public static void startBackup(Consumer<String> chat) {
        BackupCheckEnum check = BackupWrapper.checkBackups();
        chat.accept(check.getCheckMessage());
        if (check.success()) {
            chat.accept("Starting backup...");
            BackupWrapper.makeSingleBackup(0);
        }
    }

    public static void forceBackup(Consumer<String> chat) {
        chat.accept("Forcing a backup...");
        BackupWrapper.makeSingleBackup(0);
    }

    public static void reloadConfig(Consumer<String> chat) {
        chat.accept("Reloading config...");
        AVConfig.loadConfig();
        chat.accept("Done!");
    }
}
