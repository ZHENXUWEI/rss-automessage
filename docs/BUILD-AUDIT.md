# Dependency audit — branch 26.3

| Component | Version |
|---|---|
| Supported Minecraft | >=26.3 <26.4 |
| Compile Minecraft | 26.3 |
| Java | 25 |
| Fabric Loader (build / dependency-required minimum) | 0.19.5 / 0.19.3 |
| Fabric API | 0.164.0+26.3 |
| Mod Menu | 21.0.0 |
| YACL | 3.9.7+26.3-fabric |
| Loom / Gradle | 1.17.21 / 9.6.0 |

The resolved dependency JARs' fabric.mod.json were inspected on 2026-10-20. Fabric API sets the Loader minimum to 0.19.3. Install dependency variants for Minecraft 26.3; dependencies are not bundled into this mod.

Ported from 26.2 commit e1268d9. Screen switching/current-screen checks use Minecraft.gui.setScreen()/screen(); toast access uses gui.toastManager(); chat and hidden-HUD access use gui.hud. Async TXT reads still check that the selector is current before applying messages. Main-menu feedback uses SystemToast and in-world feedback uses addClientSystemMessage. Opening the TXT folder uses Blaze3D.openPath(Path), the same API used by the 26.3 vanilla resource-pack screen after the SDL transition.

Verified locally on 2026-10-20: clean Gradle build succeeded with JDK 25; 16 TXT checks and 11 message-editor checks passed. Binary archive size: 116198 bytes. Java class major 69, TXT selector classes present, no test classes, nested dependency JARs or promotional icon720-450.png. The original promotional image remains on disk.

Minecraft 26.3 has not been tested in game. The owner reports manual testing passed on 1.20.4 and 1.21.8; other versions have build verification only.

Primary compatibility references:
- [Fabric 26.3 migration notes](https://fabricmc.net/2026/09/15/263.html)
- [Mod Menu 21.0.0](https://modrinth.com/mod/modmenu/version/kyy7dbrZ)
- [YACL 3.9.7 for 26.3](https://modrinth.com/mod/yacl/version/s9SjoFu1)
- [Loom 1.17.21 module metadata](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.17.21/fabric-loom-1.17.21.module)
