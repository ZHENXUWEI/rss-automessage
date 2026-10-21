# Forge 26.3 build audit

Verified 2026-10-21 using Java 25, Gradle 9.7.1 and ForgeGradle 7.0.29.

- Minecraft: 26.3; Forge: 66.0.13.
- Required separate dependency: YACL Reforged 3.9.7+26.3, built from the author's unchanged tagged source.
- Clean build passed 16 TXT library and 11 message list editor assertions.
- Actual isolated Forge client passed 20 native config/YACL/TXT integration assertions.
- Binary: 114762 bytes; SHA-256 `a89017a1edf31130aca96b3a08589476ffb7efb9c4de839d69f0446b75299ad4`.
- Separate YACL dependency: 1182848 bytes; SHA-256 `08a9ae79c83906e832b7a63e115078756a469b85dec5c0bbf8c6b4d3862b9b4e`.
- Binary/source JARs contain the Forge metadata, matching resource pack metadata and both translations. No Fabric/NeoForge metadata, promotional image, QA mod or nested dependency is shipped in AutoMessage.
- Forge's MCP tooling adds annotations 24.0.1 to development dependencies. Runtime configurations exclude that standalone module because YACL already supplies annotations 26.0.2 through JarJar. Official vanilla and Forge installer runtime lists do not contain the MCP-added module.
- Runtime checks exercise the real Forge config extension and YACL callbacks. They do not establish multiplayer delivery or visible HUD rendering inside a world.

See [Forge build instructions](FORGE-BUILD.md) for dependency source pins, preparation commands and licensing.
