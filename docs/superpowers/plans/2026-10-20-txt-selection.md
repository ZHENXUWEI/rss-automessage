# TXT Selection Implementation Plan

> Execute in this session with superpowers:executing-plans. Track each task below.

**Goal:** Select a single TXT message source through YACL for Minecraft 1.21.8.

**Architecture:** A Java-only file library reads immutable snapshots; the Minecraft integration applies them on the client thread. A separate YACL page displays active and available files and refreshable status.

**Tech Stack:** Java 21, Fabric, Minecraft 1.21.8, YACL 3.8.2, Gradle.

## Tasks

- [x] Write `src/txtTest/java/cn/islys/util/TextFileLibraryTest.java` using a standalone main and temporary directories. Initially compiled at `src/test` with `javac --release 21`; the expected failure was the missing TextFileLibrary. It now runs as Gradle `txtLibraryTest` under `check`.
- [x] Implement `src/client/java/cn/islys/util/TextFileLibrary.java`: create empty directory, sorted direct regular TXT listing, validate filename containment, decode UTF-8/BOM, preserve existing `#`, `//`, `/` message syntax, and return snapshots without modifying configuration. All 16 checks pass.
- [x] Update `MessageImporter.java` with game-directory library access and snapshot application. Preserve legacy config-directory commands. Add `selectedTextFile` to `AutoMessengerConfig.java`; initialize the empty library in `RSsAutoMessageClient.java` and expose queue/countdown reset after switching.
- [x] Create `config/TextSelectionScreen.java` with YACL groups, per-file buttons, current/reload entry, refresh/open actions, background reading and client-thread application. Guard duplicate actions; close obsolete screens safely.
- [x] Modify `ModConfigScreen.java`: add selector before the long editor, save pending changes on navigation, return to fresh bindings, retain hidden message entries on save. Update both locale JSONs.
- [x] Add README usage, ignore local Gradle home, run Java harness and `gradlew.bat --gradle-user-home .gradle-agent build --no-daemon --console=plain`, inspect output JAR metadata and classes, and review `git diff --check` and the patch.
- [x] Review exposed repeated-save duplication of hidden messages. Extract `config/MessageListEditor.java`; reproduce the getter/pending mismatch with the old logic, fix the visible prefix length, and run the 11-check `messageListEditorTest` regression harness. Independent re-review confirmed the fix.

## Verification outcome

Full Gradle build and clean build passed. Final verification includes 16 TXT checks and 11 message-editor checks. Both locales have 24 matching TXT keys. Packaged mod version is 1.1.5 and Minecraft dependency is exactly 1.21.8. Builds needed sandbox escalation because Loom's temporary mapping JARs could not be opened inside the sandbox; the escalated builds succeeded. Game UI interactions below remain a manual acceptance checklist and have not been executed.

## Manual game checks

1. Launch 1.21.8 with Fabric API, Mod Menu and YACL. Confirm root folder exists and is empty.
2. Open selector with no files; see instructions, open directory, add two UTF-8 TXTs, refresh, choose each and check current file/messages.
3. Switch while the old list is sending; no old queued entry sends after success. The new list waits for its next interval.
4. Try an empty file, malformed UTF-8, and a deleted file. Check visible failure and unchanged messages/current file.
5. Switch to a file with over 100 messages, return and save settings; all hidden messages remain. Reopen/restart and check persisted selection.
