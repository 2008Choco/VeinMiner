<a href="https://github.com/2008Choco/VeinMiner/releases/latest" alt="Latest release">
    <img src="https://img.shields.io/github/v/release/2008Choco/VeinMiner?include_prereleases" alt="Latest release">
</a>
<a href="https://www.curseforge.com/minecraft/mc-mods/veinminer-companion" alt="CurseForge">
    <img src="https://cf.way2muchnoise.eu/veinminer-companion.svg" alt="CurseForge"/>
</a>
<a href="http://choco.wtf/javadocs/veinminer" alt="Javadocs">
    <img src="https://img.shields.io/badge/Javadocs-Regularly_updated-brightgreen" alt="Javadocs"/>
</a>
<a href="https://twitter.com/intent/follow?screen_name=2008Choco_" alt="Follow on Twitter">
    <img src="https://img.shields.io/twitter/follow/2008Choco_?style=social&logo=twitter" alt="Follow on Twitter">
</a>

# VeinMiner

This Minecraft (Bukkit) plugin aims to recreate portablejim's popular Minecraft Forge mod, VeinMiner, for CraftBukkit and Spigot servers. Licensed under GPLv3, releases are made on GitHub to comply with this license. You are currently on the GitHub page for VeinMiner (for Bukkit). portablejim's VeinMiner (for Forge) repository may be found [here](https://github.com/portablejim/VeinMiner). You are welcome to fork this project and create a pull request or request features/report bugs through the issue tracker.

For information about the plugin and how to use it, please see the plugin's [resource page on SpigotMC](https://www.spigotmc.org/resources/12038/).

## Client Companion Mod

The companion mod is an optional client-sided mod to provide a more rich user-experience for players including support for custom keybinds, keybinds for pattern switching, and vein mining wire frame rendering. This can be downloaded and installed for Fabric clients on CurseForge.

[Visit CurseForge Page](https://www.curseforge.com/minecraft/mc-mods/veinminer-companion)

# Messaging Protocol

VeinMiner communicates with the Minecraft client via its [custom payload packet](https://wiki.vg/Protocol#Plugin_Message_.28clientbound.29). While VeinMiner does have its own client-sided companion mod, other client mods are capable of listening to these channels and intercepting messages. Additionally, while VeinMiner supplies API to communicate with the client, servers also have the option of listening to the raw message contents.

For details on VeinMiner's messaging protocol, see the [Client Server Mod Protocol wiki](https://github.com/2008Choco/VeinMiner/wiki/Client-Server-Mod-Protocol).
