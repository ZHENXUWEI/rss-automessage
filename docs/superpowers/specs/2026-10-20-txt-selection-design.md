# TXT selection for Minecraft 1.21.8

The user requested a game-directory TXT folder and selection from mod settings, refined the selector to behave like the resource-pack chooser, and specified YACL as the UI library.

## Behavior

- On client initialization create `<gameDir>/rss-automessage/` without creating example files there.
- Keep legacy `config/rss-automessage/messages.txt` import/export commands available.
- Add a YACL text-selection page with Current text and Available texts groups. Each TXT has a select action; only one file is active. Include folder-open and refresh actions. List only direct regular `.txt` files, case-insensitively, sorted by filename.
- Persist the selected filename and the imported message snapshot. Restart preserves the snapshot rather than silently importing changed files. The active file has a reload action.
- Read UTF-8 off the client thread. Apply the complete parsed snapshot and save on the client thread. A successful switch stops queued old messages and resets the countdown without sending immediately.
- Missing, empty, invalid UTF-8 or unreadable files leave the active filename and messages unchanged. Display status in the YACL page, including at the main menu. Prevent simultaneous selection requests.
- Returning to settings rebuilds their bindings so stale message-list options cannot overwrite the new import. Preserve unrelated edits when opening the selector. The truncated 100-message editor must preserve undisplayed entries.
- Add Chinese and English strings and usage instructions. Target the existing 1.21.6-11 branch, whose build uses Minecraft 1.21.8 and Java 21.

## Structure and validation

`TextFileLibrary` owns directory creation, listing, filename validation and parsing without depending on Minecraft. `MessageImporter` applies successful results and preserves legacy entry points. `TextSelectionScreen` builds the YACL page and cancels callbacks after either user navigation or an engine screen replacement. Configuration stores the selected filename; the client owns queue reset. `MessageListEditor` keeps the hidden suffix separate from the YACL binding so repeated saves do not duplicate hidden entries.

Use standalone Java test harnesses for real temporary-directory creation, filtering, ordering, Unicode/BOM parsing, comments/commands, empty and malformed files, traversal rejection, and repeated editing/saving of truncated message lists. Both harnesses run under Gradle `check` (27 checks in total). Compile the full mod with Gradle and inspect the built JAR. Game rendering and interactions require an available Minecraft client; do not claim visual verification without it.
