# Version update status — 2026-10-20

All 12 historical local version branches and the new 26.2/26.3 branches have mod 1.1.5 TXT selection updates and passing Gradle builds. Each build ran 16 TXT checks and 11 message-editor checks. The owner reports manual testing passed on Minecraft 1.20.4 and 1.21.8. Other versions, including 26.2/26.3, have not been tested in game.

GitHub push completed on 2026-10-20 after the owner restored authentication. All 14 remote branch heads were read back with git ls-remote and matched the corresponding local commits. Existing branches were fast-forwarded; 26.2 and 26.3 were created as new remote branches. No branch was force-pushed.

| Local branch | GitHub branch | Verified code commit | JAR bytes | Checks |
|---|---|---|---:|---:|
| 1.21.6-11 | 1.21.6-11 | 91e03df | 127039 | 27 |
| 1.21.5 | 1.21.5 | 23354f0 | 128952 | 27 |
| 1.21.4 | 1.21.4 | f1ef8b9 | 128952 | 27 |
| 1.21.3 | 1.21.3 | 1c6ddca | 128953 | 27 |
| 1.21.2 | 1.21.2 | 3e702c2 | 128957 | 27 |
| 1.21.1 | 1.21.1 | 55612a2 | 128950 | 27 |
| 1.21 | 1.21 | d63bcfa | 128957 | 27 |
| 1.20.5-6 | 1.20.5-6 | f2fcfe3 | 118964 | 27 |
| 1.20.3-4 | 1.20.3-4 | e44aa3c | 119369 | 27 |
| 1.20.2 | 1.20.2 | 719a8d4 | 119371 | 27 |
| 1.20.1 | 1.20.1 | 3fad330 | 119370 | 27 |
| 26.1 | 26.1 | 02009c5 | 116872 | 27 |
| 26.2 | 26.2 | e1268d9 | 116236 | 27 |
| 26.3 | 26.3 | 6afc3d8 | 116198 | 27 |

Artifacts: sibling publish/1.1.5 directory. build-verification.csv records SHA-256, exact MC constraints and Java levels for all fourteen JARs. All archives were inspected: TXT selection classes are present, promotional icon720-450.png is absent, and no test classes or third-party dependency JARs are bundled.

See [DEPENDENCIES.md](DEPENDENCIES.md) for pinned dependency combinations and compatibility corrections, and [BUILD-AUDIT.md](BUILD-AUDIT.md) for the original size breakdown. The old 1.21.6-11 branch name is retained; this update on that branch targets 1.21.8 only.

The 26.2 port migrates screen/Toast/Chat/HUD access to the new GUI structure. The 26.3 port additionally uses Blaze3D.openPath(Path), matching the vanilla resource-pack selector. Both use Java 25, stable Loader 0.19.5, Loom 1.17.21 and Gradle 9.6.0. Their respective dependency metadata and actual Minecraft API signatures were checked, and compatibility review found no important issues.
