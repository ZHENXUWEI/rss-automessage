# Forge 26.1 build audit

Verified 2026-10-21 using Java 25, Gradle 9.7.1 and ForgeGradle 7.0.29.

- Minecraft: 26.1; Forge: 62.0.9.
- Required separate dependency: YACL Reforged 3.9.7+26.1-26.1.2, built from the author's unchanged tagged source.
- Clean build passed 16 TXT library and 11 message list editor assertions.
- Actual isolated Forge client passed 20 native config/YACL/TXT integration assertions.
- Binary: 114687 bytes; SHA-256 `7d9de1349185415920b7c744e9b2b8b8a16f2718f396f29fe24559cd3403bc3d`.
- Separate YACL dependency: 1182338 bytes; SHA-256 `1ece5da14f8d9c58dc97faa0d21c7c03f18a9cc5460f3229026dd542efc9775d`.
- Binary/source JARs contain the Forge metadata, matching resource pack metadata and both translations. No Fabric/NeoForge metadata, promotional image, QA mod or nested dependency is shipped in AutoMessage.
- Forge's MCP tooling adds annotations 24.0.1 to development dependencies. Runtime configurations exclude that standalone module because YACL already supplies annotations 26.0.2 through JarJar. Official vanilla and Forge installer runtime lists do not contain the MCP-added module.
- Runtime checks exercise the real Forge config extension and YACL callbacks. They do not establish multiplayer delivery or visible HUD rendering inside a world.

See [Forge build instructions](FORGE-BUILD.md) for dependency source pins, preparation commands and licensing.
