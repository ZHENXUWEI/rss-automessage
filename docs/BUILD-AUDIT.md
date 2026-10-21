# NeoForge build audit — 2026-10-21

| Component | Pinned version |
|---|---|
| Git branch | neofor-26.2 |
| Minecraft | exactly 26.2 |
| Java | 25 |
| NeoForge | 26.2.0.93 |
| YACL | dev.isxander:yet-another-config-lib:3.9.7+26.2-neoforge |
| ModDevGradle / Gradle | 2.0.148 / 9.2.1 |
| Mod version / NeoForge registration ID | 1.1.5 / rss_automessage |

This build uses the stable official Minecraft 26.2 MDK loader pin and targets Minecraft 26.2 exactly. Existing resource namespace, TXT directory and config file remain rss-automessage. Use the native NeoForge Mods configuration button and install the matching NeoForge YACL JAR separately.

## Verification

Clean Gradle build passed using JDK 25, including 16 TXT-library and 11 message-editor checks. Actual isolated client startup passed with 20 integration checks: native Mods configuration factory, YACL settings and TXT selector, initial empty directory, message/command import, switching, explicit reload, empty-file protection, leaving during an async read, rebuilt-settings save and filename/message persistence after config reload. Automatic sending/replies were disabled in the isolated profile. World/server chat, scheduled delivery and in-world HUD rendering have not been manually tested.

Run the optional integration probe with `./gradlew --init-script scripts/neoforge-smoke.init.gradle runClient` (Java 25). It creates a new isolated run/qa-neofor-* directory, prints NEOFORGE_SMOKE_PASS checks=20 and writes neo-smoke-result.json there, then closes its own client. The probe source set is only added by this script; it is excluded from release binary/source archives and ordinary CI builds.

Binary JAR: 114536 bytes; SHA256 20d26b72c7f933afb63482245db614451500635355ba131b7ce09c6be8b3936d. Both binary/source archives were inspected. NeoForge TOML and Java major 69 are correct; no Fabric metadata/API references, Mod Menu entry point, QA/test classes, nested third-party JARs or promotional image. Original icon720-450.png remains on disk. Packaged Chinese metadata was compared against UTF-8 source and matched.

A local-only download bridge was needed because this machine intermittently resolves the NeoForge Maven host incorrectly. It downloaded unmodified bytes over certificate-validated official HTTPS; release build files retain the official HTTPS repositories. This workaround is outside version control.

## Primary references

- [Official 26.2 MDK](https://github.com/NeoForgeMDKs/MDK-26.2-ModDevGradle)
- [NeoForge client events](https://github.com/neoforged/NeoForge/tree/26.2.x/src/client/java/net/neoforged/neoforge/client/event)
- [NeoForge config screen extension](https://github.com/neoforged/NeoForge/blob/26.2.x/src/client/java/net/neoforged/neoforge/client/gui/IConfigScreenFactory.java)
- [YACL author Maven](https://maven.isxander.dev/releases/dev/isxander/yet-another-config-lib/3.9.7+26.2-neoforge/)

The YACL dependency is constrained to the exact verified Minecraft/NeoForge variant. This prevents accidentally accepting another Minecraft variant when upstream dependency-table metadata is malformed. Screen, toast, chat and hidden-HUD access use the 26.2 Minecraft.gui structure. The smoke probe waits for async read-pool quiescence before checking cancellation.
