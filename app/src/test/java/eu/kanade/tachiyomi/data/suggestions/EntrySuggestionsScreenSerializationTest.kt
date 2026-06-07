package eu.kanade.tachiyomi.data.suggestions

import eu.kanade.tachiyomi.data.suggestions.sources.SuggestionMediaType
import eu.kanade.tachiyomi.ui.entries.suggestions.EntrySuggestionsScreen
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EntrySuggestionsScreenSerializationTest {

    @Test
    fun entrySuggestionsScreen_isSerializable() {
        val screen = EntrySuggestionsScreen(
            seed = SuggestionSeed(
                mediaType = SuggestionMediaType.MANGA,
                primaryTitle = "Test Manga",
                candidateTitles = listOf("Test Manga", "Test"),
                description = "Description",
                author = "Author",
                genres = listOf("Action"),
            ),
            sourceId = 1L,
            entryUrl = "/manga/test",
        )

        val bytes = ByteArrayOutputStream()
        ObjectOutputStream(bytes).use { it.writeObject(screen) }

        val restored = ObjectInputStream(ByteArrayInputStream(bytes.toByteArray())).use { it.readObject() }
        val restoredScreen = assertIs<EntrySuggestionsScreen>(restored)
        assertEquals("Test Manga", restoredScreen.seed.primaryTitle)
        assertEquals(SuggestionMediaType.MANGA, restoredScreen.seed.mediaType)
    }
}
