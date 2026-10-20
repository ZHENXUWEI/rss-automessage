# Dependency audit — branch 26.2

| Component | Version |
|---|---|
| Supported Minecraft | >=26.2 <26.3 |
| Compile Minecraft | 26.2 |
| Java | 25 |
| Fabric Loader (build / dependency-required minimum) | 0.19.5 / 0.19.2 |
| Fabric API | 0.161.0+26.2 |
| Mod Menu | 20.0.3 |
| YACL | 3.9.7+26.2-fabric |
| Loom / Gradle | 1.17.21 / 9.6.0 |

The resolved dependency JARs' fabric.mod.json were inspected on 2026-10-20. Mod Menu sets the Loader minimum to 0.19.2. Install dependency variants for Minecraft 26.2; dependencies are not bundled into this mod.

Ported from 26.1 commit 02009c5. Screen switching/current-screen checks use Minecraft.gui.setScreen()/screen(); toast access uses gui.toastManager(); chat and hidden-HUD access use gui.hud. Async TXT reads still check that the selector is current before applying messages. Main-menu feedback uses SystemToast and in-world feedback uses addClientSystemMessage.

Verified locally on 2026-10-20: clean Gradle build succeeded with JDK 25; 16 TXT checks and 11 message-editor checks passed. Binary archive size: 116236 bytes. Java class major 69, TXT selector classes present, no test classes, nested dependency JARs or promotional icon720-450.png. The original promotional image remains on disk.

Minecraft 26.2 has not been tested in game. The owner reports manual testing passed on 1.20.4 and 1.21.8; other versions have build verification only.

Primary compatibility references:
- [Fabric 26.2 migration notes](https://fabricmc.net/2026/06/15/262.html)
- [Mod Menu 20.0.3](https://modrinth.com/mod/modmenu/version/iRo7AZXf)
- [YACL 3.9.7 for 26.2](https://modrinth.com/mod/yacl/version/DoR7RYgi)
- [Loom 1.17.21 module metadata](https://maven.fabricmc.net/net/fabricmc/fabric-loom/1.17.21/fabric-loom-1.17.21.module)
