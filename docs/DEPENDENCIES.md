# Minecraft dependency matrix — 2026-10-20

These are the pinned build combinations for mod 1.1.5. Install dependency variants for the Minecraft version actually running. `build/libs` contains the mod only; Fabric API, Mod Menu and YACL remain separate mods.

| Git branch | Compile MC | Supported MC | Java | Fabric API build pin | Mod Menu | YACL | Minimum Loader |
|---|---|---|---:|---|---|---|---|
| 1.20.1 | 1.20 | 1.20 / 1.20.1 | 17 | 0.83.0+1.20 | 7.0.1 | 3.2.0+1.20 | 0.16.13 |
| 1.20.2 | 1.20.2 | 1.20.2 | 17 | 0.89.1+1.20.2 | 8.0.0 | 3.2.0+1.20.2 | 0.16.13 |
| 1.20.3-4 | 1.20.3 | 1.20.3 / 1.20.4 | 17 | 0.96.11+1.20.4 | 9.0.0 | 3.3.2+1.20.4 | 0.16.13 |
| 1.20.5-6 | 1.20.5 | 1.20.5 / 1.20.6 | 21 | 0.100.0+1.20.6 | 10.0.0 | 3.6.6+1.20.6-fabric | 0.16.13 |
| 1.21 | 1.21 | 1.21 | 21 | 0.100.1+1.21 | 11.0.0 | 3.5.0+1.21-fabric | 0.16.13 |
| 1.21.1 | 1.21.1 | 1.21.1 | 21 | 0.102.0+1.21.1 | 11.0.1 | 3.6.2+1.21-fabric | 0.16.13 |
| 1.21.2 | 1.21.2 | 1.21.2 | 21 | 0.106.1+1.21.2 | 12.0.0 | 3.6.1+1.21.2-fabric | 0.16.13 |
| 1.21.3 | 1.21.3 | 1.21.3 | 21 | 0.106.1+1.21.3 | 12.0.0 | 3.6.4+1.21.3-fabric | 0.16.13 |
| 1.21.4 | 1.21.4 | 1.21.4 | 21 | 0.111.0+1.21.4 | 13.0.1 | 3.6.4+1.21.4-fabric | 0.16.13 |
| 1.21.5 | 1.21.5 | 1.21.5 | 21 | 0.119.5+1.21.5 | 14.0.1 | 3.6.6+1.21.5-fabric | 0.16.13 |
| 1.21.6-11 | 1.21.8 | 1.21.8 | 21 | 0.136.1+1.21.8 | 15.0.0 | 3.8.2+1.21.6-fabric | 0.17.0 |
| 26.1 | 26.1 | 26.1.x | 25 | 0.145.4+26.1.1 | 18.0.0 | 3.9.2+26.1-fabric | 0.18.4 |
| 26.2 | 26.2 | 26.2.x | 25 | 0.161.0+26.2 | 20.0.3 | 3.9.7+26.2-fabric | 0.19.2 |
| 26.3 | 26.3 | 26.3.x | 25 | 0.164.0+26.3 | 21.0.0 | 3.9.7+26.3-fabric | 0.19.3 |

Fabric Loader build pin is 0.19.1 for the historical rows through 26.1 and 0.19.5 for 26.2/26.3. Gradle is 9.2.1 through 26.1 and 9.6.0 for 26.2/26.3. Loom is pinned to stable 1.14.10 for mapped versions, 1.15.5 for 26.1 and 1.17.21 for 26.2/26.3. Loom 1.14.10/1.15.5 declare Gradle plugin API 9.2.0; Loom 1.17.21 declares 9.5.0. Build JVM is at least 21; the generated mod classes target the Java version in the table.

## Corrections verified against dependency metadata

- Minecraft 1.20.5/1.20.6 requires Java 21. The previous Java 17 declaration was incorrect. YACL 3.6.6 also requires Fabric API >=0.100.0+1.20.6; the previous 0.97.5 pin was incompatible. API 0.100.0+1.20.6 itself explicitly supports both 1.20.5 and 1.20.6.
- API 0.96.11+1.20.4 explicitly supports both 1.20.3 and 1.20.4, unlike the previous 0.91.1+1.20.3 build pin. YACL 3.3.2+1.20.4 also declares both versions.
- The 1.20/1.20.1 mod uses an API compiled against 1.20. API 0.83.0+1.20 itself supports only 1.20: **1.20.1 users must install a Fabric API variant built for 1.20.1**, rather than copy the 1.20 API JAR. The mod's API minimum is 0.83.0, so compatible newer 1.20.1 variants are accepted. YACL 3.2.0+1.20 and Mod Menu 7.0.1 support both versions.
- YACL 3.8.2 raises the 1.21.8 Loader minimum to 0.17.0.
- YACL 3.2's published POM references unavailable TwelveMonkeys `3.10.0-SNAPSHOT` artifacts. The 1.20/1.20.1 and 1.20.2 build configurations replace those exact requests with the stable `3.10.0` release. These image libraries remain YACL dependencies; they are not bundled into this mod.
- For 26.1, Fabric API 0.145.4+26.1.1 explicitly accepts ~26.1 and raises the Loader minimum to 0.18.4. Its 26.1.1 suffix does not make it incompatible with 26.1. Mod Menu 18.0.0 is a stable release supporting 26.1, 26.1.1 and 26.1.2. YACL 3.9.2 accepts ~26.1.
- The historical branch name `1.21.6-11` is retained, but this update is compiled and declared for **1.21.8 only**. Compatibility with the other versions implied by that branch name has not been established.

Evidence for older rows was read directly from the resolved dependency JARs' `fabric.mod.json` in the local Loom cache. For 26.1, the author-published release JARs and official Maven module metadata were checked:

- [Fabric API metadata source](https://raw.githubusercontent.com/FabricMC/fabric/0.145.4%2B26.1.1/src/main/resources/fabric.mod.json)
- [Mod Menu 18.0.0 metadata](https://raw.githubusercontent.com/TerraformersMC/ModMenu/v18.0.0/src/main/resources/fabric.mod.json)
- [YACL 3.9.2 author-published JAR](https://cdn.modrinth.com/data/1eAoo2KR/versions/h2FGeDHG/yet_another_config_lib_v3-3.9.2%2B26.1-fabric.jar)
- [Loom 1.15.5 module](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.15.5/fabric-loom-1.15.5.module)

Each branch's `docs/BUILD-AUDIT.md` records its build result. A successful build and file-level tests do not substitute for in-game acceptance testing.

## Minecraft 26.2 / 26.3 ports

Resolved release JAR metadata confirms API 0.161.0+26.2 accepts ~26.2- and API 0.164.0+26.3 accepts ~26.3-. Both require Java 25. Mod Menu 20.0.3 requires Loader >=0.19.2, setting the 26.2 minimum; API 0.164.0 sets the 26.3 Loader minimum to 0.19.3. Both selected YACL releases support their respective Minecraft minor version.

Screen state moved into Minecraft.gui in 26.2. These ports use gui.setScreen()/screen(), gui.toastManager(), gui.hud.getChat() and gui.hud.isHidden(). The 26.3 SDL transition moves folder opening to Blaze3D.openPath(Path), matching the vanilla resource-pack selector.

- [Fabric 26.2 migration notes](https://fabricmc.net/2026/06/15/262.html)
- [Fabric 26.3 migration notes](https://fabricmc.net/2026/09/15/263.html)
- [Loom 1.17.21 module](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.17.21/fabric-loom-1.17.21.module)
- [Mod Menu 20.0.3](https://modrinth.com/mod/modmenu/version/iRo7AZXf), [Mod Menu 21.0.0](https://modrinth.com/mod/modmenu/version/kyy7dbrZ)
- [YACL for 26.2](https://modrinth.com/mod/yacl/version/DoR7RYgi), [YACL for 26.3](https://modrinth.com/mod/yacl/version/s9SjoFu1)

The owner reports manual testing passed on Minecraft 1.20.4 and 1.21.8. All other rows, including 26.2/26.3, have build and file-level regression verification only.
