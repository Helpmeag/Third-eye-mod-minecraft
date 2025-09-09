# Third Eye (Fabric 1.20.1)
Rear-view mini camera HUD for PvP/horror. Optimized for performance.

## Features
- Client-side only HUD window (toggle or hold to peek)
- 30 FPS cap for rear view (configurable)
- Low-res FBO render, upscaled in HUD
- Tiny chunk distance for rear pass
- Particles disabled during rear render (via mixin)
- Place HUD in any corner, custom size & margins

## Build
- Java 17 is required
- Run: `./gradlew build` (or `gradlew.bat build` on Windows)
- Output JAR in `build/libs`

## Install
- Put the JAR into `.minecraft/mods` with Fabric Loader + Fabric API

## Notes
- Mappings and some calls are version-sensitive. If compiling for 1.19.x/1.21.x,
  update `minecraft` + `fabric-api` in `build.gradle` and tweak method names as needed.
- To adjust performance: edit `.minecraft/config/thirdeye.json` after first run.

## Keybinds
- Toggle rear view: `V`
- Hold to peek: `Left Alt`

## Roadmap
- Proper off-axis rear camera with its own Camera instance
- Config UI (via Mod Menu)
- Optional fish-eye shader
