# NeoForge editions — 2026-10-21

Mod 1.1.5 is available in three native NeoForge branches. All three code branches were pushed to GitHub and their remote commit IDs were read back and matched. The existing Fabric branches are unchanged.

| Minecraft (exact) | Git branch | NeoForge | YACL NeoForge | JAR bytes | Verified code commit |
|---|---|---|---|---:|---|
| 26.1 | [neofor-26.1](https://github.com/ZHENXUWEI/rss-automessage/tree/neofor-26.1) | 26.1.0.19-beta | 3.9.7+26.1-neoforge | 114,417 | 5ebb167 |
| 26.2 | [neofor-26.2](https://github.com/ZHENXUWEI/rss-automessage/tree/neofor-26.2) | 26.2.0.93 | 3.9.7+26.2-neoforge | 114,536 | f3c5ca5 |
| 26.3 | [neofor-26.3](https://github.com/ZHENXUWEI/rss-automessage/tree/neofor-26.3) | 26.3.0.90 | 3.9.7+26.3-neoforge | 114,497 | 766870a |

All editions require Java 25. NeoForge 26.1.0.19-beta is the official MDK pin for the original Minecraft 26.1 release; it remains beta. These JARs target exactly 26.1, 26.2 and 26.3 respectively, not the later 26.1.1/26.1.2 releases. Install the matching external YACL NeoForge variant: this mod deliberately pins its full version to prevent accepting a wrong Minecraft/loader variant.

Each edition passed a clean build with 16 TXT-library checks and 11 message-editor checks, plus 20 integration assertions in an actual isolated Minecraft client. The client probe uses the native Mods configuration extension and real YACL callbacks to check file selection/reload, empty-file protection, async read cancellation, countdown reset and settings/file persistence. It does not test multiplayer delivery, scheduled messages in a world, HUD appearance in a world or mouse interaction with the native Mods list.

The optional integration probe is reproducible with `./gradlew --init-script scripts/neoforge-smoke.init.gradle runClient` under Java 25. It creates a fresh run/qa-neofor-* profile with sending disabled, runs its checks, writes neo-smoke-result.json and closes its own client. The probe is not part of ordinary CI builds or released binary/source JARs.

Configuration uses NeoForge's Mods screen. The native registration ID is rss_automessage; resource namespace, rss-automessage/ TXT directory and config/rss-automessage.json retain their existing names. Fabric API and Mod Menu are not dependencies. No test code, promotional image or nested external mod JARs are bundled.

The 26.2 port adopts Minecraft.gui screen/toast/HUD access; the 26.3 port additionally opens directories with Blaze3D.openPath(Path). The original YACL 26.3 JAR was used unchanged and successfully loaded with the SDL Windows backend. Although its upstream dependency table/range is malformed, this mod supplies its own correct Minecraft/NeoForge bounds and exact YACL variant requirement.

The sibling publish/1.1.5 directory contains the three neoforge-named release JARs. [NEOFORGE-BUILD.csv](NEOFORGE-BUILD.csv) records their verified sizes, hashes, dependencies and code commits. [BUILD-AUDIT.md](BUILD-AUDIT.md) records this branch's validation. SHA256 values describe the verified local artifacts; they are not a claim of byte-identical builds across different OS/line-ending configurations.
