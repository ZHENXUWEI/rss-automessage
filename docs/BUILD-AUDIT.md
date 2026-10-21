# Forge 26.3 build audit

Verified 2026-10-21 using Java 25, Gradle 9.7.1 and ForgeGradle 7.0.29.

- Minecraft: 26.3; Forge: 66.0.13.
- YACL Reforged 3.9.7+26.3 is embedded unchanged through Forge Jar-in-Jar. Players install one AutoMessage JAR.
- Clean build passed 16 TXT library and 11 message list editor assertions.
- Actual isolated Forge client passed 22 assertions: two packaged-code origin checks and 20 native config/YACL/TXT integration checks, without a standalone YACL on the launch classpath.
- Binary: 1237193 bytes; SHA-256 `e76a7f5a689e6436ee600174010a7be0f15b53af2520cab56e3372d92c5e8a36`.
- Embedded YACL binary before outer ZIP compression: 1182848 bytes; SHA-256 `08a9ae79c83906e832b7a63e115078756a469b85dec5c0bbf8c6b4d3862b9b4e`. Its original nine nested libraries, licenses and notices are retained.
- Binary/source JARs contain the Forge metadata, matching resource pack metadata and both translations. No Fabric/NeoForge metadata, promotional image or QA mod is shipped. The release embeds exactly one YACL JAR and declares a metadata-only constraint for Forge's own MixinExtras 0.5.4.
- Forge's MCP tooling adds annotations 24.0.1 to development dependencies. Runtime configurations exclude that standalone module because YACL already supplies annotations 26.0.2 through JarJar. Official vanilla and Forge installer runtime lists do not contain the MCP-added module.
- Runtime checks exercise the real Forge config extension and YACL callbacks. They do not establish multiplayer delivery or visible HUD rendering inside a world.

See [Forge build instructions](FORGE-BUILD.md) for dependency source pins, preparation commands and licensing.
