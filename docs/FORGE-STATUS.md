# Forge 26.1–26.3 status

Verified and pushed to GitHub on 2026-10-21. All targets use AutoMessage 1.1.5, Java 25, Gradle 9.7.1 and ForgeGradle 7.0.29.

| Minecraft | Branch | Forge | Separate YACL Reforged dependency | AutoMessage bytes | Unit checks | Actual client checks |
| --- | --- | --- | --- | --- | --- | --- |
| 26.1 | forge-26.1 | 62.0.9 | 3.9.7+26.1-26.1.2 | 114680 | 27 | 20 |
| 26.2 | forge-26.2 | 65.1.5 | 3.9.7+26.2 | 114793 | 27 | 20 |
| 26.3 | forge-26.3 | 66.0.13 | 3.9.7+26.3 | 114762 | 27 | 20 |

- forge-26.1: source `afc9e404ee0de80a1e251f3fd02163178ad479f0`, binary SHA-256 `cc1e4b0c0cef48566dc62dec3e96e0f9231410a8b43d55ea53657ab50f6c41df`.
- forge-26.2: source `77d62938aaeaf387c81b6ea2b37fd209f686b04c`, binary SHA-256 `4f40d6a080ca1f1efce367bf9ab394cf42ec49ef018d7029242f096a11654789`.
- forge-26.3: source `44749a512ca95db191059f2efa7c10e7a6c98f85`, binary SHA-256 `a89017a1edf31130aca96b3a08589476ffb7efb9c4de839d69f0446b75299ad4`.

Source commits identify the implementation used for the verified JARs; subsequent audit/CI documentation commits do not change packaged code or resources. Full dependency hashes and provenance are in [FORGE-BUILD.csv](FORGE-BUILD.csv) and each dependency's provenance.json.

The client probes invoke the native Forge configuration extension and real YACL action callbacks in isolated game profiles. They verify TXT directory creation, import/switch/reload, empty-file preservation, cancellation after leaving the page, countdown reset, saving and persisted filename/message data. They passed with the final client resource formats (84, 88 and 97.1). Multiplayer message delivery and HUD appearance inside a world have not been manually tested.

Install the matching AutoMessage JAR and `yacl-reforged` JAR together. YACL Reforged is an unofficial Forge port built from the author's unchanged pinned source because no public binary was available. Corresponding source JARs, original source archives, licenses and provenance are supplied beside the dependency. See [preparation and build instructions](FORGE-BUILD.md).

AutoMessage contains no embedded dependencies, disposable QA classes or promotional image. Its approximately 112 KiB size includes the actual icon and translations; the roughly 1.13 MiB YACL dependency remains separate with its original nine bundled libraries.
