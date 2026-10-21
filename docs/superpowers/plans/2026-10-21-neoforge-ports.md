# NeoForge ports implementation plan

> For agentic workers: use the executing-plans workflow to implement the tightly coupled version ports in sequence. Independent dependency/API research and final reviews may use subagents.

**Goal:** Deliver NeoForge 26.1, 26.2 and 26.3 equivalents of mod 1.1.5 in neofor-26.1, neofor-26.2 and neofor-26.3.

**Architecture:** Preserve pure TXT/editor logic and YACL UI. Replace only loader/build metadata, directory providers, event registration, command source and Mods-screen integration. Keep client-only initialization and separate per-version dependencies.

**Tech stack:** Java 25, native NeoForge, ModDevGradle, external NeoForge YACL, Gradle wrapper.

## Task 1: Verify dependency/API inputs
- [x] Resolve corresponding official NeoForge MDKs/Maven versions and YACL NeoForge JAR metadata for all three MC versions; record release/beta status and exact pins.
- [x] Inspect native tick/network/chat/client-command/HUD/config-screen event source signatures. Confirm resource/mod ID restrictions.

## Task 2: Port 26.1 build and entry points
Files: build.gradle, settings.gradle, gradle.properties, wrapper properties, .github/workflows/build.yml, src/main/resources/META-INF/neoforge.mods.toml, src/main/java/cn/islys/RSsAutoMessage.java, src/client/java/cn/islys/RSsAutoMessageClient.java.
- [x] Replace Loom with ModDevGradle and Java 25; include src/client/java in main and main output in txtTest classpaths; retain check dependencies and image exclusion.
- [x] Remove fabric.mod.json and ModMenuApiImpl.java. Add native NeoForge metadata with rss_automessage, precise MC/NeoForge/YACL constraints, and client-only dependency side.
- [x] Use a client-only NeoForge @Mod constructor to register the configuration screen, client events, commands and HUD. Preserve initialization order: instance, config load, event registration, TXT directory creation.

## Task 3: Port adapters
Files: config/AutoMessengerConfig.java, util/MessageImporter.java, command/AutoMessageCommand.java, hud/CountdownHud.java, RSsAutoMessageClient.java.
- [x] Replace FabricLoader directory access with FMLPaths.CONFIGDIR/GAMEDIR without changing filenames.
- [x] Register NeoForge post-tick, login/logout and player-chat events; keep existing scheduler/reply logic.
- [x] Port the automessage command tree to NeoForge client command registration and local feedback.
- [x] Register the HUD before vanilla chat using NeoForge's matching extraction API and hidden state.

## Task 4: Validate and package 26.1
- [x] Run .\gradlew.bat --gradle-user-home .gradle-agent clean build --no-daemon --console=plain with the JDK 25 Gradle JVM. Expect BUILD SUCCESSFUL and PASS counts 16 and 11.
- [x] Inspect output NeoForge TOML and Java major 69; reject Fabric metadata, tests, promotional image and bundled dependencies. Verify actual YACL/NeoForge dependencies and client-only entry point.
- [x] Review compatibility; fix material findings and rerun affected verification. Attempt an isolated client startup smoke check with sending disabled, recording its actual scope/result.
- [x] Update README/build audit, copy the verified JAR to publish/1.1.5 and commit neofor-26.1.

## Task 5: Port and validate 26.2
- [ ] Create neofor-26.2 from verified neofor-26.1; change native loader/YACL pins and MC constraints for 26.2.
- [ ] Apply the established Fabric 26.2 GUI migrations: gui.setScreen()/screen(), gui.toastManager(), gui.hud.getChat() and gui.hud.isHidden(). Adapt any native NeoForge event signature changes using actual sources.
- [ ] Run clean build and all 27 checks, inspect metadata/archive, review, update audit, package and commit.

## Task 6: Port and validate 26.3
- [ ] Create neofor-26.3 from verified neofor-26.2; change native loader/YACL pins and MC constraints for 26.3.
- [ ] Use Blaze3D.openPath(Path) for folder opening; inspect native event signatures and SDL-related compatibility.
- [ ] Run clean build and all 27 checks, inspect metadata/archive, review, update audit, package and commit.

## Task 7: Publish and verify
- [ ] Produce a three-artifact manifest with exact dependency pins, sizes, SHA256 and test/runtime scope. Confirm git diff --check and preserved promotional PNG.
- [ ] Push all three exact neofor-* branches without force, and compare git ls-remote SHA values with local heads.
- [ ] Report artifact links, validation evidence, any beta dependency status and remaining manual-test limitations.
