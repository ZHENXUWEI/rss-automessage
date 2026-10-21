# Forge 26.2 build audit

Verified 2026-10-21 using Java 25, Gradle 9.7.1 and ForgeGradle 7.0.29.

- Minecraft: 26.2; Forge: 65.1.5.
- Required separate dependency: YACL Reforged 3.9.7+26.2, built from the author's unchanged tagged source.
- Clean build passed 16 TXT library and 11 message list editor assertions.
- Actual isolated Forge client passed 20 native config/YACL/TXT integration assertions.
- Binary: 114800 bytes; SHA-256 `c23aa8434a07d54a16f82c6b3e4fe81b65d375d6e5b0e42a56c9db1cd3b3c457`.
- Separate YACL dependency: 1182743 bytes; SHA-256 `2284cc927ba07ef1338e3f12f5045d80f397a3036102d2fb2b38fafdc5f0b98e`.
- Binary/source JARs contain the Forge metadata, matching resource pack metadata and both translations. No Fabric/NeoForge metadata, promotional image, QA mod or nested dependency is shipped in AutoMessage.
- Forge's MCP tooling adds annotations 24.0.1 to development dependencies. Runtime configurations exclude that standalone module because YACL already supplies annotations 26.0.2 through JarJar. Official vanilla and Forge installer runtime lists do not contain the MCP-added module.
- Runtime checks exercise the real Forge config extension and YACL callbacks. They do not establish multiplayer delivery or visible HUD rendering inside a world.

See [Forge build instructions](FORGE-BUILD.md) for dependency source pins, preparation commands and licensing.
