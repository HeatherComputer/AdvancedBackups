# Advanced Backups


A powerful backup mod for Minecraft, supporting Forge and Fabric.
Many Minecraft versions are supported - request more if the one you want isn't yet supported.

[Supported Versions](#current-versions)

[Features](#features)

[Ingame Usage](#ingame)

[Command Line Usage](#commandline)

[Future Plans](#future-plans)

## Current Versions:
- Forge 1.18 
- Fabric 1.18
- Forge 1.16
- Forge 1.12
- Forge 1.7.10

## Features:
- Choose between zip, differential or incremental backups.
- Set a schedule to backup as and when you want.
- Optionally force a minimum time between backups to avoid doing so too frequently.
- Backup on server startup and / or shutdown, or neither.
- Set a cap to max backup sizes.
- Save anywhere on disk, including network locations.
- Customisable compression level.
- Commandline restoration tool built into the jar.


## Usage:

### Ingame:
\- Upon first boot, an `AdvancedBackups.properties` file will be created in your server or client root directory.

\- Adjust this to suit your needs, then restart the server or use `/advancedbackups reload` to reload the config. A small description of each config entry is below.

| Config      | Description | Default Value | Supported From |
| ----------- | ----------- | ------------- | -------------- |
| config.advancedbackups.enabled      | Enable or disable backups entirely. | true | 0.3 |
| config.advancedbackups.activity   | Enable or disable player activity requirements. | false | 1.0 |
| config.advancedbackups.save | Whether to save before making a backup. | false | 0.3 |
| config.advancedbackups.type   | Whether to use zip, differential or incremental backups. | differential | 0.3 |
| config.advancedbackups.path   | The relative or absolute location where backups are stored. | ./backups | 0.3 |
| config.advancedbackups.size   | The maximum backup size to keep, in GB. Oldest backups are deleted if this is exceeded. | 50 | 0.3 |
| config.advancedbackups.frequency.min   | The minimum time between backups. Command backups bypass this. | 0.5 | 0.3 |
| config.advancedbackups.frequency.max | If this time has passed since a backup was last made, one **will** be made. | 24 | 0.3 |
| config.advancedbackups.frequency.uptime| Whether the schedule is based on uptime. If not. it uses real-time instead. | true | 0.3 |
| config.advancedbackups.frequency.schedule | Uptime based : a looping uptime-based schedule. Real-time based : A strict schedule, following real-world time. | 12:00 | 0.3 |
| config.advancedbackups.frequency.shutdown  | Whether to make a backup on server shutdown. | false | 0.3 |
| config.advancedbackups.frequency.startup  | Whether to make a backup on server startup. | false | 0.3 |
| config.advancedbackups.frequency.delay | The delay in seconds before making a startup backup. Always at least 5 seconds. | 5 | 0.3 |
| config.advancedbackups.logging.silent  | Whether to disable chat and console logging. Does not affect debug.log or error messages. | false | N/A |
| config.advancedbackups.zips.compression  | The attempted compression level for all zip files. | 4 | 0.3 |
| config.advancedbackups.chains.length  | The maximum chain length for incremental and differential backups. | 50 | 0.3 |
| config.advancedbackups.chains.compress  | Whether to compress incremental and differential backups into zip files. | true | 0.3 |
| config.advancedbackups.chains.smart | For differential and incremental backups. Resets the chain length if every file is being backed up. | true | 0.3 |
| config.advancedbackups.purge.incremental | For incremental backups only. Enable to allow purging incremental backup chains if the defined storage usage is limit exceeded. | false | 1.0 |

#### Commands:

\- All entries in the table below must be prefixed with `/advancedbackups`.

- Example : `/advancedbackups force-backup`


| Command | Description | Supported From |
| ----------- | ----------- | -------------- |
| check      | Checks if a backup would be made at this point in time, and tells you the result. Does not make a backup.| 1.0 |
| start | Starts a backup if all checks pass. Tells you check results.| 1.0 |
| reload | Reloads the config.| 1.0 |
| force-backup | Forces a backup without running any checks.| 1.0 |
| reset-chain | Resets any current chain length.| 1.0 |


### Commandline:
TODO - export option for backups.

\- Run the jar directly from the mods folder. `java -jar AdvancedBackups-modloader-mcversion-modversion` for example, replacing the filename with the correct one for your installation.

\- It will read your config. Then, it will ask you which backup type to restore.

\- Afterwards, it will ask you if you are on a client or server. If you run the forge/fabric server software, choose server, otherwise it's a client.

\- Then it will ask for your world name. It will verify this, and will not proceed if it can't find the name.

\- Then it will prompt you to choose a backup to restore. The most recent are listed last. With differential and incremental upgrades, the backups will be labelled as partial or fill.

- This is only for your information, and will not affect the restoration process.

\- The below image shows this for a differential backup.


![image](https://github.com/MommyHeather/AdvancedBackups/assets/66441550/ba73af5f-1bca-4ec7-9a3e-a21f6b7350ab)

\- Next you choose whether you're restoring the entire backup, or a singular file.

- This restores the **ENTIRE WORLD STATE** at the time of the backups. Partial backups will have the relevant previous backups also restored to make this happen.
    
    
Finally, you can accept the warning prompt it gives you.

If you chose to restore the entire backup, the program will exit when it is complete.
If you chose to restore a singular file, you'll be presented with a rudimentary file browser. The below example is for differential backups:

![image](https://github.com/MommyHeather/AdvancedBackups/assets/66441550/d5f771f5-32d4-435f-aa82-a1da52e3996c)

Directories are marked, and `../` will traverse up a directory.

If your selected backup type is differential or incremental, files will be marked with the date they're from. This is purely for your information, the file's state will match that of when the selected backup was made.

Once you select a file, it will be restored. The program will then exit.

![image](https://github.com/MommyHeather/AdvancedBackups/assets/66441550/7a2c5d4c-e8db-48c3-8353-46486b9f86b3)



# Future Plans:
- Note : these are in no particular order.
- [Profiles](#profiles)
- [More Commands](#more-commands)
- [Client Feedback](#client-feedback)
- [Commandline Improvements](#commandline)

## Profiles

- Profiles plan to allow one server to have several backup configs active at once.
- Say, take an incremental backup every day, but make sure a zip backup is made at least once a week.
- Or maybe, you want to save a differential backup every hour on a remote drive, but keep a full-world zip backup ready locally every 24 hours.

### Profiles can do this!

Essentially, profiles will be an optional set of additional configs.

These are completely separate - meaning they will not interfere with one another, at all.
- Certain values, such as min and max, may have problems if two profiles use the same location and backup type. I am not sure if I will fix this yet.

The profile system will not be compatible with versions that predate its release.
- Backups themselves will be unaffected, and any version will be able to restore them. 
- The backup location detection however may not work if your restoration cli predates the profile release.

Upon first load with a version updated to use profiles, a default profile will be made.
- If you have a config present from an older version, the changes you made will be automatically applied to the default profile for you.
- *This means that a user who does not wish to use profiles will not be affected whatsoever. They can edit the default profile as they would with a standard config.*


## More commands

Shortly after release, I wish to add more commands.

- Config editing on the fly
- One-off backup types
    - Currently, the `start` and `force-backup` commands only allow you to make a backup of the type specified in config. This will change.


## Client feedback

Having some client feedback would allow connected clients that have the mod view backup progress.
- This should be toggleable in config.
- A config option to only show progress to ops should exist.

This might be in the form of either a progress bar, or a simple percentage.

Clients with the mod should have a way to opt out of this.


## Commandline

Export feature :
- When asked whether restoring a singular file or the whole world, a third option will be present : export.
- This will package the backup into a singular zip file (...so yeah, just copy over the backup in the case of zip backups).
- It will leave the actual world intact.
  
