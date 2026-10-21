# Self-contained Forge YACL implementation plan

> **For agentic workers:** Use superpowers:executing-plans and verify the packaged artifact rather than only a development classpath.

**Goal:** Players install one AutoMessage JAR on Forge 26.1, 26.2 and 26.3 with no separate YACL download.

**Architecture:** Use the official Forge Jar-in-Jar plugin to embed the unchanged, pinned YACL Reforged mod JAR. Forge discovers its existing nine nested libraries recursively. Preserve all embedded licenses/notices and supply corresponding dependency sources/provenance in CI artifacts and the local release directory. Keep AutoMessage's code and YACL's original packages separate.

**Tech stack:** Java 25, ForgeGradle 7.0.29, JarJar 0.2.3, Gradle 9.7.1, YACL Reforged 3.9.7 for each exact Minecraft version.

- [ ] Add `scripts/verify-bundled-yacl.py` to validate the final outer JAR, exact JarJar coordinates/version, nested byte identity, legal files, nine child libraries and absence of QA/promotional assets; demonstrate it fails on the current thin JAR.
- [ ] Configure `build.gradle` to produce a self-contained unclassified release JAR using JarJar, put the thin intermediate elsewhere, include notices and publish the release artifact.
- [ ] Adapt `scripts/forge-smoke.init.gradle` to launch the actual release JAR with QA output and Forge runtime libraries, excluding the standalone YACL JAR and main development output.
- [ ] Require the actual client probe to verify that both AutoMessage and YACL load from packaged JARs, then exercise the existing 20 configuration/TXT assertions.
- [ ] Update `.github/workflows/build.yml` to verify packaging and upload the single installable release JAR plus source/provenance artifacts.
- [ ] Update README, Forge build instructions and audits to explain one-JAR installation and the new size, preserving licensing and source provenance.
- [ ] Build and test Forge 26.3, then port the packaging changes to Forge 26.2 and 26.1 without changing their Minecraft APIs or resource formats.
- [ ] Independently review build/runtime classpaths and packaged metadata; fix any findings.
- [ ] Replace local Forge release files, record checksums/sizes and source commits, push all three existing Forge branches, and wait for successful GitHub Actions runs.

The original untracked promotional PNG remains outside commits and archives. Existing NeoForge and Fabric branches are outside this change.
