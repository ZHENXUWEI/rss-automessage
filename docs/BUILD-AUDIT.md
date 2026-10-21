# Forge 26.1 build audit

Verified 2026-10-21 using Java 25, Gradle 9.7.1 and ForgeGradle 7.0.29.

- Minecraft: 26.1; Forge: 62.0.9.
- YACL Reforged 3.9.7+26.1-26.1.2 is embedded unchanged through Forge Jar-in-Jar. Players install one AutoMessage JAR.
- Clean build passed 16 TXT library and 11 message list editor assertions.
- Actual isolated Forge client passed 22 assertions: two packaged-code origin checks and 20 native config/YACL/TXT integration checks, without a standalone YACL on the launch classpath.
- Binary: 1236558 bytes; SHA-256 `20ac8e2fed1f171927aa353cbf6baefefa3f5a403d644094f58b14c4b913addf`.
- Embedded YACL binary before outer ZIP compression: 1182338 bytes; SHA-256 `1ece5da14f8d9c58dc97faa0d21c7c03f18a9cc5460f3229026dd542efc9775d`. Its original nine nested libraries, licenses and notices are retained.
- Binary/source JARs contain the Forge metadata, matching resource pack metadata and both translations. No Fabric/NeoForge metadata, promotional image or QA mod is shipped. The release embeds exactly one YACL JAR and declares a metadata-only constraint for Forge's own MixinExtras 0.5.3.
- Forge's MCP tooling adds annotations 24.0.1 to development dependencies. Runtime configurations exclude that standalone module because YACL already supplies annotations 26.0.2 through JarJar. Official vanilla and Forge installer runtime lists do not contain the MCP-added module.
- Runtime checks exercise the real Forge config extension and YACL callbacks. They do not establish multiplayer delivery or visible HUD rendering inside a world.

See [Forge build instructions](FORGE-BUILD.md) for dependency source pins, preparation commands and licensing.
