<a href="https://github.com/2008Choco/VeinMiner/releases/latest" alt="Latest release">
    <img src="https://img.shields.io/github/v/release/2008Choco/VeinMiner?include_prereleases" alt="Latest release">
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

# Messaging Protocol

VeinMiner takes advantage of Minecraft's [custom payload packet](https://wiki.vg/Protocol#Plugin_Message_.28clientbound.29) to send messages to and from the client to interact with an optional cient sided mod. To do this, VeinMiner makes use of a specific sequence of bytes to identify specific messages and the data they contain. The following outlines the communications between client and server for VeinMiner's payloads. Every message is prefixed with a series of bytes representing a [VarInt](https://wiki.vg/Protocol#VarInt_and_VarLong) (as per Minecraft's protocol specification) identifying the message's id, followed by the data displayed in the tables below.

The messages are sent along the `veinminer:veinminer` channel under current protocol version `1`.

VeinMiner does take advantage of types similar to that of Minecraft's default protocol which you may read about [here](https://wiki.vg/Protocol#Data_types).

## Serverbound

These are messages sent from the client to the server.

### Handshake (serverbound)

Sent by the client when logging in to inform the server that the client has the VeinMiner client-sided mod installed. The server is expected to respond promptly with a Handshake Response. Upon receiving this message, the server will automatically set the player's activation mode to `CLIENT`.

<table>
    <thead>
        <tr>
            <th>Packet ID</th>
            <th>Bound To</th>
            <th>Field Name</th>
            <th>Field Type</th>
            <th>Notes</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>0x00</td>
            <td>Server</td>
            <td>Protocol Version</td>
            <td><a href="https://wiki.vg/Protocol#VarInt_and_VarLong">VarInt</href></td>
            <td>The client's protocol version</td>
        </tr>
    </tbody>
</table>

### Toggle Vein Miner

Sent by the client to inform the server that it has activated or deactivated its vein miner keybind.

<table>
    <thead>
        <tr>
            <th>Packet ID</th>
            <th>Bound To</th>
            <th>Field Name</th>
            <th>Field Type</th>
            <th>Notes</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>0x01</td>
            <td>Server</td>
            <td>State</td>
            <td>Boolean</td>
            <td>The new state of the vein miner activation</td>
        </tr>
    </tbody>
</table>

### Request Vein Mine

Sent by the client to request the server to perform a no-op vein mine on the block at which the player is currently looking. The player's active tool category, and all other vein miner required information is calculated on the server, not by the client, exception to the provided origin position.

Note that if the player's target block is also calculated on the server but the server will make use of the position sent by the client such that it is within 2 blocks of  the server calculated block position. If the position sent to the server exceeds the 2 block distance limit, the server will respond with an empty vein mine result.

<table>
    <thead>
        <tr>
            <th>Packet ID</th>
            <th>Bound To</th>
            <th>Field Name</th>
            <th>Field Type</th>
            <th>Notes</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan=1>0x02</td>
            <td rowspan=1>Server</td>
            <td>Origin</td>
            <td>BlockPosition</td>
            <td>The position at which to initiate vein miner</td>
        </tr>
    </tbody>
</table>

### Select Pattern

Sent by the client when it wants to change vein mining patterns as a result of a key press. The server is expected to respond with a Set Pattern message to confirm that the requested pattern is to be set on the client, however the server is not guaranteed to respond in the event that the request was unsuccessful.

<table>
    <thead>
        <tr>
            <th>Packet ID</th>
            <th>Bound To</th>
            <th>Field Name</th>
            <th>Field Type</th>
            <th>Notes</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan=1>0x03</td>
            <td rowspan=1>Server</td>
            <td>Pattern Key</td>
            <td>NamespacedKey</td>
            <td>The key of the pattern to request to be set</td>
        </tr>
    </tbody>
</table>

## Clientbound

These are messages sent by the server to the client

### Handshake Response

Sent in response to a client's Handshake indicating whether or not vein miner should be allowed on the client.

<table>
    <thead>
        <tr>
            <th>Packet ID</th>
            <th>Bound To</th>
            <th>Field Name</th>
            <th>Field Type</th>
            <th>Notes</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>0x00</td>
            <td>Client</td>
            <td>Allowed</td>
            <td>Boolean</td>
            <td>Whether or not the client is allowed to use the client-sided mod. If false, the client should not continue sending any packets to the server</td>
        </tr>
    </tbody>
</table>

### Sync Registered Patterns

Sent by the server after the client has successfully shaken hands and has been sent the handshake response. Synchronizes the server's registered pattern keys with the client so that it may switch between patterns using a key bind.

<table>
    <thead>
        <tr>
            <th>Packet ID</th>
            <th>Bound To</th>
            <th>Field Name</th>
            <th>Field Type</th>
            <th>Notes</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan=2>0x01</td>
            <td rowspan=2>Client</td>
            <td>Size</td>
            <td>VarInt</td>
            <td>The amount of pattern keys being sent to the client</td>
        </tr>
        <tr>
            <td>Pattern Keys</td>
            <td>Array of NamespacedKey</td>
            <td>An array containing the namespaced keys of all vein mining patterns registered on the server</td>
        </tr>
    </tbody>
</table>

### Vein Mine Results

Sent in response to a client's Request Vein Mine including all block positions as a result of a vein mine at the client's target block and currently active tool category (according to the tool in the player's hand at the time the message was received by the server).

<table>
    <thead>
        <tr>
            <th>Packet ID</th>
            <th>Bound To</th>
            <th>Field Name</th>
            <th>Field Type</th>
            <th>Notes</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan=2>0x02</td>
            <td rowspan=2>Client</td>
            <td>Size</td>
            <td><a href="https://wiki.vg/Protocol#VarInt_and_VarLong">VarInt</href></td>
            <td>The amount of block positions that were included in the resulting vein mine</td>
        </tr>
        <tr>
            <td>Positions</td>
            <td>Array of <a href="https://wiki.vg/Protocol#Position">BlockPosition</a></td>
            <td>An array containing all block positions that would be vein mined by the server.</td>
        </tr>
    </tbody>
</table>

### Set Pattern

Sets the selected pattern on the client. Sent in response to a server-bound Select Pattern message from the client, or when set by the server manually. If the client does not recognize this pattern key, the client should fall back to the pattern at index 0 of the patterns that were sent by the Sync Registered Patterns message.

<table>
    <thead>
        <tr>
            <th>Packet ID</th>
            <th>Bound To</th>
            <th>Field Name</th>
            <th>Field Type</th>
            <th>Notes</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td rowspan=1>0x03</td>
            <td rowspan=1>Client</td>
            <td>Pattern Key</td>
            <td>NamespacedKey</td>
            <td>The pattern key to set on the client.</td>
        </tr>
    </tbody>
</table>
