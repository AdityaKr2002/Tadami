package eu.kanade.domain.source.service

import org.junit.jupiter.api.Test
import tachiyomi.core.common.preference.InMemoryPreferenceStore
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SourcePreferencesTest {

    @Test
    fun `migration strategy reuses prioritize by chapters preference`() {
        val store = InMemoryPreferenceStore(
            sequenceOf(
                InMemoryPreferenceStore.InMemoryPreference(
                    key = "migration_prioritize_by_chapters",
                    data = true,
                    defaultValue = false,
                ),
            ),
        )
        val preferences = SourcePreferences(store)

        assertEquals(SourcePreferences.MigrationStrategy.MOST_CHAPTERS, preferences.migrationStrategy())
    }

    @Test
    fun `migration defaults favor the new wizard flow`() {
        val preferences = SourcePreferences(InMemoryPreferenceStore())

        assertTrue(preferences.migrationSearchKeywords().get())
        assertTrue(preferences.migrationExtraSearchParam().get())
        assertFalse(preferences.migrationSkipNextTime().get())
        assertFalse(preferences.migrationHideNotFound().get())
        assertFalse(preferences.migrationOnlyNewChapters().get())
    }
}
