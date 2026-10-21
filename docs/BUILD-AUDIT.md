# Forge 26.2 build audit

Verified 2026-10-21 using Java 25, Gradle 9.7.1 and ForgeGradle 7.0.29.

- Minecraft: 26.2; Forge: 65.1.5.
- YACL Reforged 3.9.7+26.2 is embedded unchanged through Forge Jar-in-Jar. Players install one AutoMessage JAR.
- Clean build passed 16 TXT library and 11 message list editor assertions.
- Actual isolated Forge client passed 22 assertions: two packaged-code origin checks and 20 native config/YACL/TXT integration checks, without a standalone YACL on the launch classpath.
- Binary: 1237108 bytes; SHA-256 `748a6f676852dbdcf19a325f3ee0f7194852c2e360ce1bfef557676b42b83292`.
- Embedded YACL binary before outer ZIP compression: 1182743 bytes; SHA-256 `2284cc927ba07ef1338e3f12f5045d80f397a3036102d2fb2b38fafdc5f0b98e`. Its original nine nested libraries, licenses and notices are retained.
- Binary/source JARs contain the Forge metadata, matching resource pack metadata and both translations. No Fabric/NeoForge metadata, promotional image or QA mod is shipped. The release embeds exactly one YACL JAR and declares a metadata-only constraint for Forge's own MixinExtras 0.5.4.
- Forge's MCP tooling adds annotations 24.0.1 to development dependencies. Runtime configurations exclude that standalone module because YACL already supplies annotations 26.0.2 through JarJar. Official vanilla and Forge installer runtime lists do not contain the MCP-added module.
- Runtime checks exercise the real Forge config extension and YACL callbacks. They do not establish multiplayer delivery or visible HUD rendering inside a world.

See [Forge build instructions](FORGE-BUILD.md) for dependency source pins, preparation commands and licensing.
