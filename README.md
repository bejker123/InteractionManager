# Interaction Manager

![Screenshot of the Interactions Menu, showing the available options.](https://cdn.modrinth.com/data/5N1n8jD7/images/a4c2459eb3a31e5db5cee0ce46b03a8af90d3315.png)

Interaction Manager is a client side mod allowing you to toggle certain block, item and mob interactions.
This mod aims to streamline the player experience, as some may find many vanilla features annoying or unnecessary.
By default, vanilla behavior is preserved.

### Entity Options
- Attacking players
- Attacking hostile mobs
- Attacking passive mobs
- Attacking villagers
- Attacking vehicles
- Attacking pets
- Sweeping-edge protection
- Interacting with mobs
- Villager trading
- Entity deny list (Deny listed entities can't be attacked)
 
### Block Options
- Breaking blocks
- Placing blocks
- Opening (block) GUIs
- Opening doors
- Replacing blocks (with offhand)
- Block deny list (Deny listed blocks can't be broken)

### Item Options
- Shovels creating paths
- Axes striping
- Allow eating
- Allow drinking potions
- Dropping items
- Locking hot bar
- Item interactions
   - Search for an item and select it
   - Toggle interacting with it on blocks or 'in the air,' as in not on blocks

### Rendering Options/Misc
- Drawing Protected Mobs
- Draw Protected Blocks
- Add (`Interactions`) Button
- Render items, in mod options
- Animate block replace
- Draw denied block placement

These options are easily found in the in game `Interactions` menu.
To access them, go to `Options`>`Interactions`

If Mod Menu is installed, by default the `Interactions` button is hidden. And the settings can be accessed using Mod Menu.
To show the `Interacions` button if Mod Menu is installed see the [Config Section](#config).
<details>
<summary>Show Image</summary>

![Screenshot of the in game options menu, showing the 'Interactions' button.](https://cdn.modrinth.com/data/5N1n8jD7/images/af905b86c6341b203eccb4769984b106a328f2c4.png)

</details>

### Config
<details>
<summary> Expand Configuration Information </summary>

#### If you don't intend to change more advanced options, feel free to skip this section.
**Options related to player behavior (block and mob interactions) are accessible in the in game options menu.**

The configuration file allows for more fine-tuning and isn't strictly necessary to edit.
It's located in `config/interactionmanager.json`.
It's stored in JSON, which is straightforward to read and modify.
Currently, all config options are accessible through the in-game menu.
</details>

### [See Change Log](CHANGELOG.md)

### Suggestions and Issues
If you have any **suggestions** or run into some **issues** please create a new ticket on the **[Issue Tracker](https://github.com/bejker123/InteractionManager/issues)**. If the issue is a bug or a crash please include game logs alongside a description of how to reproduce the issue.

### Credits and Attribution
Special thanks to [EvarikaArkana](https://github.com/EvarikaArkana) for a lot of feedback and ideas.
Checkout [their Mod](https://github.com/EvarikaArkana/Simply-Dual-Wielding), it's fully compatible with Interaction Manager. <br>
Config inspired by [Mod Menu](https://github.com/TerraformersMC/ModMenu) <br>
Search inspired by [JustEnoughItems](https://github.com/mezz/JustEnoughItems) and Alessandro Bahgat Shehata