# Implementation Plan: Backup Compatibility with Sister Apps

This plan details the step-by-step tasks required to implement bidirectional backup compatibility between Tadami and Tachiyomi/Mihon forks.

## Goal
Implement a robust intermediate model (`MihonBackup`), intelligent auto-routing on restore (mapping novels/manga), and a "Smart Export" compatibility switch in backup creation options and UI settings.

## Tasks

- [ ] **Task 1: Add String Resources for Compatibility Option**
  * Add string resource keys (`pref_backup_sister_app_compat`, `pref_backup_sister_app_compat_summary`) to base and Russian strings files:
    - [base/strings.xml](file:///D:/lnreader/Tadami/ranobe-aniyomi/.worktrees/ranobe-novel/i18n/src/commonMain/moko-resources/base/strings.xml)
    - [ru/strings.xml](file:///D:/lnreader/Tadami/ranobe-aniyomi/.worktrees/ranobe-novel/i18n/src/commonMain/moko-resources/ru/strings.xml)
  * Verify: String resources are added successfully.

- [ ] **Task 2: Define Intermediate `MihonBackup` Model**
  * Create file [MihonBackup.kt](file:///D:/lnreader/Tadami/ranobe-aniyomi/.worktrees/ranobe-novel/app/src/main/java/eu/kanade/tachiyomi/data/backup/models/MihonBackup.kt) defining `MihonBackup` with standard Tachiyomi tags (1 to 106).
  * Verify: File compiles successfully.

- [ ] **Task 3: Add Model Mappings for Auto-Routing**
  * Write extension mappings inside `MihonBackup.kt` to copy fields from `BackupManga` to `BackupNovel` and `BackupAnime`.
  * Verify: Glues together chapters/episodes, history, tracking, and basic metadata.

- [ ] **Task 4: Implement `MihonBackup.toTadamiBackup` Method**
  * Implement the conversion logic from `MihonBackup` to Tadami `Backup`. 
  * Iterate over `backupManga` and check their source IDs via `mangaSourceManager`, `novelSourceManager`, and `animeSourceManager` to classify them.
  * Verify: Maps entries into their respective lists (`backupManga`, `backupNovel`, `backupAnime`).

- [ ] **Task 5: Update `BackupDecoder.kt` to Handle Both Formats**
  * Modify [BackupDecoder.kt](file:///D:/lnreader/Tadami/ranobe-aniyomi/.worktrees/ranobe-novel/app/src/main/java/eu/kanade/tachiyomi/data/backup/BackupDecoder.kt) to catch `SerializationException` when parsing standard `Backup`, fall back to `MihonBackup.serializer()`, and convert to Tadami `Backup`.
  * Verify: Passes source managers into the decoder or retrieves them from `Injekt`.

- [ ] **Task 6: Extend `BackupOptions.kt` with Compatibility Flag**
  * Add the `sisterAppCompatible` boolean flag to `BackupOptions` in [BackupOptions.kt](file:///D:/lnreader/Tadami/ranobe-aniyomi/.worktrees/ranobe-novel/app/src/main/java/eu/kanade/tachiyomi/data/backup/create/BackupOptions.kt), updating array serialization methods.
  * Verify: Flag propagates correctly.

- [ ] **Task 7: Integrate Flag into UI (`CreateBackupScreen.kt`)**
  * Update [CreateBackupScreen.kt](file:///D:/lnreader/Tadami/ranobe-aniyomi/.worktrees/ranobe-novel/app/src/main/java/eu/kanade/presentation/more/settings/screen/data/CreateBackupScreen.kt) to render the new compatibility option under the "Settings" or a new "Compatibility" section card.
  * Verify: Checkbox appears in the UI and toggles correctly.

- [ ] **Task 8: Implement "Smart Export" Compatibility Mode in `BackupCreator.kt`**
  * Modify [BackupCreator.kt](file:///D:/lnreader/Tadami/ranobe-aniyomi/.worktrees/ranobe-novel/app/src/main/java/eu/kanade/tachiyomi/data/backup/create/BackupCreator.kt) to flatten Novels and Anime into `backupManga` and `backupCategories` when `sisterAppCompatible` is enabled.
  * Verify: Conflict-prone fields like tag 106 (`backupMangaExtensionRepo`) and Aniyomi tags (501+) are fully cleared.

- [ ] **Task 9: Verify Project Compiles Successfully**
  * Run type check compile command `./gradlew compileDebugKotlin --no-daemon` to ensure zero compilation or syntax errors.
  * Verify: Command completes successfully.

- [ ] **Task 10: Run Unit Tests for Backup and Restore**
  * Run backup test suite `./gradlew testDebugUnitTest --tests "*Backup*" --no-daemon` to ensure no regressions are introduced.
  * Verify: All tests pass.

## Done When
- [ ] Bidirectional backup parsing works without `SerializationException`.
- [ ] Tachiyomi/Mihon imports auto-route novels into the **Novels** tab and manga into the **Manga** tab.
- [ ] Exports in compatibility format restore correctly inside Mihon/Komikku.
