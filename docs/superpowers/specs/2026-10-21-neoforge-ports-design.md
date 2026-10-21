# NeoForge 26.1–26.3 ports

The owner requests NeoForge editions of the existing mod for Minecraft 26.1, 26.2 and 26.3, using branches neofor-26.1, neofor-26.2 and neofor-26.3. Existing Fabric behavior and the approved YACL TXT selector remain the functional specification. Version remains 1.1.5.

Use a native NeoForge build with ModDevGradle, not a compatibility loader. Each branch targets its own Minecraft minor version and installs the corresponding external NeoForge YACL release. The NeoForge Mods screen supplies the configuration entry point; Fabric API and Mod Menu are removed. Keep existing client sources in src/client/java but include them in the main source set, with a client-only @Mod entry point so dedicated servers do not initialize Minecraft client classes.

NeoForge requires a mod ID containing letters, numbers and underscores, so its registration ID is rss_automessage. Existing resource namespace, TXT directory rss-automessage and config/rss-automessage.json are retained. The YACL selector still switches immediately, saves the selected filename/message snapshot, rejects empty or unreadable files, cancels reads after leaving the screen and resets the old sending queue/countdown.

Migrate ticks, login/logout, player-chat messages, client commands and the countdown HUD to native NeoForge events. Keep automatic replies restricted to player chat, matching the 26.x Fabric edition. Register the HUD before vanilla chat and respect the hidden-HUD state. Use each Minecraft version's own screen/folder-opening APIs. Preserve legacy import/export/example/clear commands.

Validate each branch with Java 25 clean build, the existing 16 TXT checks and 11 message-editor checks, dependency metadata checks, archive inspection and independent code review. Archive checks require NeoForge metadata, no Fabric metadata or Mod Menu entry point, no tests, no bundled external mod dependencies and no promotional image. Report runtime smoke checks separately from manual in-game interaction. Build success alone is not game acceptance.

Produce separate neoforge-named JARs under sibling publish/1.1.5. Commit each branch, push using the restored GitHub authentication and read back remote commit IDs. Do not change the existing Fabric branches or delete the untracked promotional image.
