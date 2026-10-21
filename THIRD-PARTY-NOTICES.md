# Bundled YACL Reforged

RS's AutoMessage's own code is MIT licensed. Its Forge distribution embeds the
unchanged YACL Reforged mod using Forge Jar-in-Jar. Players install only the
AutoMessage release JAR; the nested library remains a separate Forge mod with
its original package names, metadata, notices and licenses.

YACL Reforged is SvyatON's unofficial Forge port of isXander's Yet Another Config
Lib, licensed LGPL-3.0-or-later. Original source:
https://github.com/SvyatON/YACL-Reforged

The version-specific source tags are F-3.9.7+26.1-26.1.2, F-3.9.7+26.2 and
F-3.9.7+26.3. Immutable source commits and archive SHA-256 values are recorded in
scripts/prepare-yacl.py and docs/FORGE-BUILD.md. Corresponding source JARs,
original source archives and provenance are distributed with the CI artifacts
and local release files. No upstream source files or binary contents are changed.

The nested YACL JAR contains its original META-INF/LICENSE, NOTICE,
MODIFICATIONS.md, THIRD-PARTY-LICENSES.md and individual third-party licenses,
including the notices for its nine nested libraries. Those files retain their
original terms and authorship.

To inspect or replace this library, open the outer mod JAR as a ZIP archive and
locate the YACL JAR using META-INF/jarjar/metadata.json. The embedded file can be
replaced with a compatible modified build retaining the expected mod ID and
version. The build is reproducible from the supplied corresponding sources.
