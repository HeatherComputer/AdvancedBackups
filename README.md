# Advanced Backups 
[![](https://img.shields.io/curseforge/dt/876284?label=downloads&style=for-the-badge&logo=curseforge&color=2D2D2D)](https://www.curseforge.com/minecraft/mc-mods/advanced-backups) [![](https://img.shields.io/modrinth/dt/Jrmoreqs?label=downloads&style=for-the-badge&logo=modrinth&color=2D2D2D)](https://modrinth.com/mod/advanced-backups) [![](https://img.shields.io/badge/discord-2D2D2D?style=for-the-badge&logo=discord)](https://discord.gg/SxN7JtzUyX)

A powerful backup mod for Minecraft, supporting Neoforge, Forge and Fabric.
Many Minecraft versions are supported - request more if the one you want isn't yet supported.

[Supported Versions](#current-versions)

[Features](#features)

[FAQ](#faq)

[Version differences](#version-differences)

[What does each backup type mean?](#backup-types)

[Ingame Usage](#ingame)

[Command Line Usage](#commandline)

[Future Plans](#future-plans)

[Notices](#notices)

## Features:
- Command-line restoration tool built into the jar, with helper scripts to run it
- Optional differential or incremental backups to reduce backup size whilst still retaining the same data
- Extensive config options to control schedules, logging, activity requirements and more
- Three different automatic purging options - full control over when a backup is deleted
- Command based "snapshots" that are immune to automatic purging
- Client-side toasts - get notified when backups happen. Can be disabled client or server-side, and restricted to ops or allowed for all players
- Set a minimum or maximum time between backups - avoid common problem scenarios for local worlds


## FAQ:
- How do the client toasts work with Spigot servers?

  - A client needs the Forge, Neoforge or Fabric version of the mod to see the toasts. This works even on Spigot servers!
- Will a backup be made if nobody has been online since the last backup?
  - By default, no! This can be changed in config if need be.
- Can the mod run serverside only?
  - Yes, the mod can work entirely on the server - a clientside installation is only required to see backup toasts.
- How do I run the commandline tool?
  - There's scripts provided in your backup folder, or you can use `java -jar` to run it. Java is required on your PATH. 
  - See the readme in your backup folder for more information.


## Version Differences:
- Spigot has no client.
- 1.7.10 uses a text overlay to show progress instead of the toast system.


## Backup Types:
- Zip backups are fairly self explanatory - zip up the whole world and there's your backup.
- Differential backups have a concept of "full" and "partial" backups :
    - "full" backups are the same as a zip. 
    - "partial" backups contain only the files that have changed since the last "full" backup. This can greatly reduce space usage.
    
- Incremental backups are similar to differential, but they backup files that have changed since the last *partial* instead - which further reduces space usage, but can create an instability as a loss of a partial can damage all backups up until the next full backup. They also take the longest to restore.

Because of the way this "chain" works with incrementals, they purge slightly differently:
- Whole chains have to be purged at once.
- A chain will only be purged when over the configured minimum amount of chains to keep.
- Purging of incrementals can be outright disabled.
- When using day-based purging, *all backups in the chain* most be older than the configured day count for the chain in question to be deleted.


## Usage:

### Ingame:
\- Upon first boot, an `AdvancedBackups.properties` file will be created in your server or client `config` directory.

\- Adjust this to suit your needs, then restart the server or use `/backup reload-config` in versions 2.2+ or `/advancedbackups reload` in older pre-2.2 versions to reload the config. A small description of each config entry is below.

<details>
<summary>config</summary>


| Config      | Description | Default Value | Supported From / Until |
| ----------- | ----------- | ------------- | -------------- |
| config.advancedbackups.enabled      | Enable or disable backups entirely. | true | 0.3 |
| config.advancedbackups.save | Whether to save before making a backup. | true | 0.3 |
| config.advancedbackups.buffer | The buffer size in memory to use for (some) file I/O. | 1048576 | 3.5 |
| config.advancedbackups.flush | Whether to flush when making this save. Usually never needed, and can create a lag spike if enabled. (Unused prior to minecraft 1.16) | false | 3.1 |
| config.advancedbackups.activity   | Enable or disable player activity requirements. | true | 1.0 |
| config.advancedbackups.type   | Whether to use zip, differential or incremental backups. | differential | 0.3 |
| config.advancedbackups.blacklist   | A comma separated list of relative paths to files not to backup. | session.lock | 3.5.1 |
| config.advancedbackups.path   | The relative or absolute location where backups are stored. | ./backups | 0.3 |
| config.advancedbackups.size   | The maximum backup size to keep, in GB. Oldest backups are deleted if this is exceeded. | 50 | 0.3 / 3.4 |
| config.advancedbackups.frequency.min   | The minimum time between backups. Command backups bypass this. | 0.5 | 0.3 |
| config.advancedbackups.frequency.max | If this time has passed since a backup was last made, one **will** be made. | 24 | 0.3 |
| config.advancedbackups.frequency.uptime| Whether the schedule is based on uptime. If not. it uses real-time instead. | true | 0.3 |
| config.advancedbackups.frequency.schedule | Uptime based : a looping uptime-based schedule. Real-time based : A strict schedule, following real-world time. | 1:00 | 0.3 |
| config.advancedbackups.frequency.shutdown  | Whether to make a backup on server shutdown. | false | 0.3 |
| config.advancedbackups.frequency.startup  | Whether to make a backup on server startup. | false | 0.3 |
| config.advancedbackups.frequency.delay | The delay in seconds before making a startup backup. Always at least 5 seconds. | 30 | 0.3 |
| config.advancedbackups.logging.silent  | Whether to disable chat and console logging. Does not affect debug.log or error messages. | false | 2.0 / 3.6 |
| config.advancedbackups.logging.clients  | Which clients to send backup progress info to. | ops | 3.6 |
| config.advancedbackups.logging.clientfrequency  | How often to send backup progress info to clients, in milliseconds. | 500 | 3.6 |
| config.advancedbackups.logging.console  | Whether to log backup progress info to console. Start and end are always logged. | true | 3.6 |
| config.advancedbackups.logging.consolefrequency  | How often to log backup progress info to console, in milliseconds. | 5000 | 3.6 |
| config.advancedbackups.purge.size | The maximum total backup size to keep. Moved from `config.advancedbackup.size`, will auto migrate. Optional. | 50 | 3.4 |
| config.advancedbackups.purge.days | The maximum number of days to keep backups for. Optional. | 0 | 3.4 |
| config.advancedbackups.purge.count | The maximum number of backups to keep. Optional. | 0 | 3.4 |
| config.advancedbackups.zips.compression  | The attempted compression level for all zip files. | 4 | 0.3 |
| config.advancedbackups.chains.length  | The maximum chain length for incremental and differential backups. | 50 | 0.3 |
| config.advancedbackups.chains.compress  | Whether to compress incremental and differential backups into zip files. | true | 0.3 |
| config.advancedbackups.chains.smart | For differential and incremental backups. Resets the chain length if every file is being backed up. | true | 0.3 |
| config.advancedbackups.chains.maxpercent | If the size of a partial backup exceeds this % of a full backup's size, a full backup is made instead. | 50 | 2.0
| config.advancedbackups.purge.incremental | For incremental backups only. Enable to allow purging incremental backup chains if the defined storage usage is limit exceeded. | true | 1.0 |

</details>


#### Commands:

\- All entries in the table below must be prefixed with `/backup` in 2.2+ or `/advancedbackups` in older pre-2.2.

- Example : `/backup start`


<details>
<summary>pre-2.2</summary>

| Command | Description | Supported From |
| ----------- | ----------- | -------------- |
| check      | Checks if a backup would be made at this point in time, and tells you the result. Does not make a backup.| 1.0 |
| start | Starts a backup if all checks pass. Tells you check results.| 1.0 |
| reload | Reloads the config.| 1.0 |
| force-backup | Forces a backup without running any checks.| 1.0 |
| reset-chain | Resets any current chain length.| 1.0 |
</details>


<details open>
<summary>2.2+</summary>

| Command | Description | Supported From |
| ----------- | ----------- | -------------- |
| start | Makes a backup using the configured backup type.| 2.2 |
| reload-config | Reloads the config.| 2.2 |
| reload-client-config | Reloads the clientside config.| 3.5 |
| snapshot | Creates a "snapshot" backup that cannot be auto-deleted.| 2.2 |
| snapshot \<name> | Creates a "snapshot" backup with the given name.| 3.5 |
| reset-chain | Resets any current chain length.| 1.0 |

</details>

### Commandline:

\- Run the jar directly from the mods folder. `java -jar AdvancedBackups-modloader-mcversion-modversion` for example, replacing the filename with the correct one for your installation.
- As of version 2.0, the mod creates bat or sh scripts you can use instead.

\- It will read your config. Then, it will check for backups from other mods.
- If you have backups from other mods, you'll be asked if you wish to work with those, or ones made by Advanced Backups.
- ![image](https://github.com/HeatherComputer/AdvancedBackups/assets/66441550/6b2a3b87-e25b-4c24-9d14-a022ad8471f6)
- If you choose to use the other mod, it will skip world selection and backup type selection. Note that the export feature does *not* work with backups from other mods.

  
\- Afterwards, it will ask you which backup type to restore. It tells you which type is specified in config - use this if you're unsure.


\- Next you choose whether you're exporting a backup, restoring the entire backup, or a singular file.
- Exporting will export the entire world state at the time of the chosen backup. Restoring the entire backup does the same, but in place of the existing world, and choosing to restore a singular file will let you choose which file.
- The export option will ask which backup to export, and present a confirmation message. Once accepted, the program will export for you and exit.

![image](https://github.com/HeatherComputer/AdvancedBackups/assets/66441550/e0402c55-5bf6-4662-a890-a4cda900d02b)


\- Then it will help find a world - first asking if you play on a client or server, then by listing world names.
- Backups not labelled as for the world you chose will be hidden. The export option can be used if your backup was made in an older version or you wish to restore into a fresh world.

![image](https://github.com/HeatherComputer/AdvancedBackups/assets/66441550/854da5f7-c5ea-4233-b772-3952bfa37cbd)


\- Then it will prompt you to choose a backup to restore. The most recent are listed last. With differential and incremental upgrades, the backups will be labelled as partial or full.

- This is only for your information, and will not affect the restoration process.

\- The below image shows this for a differential backup.

![image](https://github.com/HeatherComputer/AdvancedBackups/assets/66441550/f580e348-4aee-40fb-9ab4-5c7c8d97ff7c)

    
Finally, you can accept the warning prompt it gives you.

If you chose to restore the entire backup, the program will exit when it is complete.


If you chose to restore a singular file, you'll be presented with a rudimentary file browser. 

Directories are marked, and `../` will traverse up a directory.

If your selected backup type is differential or incremental, files will be marked with the date they're from. This is purely for your information, the file's state will match that of when the selected backup was made.

Once you select a file, it will be restored. The program will then exit.

![image](https://github.com/HeatherComputer/AdvancedBackups/assets/66441550/ca7846b5-e326-449e-852a-9e0d670ea65c)




# Future Plans:
- Note : these are in no particular order.
- [Profiles](#profiles)
- [More Commands](#more-commands)
- [Client Feedback Improvements](#client-feedback-improvements)
- [API and in-client restoration](#api-and-clientside-restoration)

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
    - Currently, the `start` command only allows you to make a backup of the type specified in config. This will change.


## Client feedback improvements

- As of version 3.2, all players with operator permissions will get progress updates.
- As of version 3.5, there is a client config which includes a toggle.
- A serverside option for sending this info to *all* players might be beneficial.


## API and clientside restoration

I plan to add an API, which will be a rather hefty project. With this done however:

- I'll be rewriting the commandline to use this api. It'll clear a ton of tech debt, along with allowing for easier maintenance and updates.

- I'll be able to make a separate gui for restoring backups.
    - It won't have such a wide range of supported versions, but using the API will help keep maintenance costs down and keep older versions of the restoration mod working.

- External tools can have an easier time integrating, if they so choose. (say, control panels, or other mods)




# Notices:
- This software bundles an unmodified version of [jansi](https://github.com/fusesource/jansi).
    - Jansi uses the Apache-2.0 license, and permits redistribution. Its full license can be read [here](https://github.com/fusesource/jansi/blob/cdb8d8c6daf86aaa2de31f8b047bd24acfb56d90/license.txt).
