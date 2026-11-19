# Chat Copy

A client-side Fabric mod for Minecraft **1.21.11** that adds a blue **(copy)** button after every chat message, so you can copy anything said in chat with one click.

## Features

- A blue **(copy)** button appears at the end of every chat message
- Hover over it to see the **Copy Text** tooltip
- Click it to copy the full message to your clipboard

## Usage

1. Join a world or server.
2. Open the chat with **T** (clicks only work while the chat box is open).
3. Click the blue **(copy)** next to any message.
4. Paste it anywhere with **Ctrl+V**.

## Requirements

- Minecraft 1.21.11
- [Fabric Loader](https://fabricmc.net/use/) 0.20.1 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 21 or newer

## Installation

1. Install Fabric Loader for Minecraft 1.21.11.
2. Download Fabric API and put it in your `mods` folder.
3. Put the Chat Copy `.jar` in the same `mods` folder.
4. Launch the game.

This is a client-side mod. It doesn't need to be installed on servers.

## Building from source

```bash
./gradlew build
```

The built jar will be in `build/libs/`.

## Notes

- The button is added to all chat lines, including system messages such as join/leave messages and server announcements.
- The copied text is the full line as shown in chat, including the player name prefix.

## How it works

`ChatComponentMixin` hooks into Minecraft's `ChatComponent.addMessage` and appends a blue `(copy)` component with a hover event (`Copy Text`) and a click event (copy to clipboard) to each message.

## License

Released under the CC0-1.0 license. See [LICENSE](LICENSE).
