There is a commandline restoration tool - using this is STRONGLY recommended! Basic instructions for this or manual restoration are below.

Java 8 or newer needs to be on your PATH. If you have manually installed java, it will most likely be on your PATH.
    - If you have java installed but not on PATH, the bat and bash scripts won't work but you can still run the jar manually.

Restoration scripts now exist alongside this folder. If you are using the bash script, you may need to use chmod to make it executable.
All these two do is run the commandline tool using your system java - if you can't or don't want to use these, you can run the jar manually :

    You can run it by navigating to your mods folder and running "java -jar <filename.jar>", of course replacing <filename> with the actual filename.
    Full documentation for this is available at https://github.com/MommyHeather/AdvancedBackups#commandline



To manually restore an entire backup:


First, make sure the world is NOT loaded. Make a manual backup of it, then empty the world's folder.
Then, find the backup you want to restore. 

For complete zip backups, full differentials or full incrementals, you can simply copy the contents of the zip / folder over into the world folder.

For partial differential backups, you will first need to select the most recent full backup that is BEFORE your selected partial and restore that, then you can restore the partial.

For partial incremental backups, you will need to select the most recent full backup that is BEFORE your selected partial and restore that.
You will then need to restore every single partial backup after that full backup, moving towards more recent backup, until you reach your chosen backup.
Once you have restored your chosen backup, you are done.


To manually restore a single file:


First, make sure the world is NOT loaded. Make a manual backup of the latest copy of the file, then delete the original.

For complete zip backups, full differentials or full incrementals, look inside the backup for your chosen file and copy it into place in the world's folder.

For partial differential backups, look inside your chosen backup - if it is not present, it has not changed since the most recent differential, so get the file from that.

For partial incremental backups, look inside your chosen backup - if it is not present, check the previous partial and rinse and repeat until you find the file you need.