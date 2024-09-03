LOADING A BACKUP

- Run the script found in the backups folder to start the restoration tool.
- Follow the prompts



TROUBLESHOOTING

- You need Java 8 or newer on your PATH to run the restore script. If it's failing, this is a good first thing to check.
- When using the bash script, you may need to use chmod to make it executable



DOCUMENTATION

All documentation is available at https://github.com/MommyHeather/AdvancedBackups#usage



MANUAL BACKUP RESTORATION - FULL BACKUP

- Stop the world
- Make a manual backup of it
- Empty the world's folder

For complete zip backups, full differentials or full incrementals, you can simply copy the contents of the zip / folder over into the world folder.

For partial differential backups, you will first need to select the most recent full backup that is BEFORE your selected partial and restore that, then you can restore the partial.

For partial incremental backups, you will need to select the most recent full backup that is BEFORE your selected partial and restore that.
You will then need to restore every single partial backup after that full backup, moving towards more recent backup, until you reach your chosen backup.
Once you have restored your chosen backup, you are done.



MANUAL BACKUP RESTORATION - SINGLE FILE

- Stop the world
- Make a manual backup of the file you want to restore (if present)
- Delete the file you want to restore (if present)

For complete zip backups, full differentials or full incrementals, look inside the backup for your chosen file and copy it into place in the world's folder.

For partial differential backups, look inside your chosen backup - if it is not present, it has not changed since the most recent differential, so get the file from that.

For partial incremental backups, look inside your chosen backup - if it is not present, check the previous partial and rinse and repeat until you find the file you need.
