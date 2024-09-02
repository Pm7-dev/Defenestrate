
![defenestrateBanner.png](images%2FdefenestrateBanner.png)

Window not required!  

This is a spigot plugin designed to give players the ability to pick up and throw most blocks and entities in the game. You can customize the throw power individually for blocks, players, and entities, and can disable the throwing of any group if you so choose.

## Usage
Simply crouch and right click with no item in your main hand to pick a Player, Entity, or Block up. After that, look in the direction you want to launch, and left click to throw!  
Note: This will not work if the block/entity you are trying to pick up/throw is in the spawn protection set in `server.properties` and your player is not an operator

## Installation and Setup
To install, simply add the [latest release](https://github.com/Pm7-dev/Defenestrate/releases) to the `Plugins` folder in any spigot fork server and run `/reload`

Settings can be either changed in the plugin config file, or by using the in-game `/dSettings` command.  
The `/dSettings` command works as follows:
`/dSettings <get|set|list> <setting> <value>`  
`/dSettings list` - Lists all settings and their respective values  
`/dSettings get <setting>` - Returns the value of the specified setting  
`/dSettings set <setting> <value>` - Sets the value of the specified setting to the specified value


<details>
  <summary> List of settings that can be changed </summary>

Power settings:
- `playerThrowPower (Default: 2.5)` - The power at which players are thrown
- `entityThrowPower (Default: 2.5)` - The power at which other entities are thrown
- `blockThrowPower (Default: 2.5)` - The power at which blocks are thrown

Feature toggles:
- `playerThrowEnabled (Default: true)` - If any player will be allowed to throw players
- `entityThrowEnabled (Default: true)` - If any player will be allowed to throw other entities
- `blockThrowEnabled (Default: true)` - If any player will be allowed to throw blocks

Permission settings:
- `throwPlayersRequiresPermission (Default: false)` - If players need a permission to throw players (only used if playerThrowEnabled is true)
- `throwEntitiesRequiresPermission (Default: false)` - If players need a permission to throw entities (only used if entityThrowEnabled is true)
- `throwBlocksRequiresPermission (Default: false)` - If players need a permission to throw blocks (only used if blockThrowEnabled is true)

Other settings:
- `oldBlockHandling (Default: false)` - Uses a simpler method of handling the picking up and throwing of blocks (not recommended, more info [here](https://github.com/Pm7-dev/Defenestrate#old-block-handling))
- `breakThingsMode (Default: false)` - Removes the filter stopping players from picking up and throwing illegal blocks (nether portals, bedrock, barriers)

</details>

## Permissions
There is no built-in permissions manager in this plugin. However, if you wish to set up ranks to give players permission to use parts of this plugin, you can use a permission manager such as LuckPerms to give players these permissions.  

`defenestrate.players` - Permission for throwing players (only required if throwPlayersRequiresPermission is true)  
`defenestrate.entities` - Permission for throwing entities (only required if throwEntitiesRequiresPermission is true)  
`defenestrate.blocks` - Permission for throwing blocks (only required if throwBlocksRequiresPermission is true)  
`defenestrate.settings` - Permission needed for non-operators to use `/dSettings`  
`defenestrate.all` - Grants every permission listed

## Old Block Handling
The old block handling method uses Minecraft's "falling block" entity. The issue with this is that falling blocks have a habit of just... randomly breaking. If you pick a block up while crouching and just in general being in the wrong area, the block will just drop. Even more than that, when falling blocks land, there's a stupidly large chance that the block just drops as an item instead of being placed as a block.

My solution to this is to create a ""custom"" block entity using the 1.19.4 Block Displays and Interactions, along with a very shrunk down axolotl to handle the gravity. This means that the block entities are actually 3 entities stacked on top of each other. If your server starts lagging because of players throwing blocks, it may be worth it to use the old block handling. 

## Issues and Additional Features
If you've found an issue with the plugin or have a request for a new feature, head over to the [issues](https://github.com/Pm7-dev/Defenestrate/issues) page and create an issue. Please be sure to include as much detail about the bug/feature request as possible in the issue.
