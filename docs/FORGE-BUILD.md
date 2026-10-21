# Forge build and dependency preparation

The Forge branches include [SvyatON's YACL Reforged](https://github.com/SvyatON/YACL-Reforged), an unofficial Forge port, inside the AutoMessage release JAR. Players install one AutoMessage JAR and do not download YACL separately. Since no public YACL Reforged binary was available on 2026-10-21, the build prepares the original pinned sources in a repository-local Maven directory, then embeds the complete unchanged dependency through official Forge Jar-in-Jar.

| Branch | Minecraft | Forge | YACL Reforged | Gradle wrapper |
| --- | --- | --- | --- | --- |
| forge-26.1 | 26.1 | 62.0.9 | 3.9.7+26.1-26.1.2 | 9.7.1 |
| forge-26.2 | 26.2 | 65.1.5 | 3.9.7+26.2 | 9.7.1 |
| forge-26.3 | 26.3 | 66.0.13 | 3.9.7+26.3 | 9.7.1 |

All targets use Java 25, ForgeGradle 7.0.29 and JarJar 0.2.3. The dependency's original build also retains Stonecutter 0.9.8 and Kotlin 2.3.0. Java API consumers do not require a separate Kotlin mod. The outer JarJar metadata requests Forge's existing MixinExtras library explicitly because Forge's automatic mixin detection only inspects root mods; no extra MixinExtras file is embedded or required from the player.

## Reproduce a build

Install Python 3.11+ and JDK 25, set `JAVA_HOME`, then run from the repository root:

```powershell
python scripts/prepare-yacl.py
.\gradlew.bat --gradle-user-home .gradle-agent clean build --no-daemon --console=plain
python scripts/verify-bundled-yacl.py
```

On Linux use `python3` and `./gradlew`. The preparation command downloads the fixed upstream tag object, checks the pinned source ZIP's SHA-256, leaves upstream source files unchanged, and invokes the author's build plus Maven publication. It starts the upstream Gradle Wrapper JAR directly with Java, avoiding the upstream POSIX launcher's CRLF shebang on Linux. It saves build dependencies and provenance under `.gradle-agent/yacl-repository/dev/svyaton/yacl-reforged/<version>/`. JarJar embeds the unchanged YACL binary in the unclassified `build/libs/rss-automessage-forge-1.1.5.jar`. The thin intermediate stays under `build/intermediates/` and is not a release artifact.

| Minecraft | Author tag | Source commit |
| --- | --- | --- |
| 26.1 | F-3.9.7+26.1-26.1.2 | ab2bc9ba56f31c5e0c4fa4a9a0c578e741268b22 |
| 26.2 | F-3.9.7+26.2 | 0d92aeb8865ca08fa401313bc0a918cdf871f0b2 |
| 26.3 | F-3.9.7+26.3 | a70ec655b25f68b33885889a0fe26bc1dd4fca65 |

YACL Reforged is LGPL-3.0-or-later. Its unchanged nested binary retains the original licenses, notices and nine bundled libraries. The outer mod includes `META-INF/THIRD-PARTY-NOTICES.md`. AutoMessage's own code remains MIT licensed. Corresponding source JARs, complete original project source archives, licenses and provenance accompany the release; the source JAR alone is not the complete build source. To inspect or replace the library, use the nested path in `META-INF/jarjar/metadata.json` and retain its expected mod ID/version when building a compatible replacement.

CI verifies that the final release JAR contains the exact prepared YACL bytes, all original nested libraries and legal files. Artifact uploads include the single installable AutoMessage JAR, AutoMessage sources, dependency source JARs, complete original source ZIPs and provenance. Only the AutoMessage binary belongs in `mods/`.

## Actual client integration probe

```powershell
.\gradlew.bat --gradle-user-home .gradle-agent --init-script scripts/forge-smoke.init.gradle runSmokeClient --no-daemon --console=plain
```

The optional init script creates a separate QA mod and a fresh `run/qa-forge-*` profile, with scheduled messages and replies disabled. It loads the actual release JAR while excluding the standalone YACL dependency and main development output. Two additional origin checks establish that AutoMessage and YACL load from packaged JARs, followed by the existing 20 configuration/TXT checks. Success prints `FORGE_SMOKE_PASS checks=22` and writes `forge-smoke-result.json` in that profile. It then closes its own client. QA classes and metadata are excluded from release archives.

To reuse an already complete Minecraft asset cache for a local probe, set `AUTOMESSAGE_ASSETS` to its absolute path. Without this optional environment variable, the official launcher handles asset downloads normally.

The probe covers initial empty directory creation, TXT import/switch/reload, empty text preservation, cancellation when leaving the page, countdown reset, saving and persisted filename/message data. Multiplayer delivery and visible HUD behavior inside a world require separate gameplay testing.
