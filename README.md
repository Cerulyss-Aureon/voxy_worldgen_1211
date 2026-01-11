# Voxy World Gen

Automatically generates chunks around players as they explore, integrating with Voxy for LOD generation.

![Voxy World Gen Logo](pregen-logo.png)

## Features

- **Passive Chunk Generation**: Automatically generates chunks around players and world spawn
- **Multiple Generation Patterns**: Choose from various chunk loading patterns:
  - **Spiral Out** - Generates from center outward (best for exploration)
  - **Spiral In** - Generates from edges inward
  - **Concentric Rings** - Generates in square rings around center
  - **Original** - Line-based pattern (fastest, less uniform)
  - **Random** - Random order within radius
- **Voxy Integration**: Automatically sends generated chunks to Voxy for LOD processing
- **Configurable Distances**: Set generation radius around players and spawn separately
- **Performance Options**: Control chunks per tick and prioritization settings

## Dependencies

### Required

| Mod | Description | Link |
|-----|-------------|------|
| **Voxy** | LOD rendering mod that this mod integrates with | [Modrinth](https://modrinth.com/mod/voxy) |
| **Fabric API** | Core Fabric modding library | [Modrinth](https://modrinth.com/mod/fabric-api) |
| **Fabric Loader** | Mod loader for Fabric | [Modrinth](https://modrinth.com/mod/fabric-loader) |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Install [Voxy](https://modrinth.com/mod/voxy)
4. Download this mod and place in your `mods` folder
5. (Optional) Install [Mod Menu](https://modrinth.com/mod/modmenu) and [YACL](https://modrinth.com/mod/yacl) for in-game config

## Credits

- Original mod by [liliandev](https://modrinth.com/user/liliandev)
- Fork maintained by [iSeeEthan](https://github.com/iSeeEthan)
=======

