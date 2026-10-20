# Build and dependency audit — 2026-10-20

## Minecraft 1.21.8 (branch 1.21.6-11)

| Component | Pinned build version | Runtime requirement |
|---|---|---|
| Minecraft | 1.21.8 | exactly 1.21.8 |
| Java | 21 | 21 or newer |
| Gradle | 9.2.1 | build only |
| Loom | 1.14.10 | build only; replaces a moving SNAPSHOT |
| Fabric Loader | 0.19.1 | 0.17.0 or newer (required by YACL) |
| Fabric API | 0.136.1+1.21.8 | 0.136.1 or newer, for Minecraft 1.21.8 |
| Mod Menu | 15.0.0 | 15.0.0 or newer, matching Minecraft |
| YACL | 3.8.2+1.21.6-fabric | 3.8.2 or newer, matching Minecraft |

Versions were checked against the resolved dependencies' `fabric.mod.json`, rather than inferred from artifact filenames. YACL and Mod Menu both declare `minecraft: ~1.21.6`, which covers 1.21.8. YACL requires Loader >=0.17.0. The selected Fabric API declares `minecraft: >=1.21.8- <1.21.9-` and Java >=21.

Sources: [Fabric Maven](https://maven.fabricmc.net/), [Mod Menu Maven](https://maven.terraformersmc.com/), [YACL Maven](https://maven.isxander.dev/releases/). These libraries remain external dependencies; they are not bundled in this mod.

## JAR size

Before exclusion: **258,600 bytes**. The promotional image `assets/rss-automessage/icon720-450.png` occupied **131,394 compressed bytes**, and the registered mod icon `icon.png` occupied **64,930 compressed bytes**. Together they accounted for about 76% of the archive.

The promotional image is not referenced by mod metadata or code. It is retained on disk but excluded from the main resource source set, so it is absent from both binary and source JARs. The normal mod icon is retained.

After exclusion and dependency declaration corrections: **127,039 bytes** (about 124 KiB), a reduction of **131,561 bytes / 50.9%**. Remaining compressed contents are approximately 65 KB of icon and 62 KB of code, translations and ZIP metadata. No Fabric API, Mod Menu or YACL JAR is embedded.

Verification: clean Gradle build, 16 real-file TXT checks, 11 message-editor regression checks, archive inspection, and `git diff --check`. The owner reports that manual in-game testing passed on 1.21.8 (and separately on 1.20.4). This does not establish in-game compatibility for other versions.
