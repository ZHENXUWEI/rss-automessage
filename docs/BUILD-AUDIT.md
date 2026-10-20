# Dependency audit — branch 26.1

| Component | Version |
|---|---|
| Supported Minecraft | >=26.1 <26.2 |
| Compile Minecraft | 26.1 |
| Java | 25 |
| Fabric Loader (build / runtime minimum) | 0.19.1 / 0.18.4 |
| Fabric API | 0.145.4+26.1.1 |
| Mod Menu | 18.0.0 |
| YACL | 3.9.2+26.1-fabric |
| Loom / Gradle | 1.15.5 / 9.2.1 |

Dependency variants must match the running Minecraft version. Build pins are verified combinations; the runtime metadata specifies tested minimums. The root TXT folder and YACL selector are ported from commit 91e03df. Promotional icon720-450.png remains on disk and is excluded from binary and source archives.

Java-only TXT and message-editor regression checks run under Gradle check. A successful build verifies compilation and packaging, not in-game interaction.

Verified locally on 2026-10-20: clean Gradle build; 16 TXT checks and 11 message-editor checks passed. Archive inspected; JAR size 116872 bytes. In-game interactions remain untested.

Main-menu legacy file-operation feedback uses a native SystemToast; in-world feedback uses addClientSystemMessage. This avoids ChatListener dropping messages without a player.
