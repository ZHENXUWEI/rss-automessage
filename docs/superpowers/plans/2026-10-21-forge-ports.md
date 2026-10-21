# Forge 26.1–26.3 implementation plan

> **For agentic workers:** Use superpowers:executing-plans to implement these tasks and verify each result.

**Goal:** Ship native Forge versions of AutoMessage 1.1.5 for Minecraft 26.1, 26.2 and 26.3, retaining the existing YACL TXT selection and messaging behavior.

**Architecture:** Start from the corresponding working NeoForge implementation and replace loader integration with official Forge APIs. Keep TXT parsing, configuration and scheduling logic shared in behavior. YACL Reforged retains the original YACL API; build the author's tagged sources when no public binaries are available and provide it as a separate dependency.

**Tech stack:** Java 25, official Forge MDKs, ForgeGradle 7, YACL Reforged 3.9.7.

## Dependency and build setup

- [x] Verify official Forge downloads: 26.1/62.0.9, 26.2/65.1.5, 26.3/66.0.13.
- [x] Create `forge-26.1` from `neofor-26.1`, preserving the unrelated untracked promotional image.
- [x] Build original YACL Reforged tags, preserve legal files and sources, and record source commits and artifact hashes.
- [x] Replace `build.gradle`, `settings.gradle`, `gradle.properties` and wrapper with official Forge build structure and exact dependency pins.
- [x] Add a reproducible dependency preparation command usable locally and in CI.

## Native Forge integration

- [x] Replace metadata with `META-INF/mods.toml`, set `clientSideOnly=true`, and require exact Minecraft and YACL variants.
- [x] Replace entry point, config screen factory, game/config paths, chat, tick, network and command registration with official Forge APIs.
- [x] Register countdown HUD through Forge overlay layers while preserving visibility and ordering.
- [x] Adapt the disposable real-client smoke mod and run configuration; exclude all QA files from release archives.

## Verification per branch

- [x] Run `clean build`; require the existing 16 TXT library and 11 message editor checks to pass.
- [x] Launch an isolated actual Forge client and run the native config/TXT integration probe, including file switching, empty input preservation, closed-screen cancellation and persistence.
- [x] Inspect binary/source archives for exact loader/dependency metadata, Java 25, no promotional image, no QA classes and no unintended embedded libraries.
- [x] Port to `forge-26.2` and `forge-26.3`, using Gradle 9.7.1 and the corresponding existing Minecraft API changes.
- [x] Review the final native integration and dependency preparation independently, address findings and rerun affected checks.

## Delivery

- [x] Update Chinese/English installation instructions with the separate YACL Reforged dependency and tested limitations.
- [x] Copy verified JARs and required dependency/source/legal artifacts into `publish/1.1.5`, with a manifest containing source commits and SHA-256 values.
- [x] Commit each branch, push `forge-26.1`, `forge-26.2`, `forge-26.3` without force, and verify remote tips.

Commands use the checked-in Gradle wrapper with `--gradle-user-home .gradle-agent --no-daemon --console=plain -Dorg.gradle.java.home=<installed JDK 25>`. The runtime probe invokes the actual Forge config-screen extension and YACL action callbacks. It does not establish multiplayer delivery or manual HUD behavior in a world.
