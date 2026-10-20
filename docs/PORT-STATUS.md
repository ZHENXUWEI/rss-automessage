# Version update status — 2026-10-20

All 12 existing local version branches have mod 1.1.5 TXT selection updates and passing Gradle builds. Each build ran 16 TXT checks and 11 message-editor checks. In-game interactions have not been manually tested.

GitHub push is pending: stored authentication was rejected. The owner requested completing local updates first, then pushing together after login is restored. No branch was force-pushed.

| Local branch | Existing GitHub branch | Feature commit | JAR bytes | Checks |
|---|---|---|---:|---:|
| 1.21.6-11 | 1.21.6-11 | 91e03df | 127039 | 27 |
| 1.21.5 | 1.21.5 | 23354f0 | 128952 | 27 |
| 1.21.4 | 1.21.4 | f1ef8b9 | 128952 | 27 |
| 1.21.3 | 1.21.2-3 | 1c6ddca | 128953 | 27 |
| 1.21.2 | 1.21.2 | 3e702c2 | 128957 | 27 |
| 1.21.1 | 1.21.1 | 55612a2 | 128950 | 27 |
| 1.21 | 1.21 | d63bcfa | 128957 | 27 |
| 1.20.5-6 | 1.20.5-6 | f2fcfe3 | 118964 | 27 |
| 1.20.3-4 | 1.20.3-4 | e44aa3c | 119369 | 27 |
| 1.20.2 | 1.20.2 | 719a8d4 | 119371 | 27 |
| 1.20.1 | 1.20.1 | 3fad330 | 119370 | 27 |
| 26.1 | 26.1 | dde6a7c | 116586 | 27 |

Artifacts: sibling publish/1.1.5 directory. build-verification.csv records SHA-256, exact MC constraints and Java levels for all twelve JARs. All archives were inspected: TXT selection classes are present, promotional icon720-450.png is absent, and no test classes or third-party dependency JARs are bundled.

See [DEPENDENCIES.md](DEPENDENCIES.md) for pinned dependency combinations and compatibility corrections, and [BUILD-AUDIT.md](BUILD-AUDIT.md) for the original size breakdown. The old 1.21.6-11 branch name is retained; this update on that branch targets 1.21.8 only.
