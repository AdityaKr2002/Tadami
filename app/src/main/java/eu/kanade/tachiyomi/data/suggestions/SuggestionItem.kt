package eu.kanade.tachiyomi.data.suggestions

import eu.kanade.tachiyomi.data.suggestions.sources.SuggestionMediaType
import java.io.Serializable

/**
 * Reason describing where a [SuggestionItem] came from. Used to drive both
 * the final-score weighting (see [SuggestionSourceWeight]) and the
 * "source badge" rendered in the UI.
 */
enum class SuggestionReason {
    /** Native "related novels" returned by the source plugin. */
    RELATED,

    /** External recommendations sourced from AniList. */
    EXTERNAL_ANILIST,

    /** External recommendations sourced from MyAnimeList / Jikan. */
    EXTERNAL_MAL,

    /** External recommendations sourced from MangaUpdates. */
    EXTERNAL_MU,

    /** External recommendations sourced from NovelUpdates. */
    EXTERNAL_NU,

    /** Matched via Tier 1 / Tier 2 (relaxed) title search in the active source. */
    SEARCH_TITLE,

    /** Matched via author search in the active source. */
    SEARCH_AUTHOR,

    /** Matched via genre search backfill in the active source. */
    SEARCH_GENRE,

    /** Fallback from the source's popular catalogue. */
    POPULAR_BACKFILL,
}

data class SuggestionItem(
    val title: String,
    /**
     * All possible search query variants. The UI will try each one in order
     * and open the first non-empty result. Kept as a list so we can include
     * the original Cyrillic title, the Latin translation, the slug variant,
     * and the metadata primary title simultaneously.
     */
    val searchQueries: List<String> = listOf(title),
    val thumbnailUrl: String?,
    val providerName: String,
    val providerUrl: String,
    val providerId: String?,
    val mediaType: SuggestionMediaType,
    val reason: SuggestionReason = SuggestionReason.SEARCH_TITLE,
) : Serializable {

    /**
     * Backwards-compatible accessor for the "best" search query (the first
     * non-blank entry). Prefer [searchQueries] for iteration; this is kept
     * so older call sites keep compiling until they are migrated.
     */
    val searchQuery: String
        get() = searchQueries.firstOrNull { it.isNotBlank() } ?: title
}
