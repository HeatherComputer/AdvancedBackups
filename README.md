# Advanced Backups

# Note - early release. Some features here aren't implemented yet, and not all features are documented.   

A powerful backup mod for Minecraft, supporting Forge and Fabric.
Many Minecraft versions are supported - request more if the one you want isn't yet supported.

[Supported Versions](#current-versions)

[Features](#features)

[Ingame Usage](#ingame)

[Command Line Usage](#commandline)

## Current Versions:
- Forge 1.18 
- Fabric 1.18
- Forge 1.16

## Features:
Choose between zip, differential or incremental backups.
Set a schedule to backup as and when you want.
Optionally force a minimum time between backups to avoid doing so too frequently.
Backup on server startup and / or shutdown, or neither.
Set a cap to max backup sizes.
Save anywhere on disk, including network locations.
Customisable compression level.


## Usage:

### Ingame:
\- Upon first boot, an `AdvancedBackups.properties` file will be created in your server or client root directory.
\- Adjust this to suit your needs, then restart the server or use `/AdvancedBackups reload` to reload the config. A small description of each config entry is below.

| Config      | Description | Default Value |
| ----------- | ----------- | ------------- |
| config.advancedbackups.enabled      | Enable or disable backups entirely. | true |
| config.advancedbackups.activity   | Enable or disable player activity requirements. | false |
| config.advancedbackups.type   | Whether to use zip, differential or incremental backups. | zip |
| config.advancedbackups.path   | The relative or absolute location where backups are stored. | ./backups |
| config.advancedbackups.size   | The maximum backup size to keep, in GB. Oldest backups are deleted if this is exceeded. | 50 |
| config.advancedbackups.frequency.min   | The minimum time between backups. Command backups bypass this. | 0.5 |
| config.advancedbackups.frequency.shutdown  | Whether to make a backup on server shutdown. | false |
| config.advancedbackups.frequency.startup  | Whether to make a backup on server startup. | false |
| config.advancedbackups.logging.silent  | Whether to disable chat and console logging. Does not affect debug.log or error messages. | false |
| config.advancedbackups.zips.compression  | The attempted compression level for all zip files. | 4 |
| config.advancedbackups.chains.length  | The maximum chain length for incremental and differential backups. | 50 |
| config.advancedbackups.chains.compress  | Whether to compress incremental and differential backups into zip files. | true |
