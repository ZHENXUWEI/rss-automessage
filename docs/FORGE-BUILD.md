# Forge build and dependency preparation

The Forge branches use the YACL API through [SvyatON's YACL Reforged](https://github.com/SvyatON/YACL-Reforged), an unofficial Forge port. As checked on 2026-10-21, the author's GitHub releases contain no downloadable binaries and the linked Modrinth project returns HTTP 404. These branches therefore build the original tagged sources and publish only to a repository-local Maven directory.

| Branch | Minecraft | Forge | YACL Reforged | Gradle wrapper |
| --- | --- | --- | --- | --- |
| forge-26.1 | 26.1 | 62.0.9 | 3.9.7+26.1-26.1.2 | 9.7.1 |
| forge-26.2 | 26.2 | 65.1.5 | 3.9.7+26.2 | 9.7.1 |
| forge-26.3 | 26.3 | 66.0.13 | 3.9.7+26.3 | 9.7.1 |

All targets use Java 25 and ForgeGradle 7.0.29, matching the dependency's original build. That build also retains its JarJar 0.2.3, Stonecutter 0.9.8 and Kotlin 2.3.0 pins. Java API consumers do not require a separate Kotlin mod.

## Reproduce a build

Install Python 3.11+ and JDK 25, set `JAVA_HOME`, then run from the repository root:

```powershell
python scripts/prepare-yacl.py
.\gradlew.bat --gradle-user-home .gradle-agent clean build --no-daemon --console=plain
```

On Linux use `python3` and `./gradlew`. The preparation command downloads the fixed upstream tag object, checks the pinned source ZIP's SHA-256, leaves upstream source files unchanged, and invokes the author's build plus Maven publication. It starts the upstream Gradle Wrapper JAR directly with Java, avoiding the upstream POSIX launcher's CRLF shebang on Linux. It saves dependency binary/source JARs, POM and provenance under `.gradle-agent/yacl-repository/dev/svyaton/yacl-reforged/<version>/`. The compiled dependency is separate from AutoMessage.

| Minecraft | Author tag | Source commit |
| --- | --- | --- |
| 26.1 | F-3.9.7+26.1-26.1.2 | ab2bc9ba56f31c5e0c4fa4a9a0c578e741268b22 |
| 26.2 | F-3.9.7+26.2 | 0d92aeb8865ca08fa401313bc0a918cdf871f0b2 |
| 26.3 | F-3.9.7+26.3 | a70ec655b25f68b33885889a0fe26bc1dd4fca65 |

YACL Reforged is LGPL-3.0-or-later. Preserve the upstream binary's embedded license, notices and third-party licenses when distributing it; provide its corresponding source JAR and original source archive alongside the dependency. AutoMessage remains MIT licensed.

CI artifact uploads include the separate dependency binary, source JAR, original source ZIP and provenance as well as AutoMessage. Install only the two runtime mod JARs; the source artifacts are provided for inspection and redistribution.

## Actual client integration probe

```powershell
.\gradlew.bat --gradle-user-home .gradle-agent --init-script scripts/forge-smoke.init.gradle runSmokeClient --no-daemon --console=plain
```

The optional init script creates a separate development mod and a fresh `run/qa-forge-*` profile, with scheduled messages and replies disabled. The probe uses the actual Forge configuration extension and YACL button callbacks. Success prints `FORGE_SMOKE_PASS checks=20` and writes `forge-smoke-result.json` in that profile. It then closes its own client. QA classes and metadata are excluded from the normal binary and source JARs.

To reuse an already complete Minecraft asset cache for a local probe, set `AUTOMESSAGE_ASSETS` to its absolute path. Without this optional environment variable, the official launcher handles asset downloads normally.

The probe covers initial empty directory creation, TXT import/switch/reload, empty text preservation, cancellation when leaving the page, countdown reset, saving and persisted filename/message data. Multiplayer delivery and visible HUD behavior inside a world require separate gameplay testing.
