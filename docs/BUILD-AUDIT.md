# Dependency audit — branch 1.21.5

| Component | Version |
|---|---|
| Supported Minecraft | 1.21.5 |
| Compile Minecraft | 1.21.5 |
| Java | 21 |
| Fabric Loader (build / runtime minimum) | 0.19.1 / 0.16.13 |
| Fabric API | 0.119.5+1.21.5 |
| Mod Menu | 14.0.1 |
| YACL | 3.6.6+1.21.5-fabric |
| Loom / Gradle | 1.14.10 / 9.2.1 |

Dependency variants must match the running Minecraft version. Build pins are verified combinations; the runtime metadata specifies tested minimums. The root TXT folder and YACL selector are ported from commit 91e03df. Promotional icon720-450.png remains on disk and is excluded from binary and source archives.

Java-only TXT and message-editor regression checks run under Gradle check. A successful build verifies compilation and packaging, not in-game interaction.

Verified locally on 2026-10-20: clean Gradle build; 16 TXT checks and 11 message-editor checks passed. Archive inspected; JAR size 128952 bytes. In-game interactions remain untested.
