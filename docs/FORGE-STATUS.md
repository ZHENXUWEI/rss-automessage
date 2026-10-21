# Forge 26.1–26.3 status

Self-contained releases verified on 2026-10-21. All targets use AutoMessage 1.1.5, Java 25, Gradle 9.7.1, ForgeGradle 7.0.29 and JarJar 0.2.3. Players install only one AutoMessage JAR; YACL is included.

| Minecraft | Branch | Forge | Included YACL Reforged | AutoMessage bytes | Unit checks | Actual client checks |
| --- | --- | --- | --- | --- | --- | --- |
| 26.1 | forge-26.1 | 62.0.9 | 3.9.7+26.1-26.1.2 | 1236558 | 27 | 22 |
| 26.2 | forge-26.2 | 65.1.5 | 3.9.7+26.2 | 1237108 | 27 | 22 |
| 26.3 | forge-26.3 | 66.0.13 | 3.9.7+26.3 | 1237193 | 27 | 22 |

- forge-26.1: source `f497699971573ab14243f222aca7ef8cb5954d99`, binary SHA-256 `20ac8e2fed1f171927aa353cbf6baefefa3f5a403d644094f58b14c4b913addf`.
- forge-26.2: source `a7a47a867e566f3c68019b18228634184eb17ba9`, binary SHA-256 `748a6f676852dbdcf19a325f3ee0f7194852c2e360ce1bfef557676b42b83292`.
- forge-26.3: source `f5a8009a9ca7da888ed5463fdfcfabdddfc74516`, binary SHA-256 `e76a7f5a689e6436ee600174010a7be0f15b53af2520cab56e3372d92c5e8a36`.

Source commits identify the implementation used for the verified JARs; subsequent audit documentation commits do not change packaged code or resources. Full embedded-dependency hashes and provenance are in [FORGE-BUILD.csv](FORGE-BUILD.csv) and each dependency's provenance.json.

The client probes launch the actual release JAR with no standalone YACL or main development output. Two origin assertions confirm AutoMessage loads from the release and YACL from its nested JAR; 20 more assertions exercise native Forge configuration and actual YACL callbacks for TXT creation/import/switch/reload, empty-file preservation, cancellation, countdown reset, saving and persistence. Client resource formats are 84, 88 and 97.1. Multiplayer message delivery and HUD appearance inside a world have not been manually tested.

Install the matching AutoMessage binary only. The full unchanged YACL Reforged JAR is embedded through Forge Jar-in-Jar, with its original nine child libraries and all legal files. Forge's own optional MixinExtras is requested by a metadata constraint (0.5.3 on 26.1; 0.5.4 on 26.2/26.3), without embedding another copy. Corresponding source JARs, complete original project source archives, licenses and provenance are supplied for inspection and redistribution. See [preparation and build instructions](FORGE-BUILD.md).

The release is approximately 1.24 MB because it now includes YACL and its existing libraries. No disposable QA classes or promotional image are included. The thin intermediate stays outside build/libs and is not distributed.
