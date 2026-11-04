<h1 align="center">Custom Capes</h1>
<p align="center">
<b>A client-side cape extension for RusherHack.</b>
</p>

## Overview

**Custom Capes** is a RusherHack plugin that lets you wear any cape texture, on the cape and on elytras.
Capes can be local (only you see them) or shared through a public registry, so other players using the plugin see yours too.

> [!IMPORTANT]
> Everything is client-side. Players without this plugin keep seeing your normal cape.
> OptiFine users cannot see these capes either: OptiFine only loads capes from its own servers.

Supported Minecraft versions: **1.21.4**, **1.21.11** and **26.2**.

## Features

- **Dynamic cape loading:** put png files in the capes folder and load them without restarting the game.
- **Elytra sync:** the texture is used on the cape and on elytras.
- **Only for me:** show your selected cape only on yourself (default), or on everyone.
- **Shared capes:** shows the capes registered in the [CustomCapes-Registry](https://github.com/eclipxia/CustomCapes-Registry), see below.

## Commands

- `*cape reload` reloads local capes and the shared registry
- `*cape uuid` prints your UUID (needed to add your cape to the registry)

## Using a local cape

1. After the first launch you have a `rusherhack/capes/` folder.
2. Put your cape texture (png) in it.
3. Run `*cape reload`, then pick the cape in the **CustomCape** module (Render category).

A cape texture is the vanilla cape layout: 64x32, or a multiple of it (128x64 ... 1024x512). The outer face of the cape is the block at the top left (x1, y1, 10x16); elytras use the same texture, with the wing shape cut out by transparency.

## Shared capes

With **Shared Capes** enabled (default), the plugin downloads the registry index (`capes.json`, a public file) and shows each listed player's cape.
A cape png is only downloaded the first time that player is seen. Nothing about the players around you is ever sent anywhere, the plugin only reads the registry.

To get your own cape in the registry, open a pull request on [CustomCapes-Registry](https://github.com/eclipxia/CustomCapes-Registry), the steps are in its README.
The registry URL can be changed in the module settings (https only).

## Installation

1. Download the jar for your Minecraft version from the [Releases](https://github.com/eclipxia/CustomCapes/releases) page (`customcapes-<version>+mc<minecraft>.jar`).
2. Make sure the JVM flag `-Drusherhack.enablePlugins=true` is set in your launcher.
3. Put the jar in your `rusherhack/plugins` folder (the folder next to your Minecraft instance, not `mods`).
4. Launch the game, or run `*reload` if it is already running.

## Building

Shared code lives in `common/`, version specific code (`Compat`, `MixinPlayerInfo`) in `versions/<mc>/`.
Gradle needs Java 25.

- `./gradlew buildAll` builds every version into `build/libs`
- `./gradlew build-1.21.4`, `build-1.21.11` or `build-26.2` builds a single version
- `./gradlew run-<mc>` starts a dev client (needs `rusherhack-loader.jar` in `lib/`; the first launch of a fresh run folder only migrates Rusherhack's folders, start it a second time to load the plugin)

## Credits

Original plugin by FelekDevYT.
