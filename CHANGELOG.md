# Change Log

## 1.4.0
### Added
- Toggling attacking vehicles (boats, minecarts)
- Displaying block icons in the block blacklist screen
### Fixed
- Vehicles being grouped as passive entities, and turning off attacking passive mobs would make you unable to damage vehicles
- UI scrolling bug
## 1.3.0
### Added
- Entity blacklist
- Searching by block id (e.g. minecraft:stone)
### Changed
- Block blacklist button, and entity blacklist button are visually disabled if they are disabled
## 1.2.0
### Added 
- A button to toggle the block blacklist
### Fixed
- Game crash when searching, and the language is set to one using non-english characters
## 1.1.2
### Fixed
- Game crash when loading an older config
## 1.1.1
### Fixed
- Block breaking prevention not working properly on the server
## 1.1.0
### Added
- Block blacklist
- Toggling breaking blocks
## 1.0.0
### Added
- Confirmation before restoring defaults
- Compatibility with Mod Menu
- 'Restore Defaults' button
- Managing attacking pets
- Toggling attacking villagers
- Tooltips
- Toggling attacking hostile mobs
- Toggling attacking passive mobs
- Toggling fireworks setting off when used on blocks
- contact section to fabric.mod.json

### Changed
- 'Interactions' button is now by default hidden if Mod Menu is installed
- Default values to match vanilla behaviour
- Option implementation
 
### Fixed
- 'Interactions Menu' title
- Options sometimes displaying incorrect values
- 'Interactions' button placement
- Toggle axe stripping button text
 
### Removed
- Debug code

### Docs Change
- Starting from this version all future versions follow semver.

## 0.1.0
### Initial Version
### Added
- Toggling shovels creating paths
- Toggling axes stripping blocks