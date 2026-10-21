# NeoForge build audit — 2026-10-21

| Component | Pinned version |
|---|---|
| Git branch | neofor-26.1 |
| Minecraft | exactly 26.1 |
| Java | 25 |
| NeoForge | 26.1.0.19-beta |
| YACL | dev.isxander:yet-another-config-lib:3.9.7+26.1-neoforge |
| ModDevGradle / Gradle | 2.0.148 / 9.2.1 |
| Mod version / NeoForge registration ID | 1.1.5 / rss_automessage |

The official base-Minecraft-26.1 MDK still pins a beta NeoForge release. This build targets 26.1 exactly; it does not declare compatibility with 26.1.1/26.1.2. Existing resource namespace, TXT directory and config file remain rss-automessage. Use the native NeoForge Mods configuration button and install the matching NeoForge YACL JAR separately.

## Verification

Clean Gradle build passed using JDK 25, including 16 TXT-library and 11 message-editor checks. Actual isolated client startup passed with 20 integration checks: native Mods configuration factory, YACL settings and TXT selector, initial empty directory, message/command import, switching, explicit reload, empty-file protection, leaving during an async read, rebuilt-settings save and filename/message persistence after config reload. Automatic sending/replies were disabled in the isolated profile. World/server chat, scheduled delivery and in-world HUD rendering have not been manually tested.

Run the optional integration probe with `./gradlew --init-script scripts/neoforge-smoke.init.gradle runClient` (Java 25). It creates a new isolated run/qa-neofor-* directory, prints NEOFORGE_SMOKE_PASS checks=20 and writes neo-smoke-result.json there, then closes its own client. The probe source set is only added by this script; it is excluded from release binary/source archives and ordinary CI builds.

Binary JAR: 114417 bytes; SHA256 5edbf039382678b4f576e4d58c24fe221e42f559452aee227925334cc4a61c82. Both binary/source archives were inspected. NeoForge TOML and Java major 69 are correct; no Fabric metadata/API references, Mod Menu entry point, QA/test classes, nested third-party JARs or promotional image. Original icon720-450.png remains on disk. Packaged Chinese metadata was compared against UTF-8 source and matched.

A local-only download bridge was needed because this machine intermittently resolves the NeoForge Maven host incorrectly. It downloaded unmodified bytes over certificate-validated official HTTPS; release build files retain the official HTTPS repositories. This workaround is outside version control.

## Primary references

- [Official 26.1 MDK](https://github.com/NeoForgeMDKs/MDK-26.1-ModDevGradle)
- [NeoForge client events](https://github.com/neoforged/NeoForge/tree/26.1.x/src/client/java/net/neoforged/neoforge/client/event)
- [NeoForge config screen extension](https://github.com/neoforged/NeoForge/blob/26.1.x/src/client/java/net/neoforged/neoforge/client/gui/IConfigScreenFactory.java)
- [YACL author Maven](https://maven.isxander.dev/releases/dev/isxander/yet-another-config-lib/3.9.7+26.1-neoforge/)

The YACL dependency now pins the exact verified Minecraft/NeoForge variant. The client smoke probe also waits for asynchronous read-pool quiescence before checking that leaving the selector cancels a read. These shared improvements were reverified with a fresh clean build and all 20 actual-client checks.
