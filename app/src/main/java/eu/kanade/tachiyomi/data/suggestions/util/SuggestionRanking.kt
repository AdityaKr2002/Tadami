package eu.kanade.tachiyomi.data.suggestions.util

import eu.kanade.tachiyomi.data.suggestions.SuggestionItem
import eu.kanade.tachiyomi.data.suggestions.SuggestionSeed
import eu.kanade.tachiyomi.data.suggestions.SuggestionSourceWeight
import eu.kanade.tachiyomi.data.suggestions.SuggestionTitleResolver

/**
 * F2.2 — Global dedup by cleaned title.
 *
 * Two items collide when their [SuggestionTitleResolver.cleanTitle] matches.
 * On a collision we keep the entry with the higher
 * [SuggestionSourceWeight] so the strongest signal wins.
 */
fun List<SuggestionItem>.dedupeByCleanTitle(): List<SuggestionItem> {
    if (isEmpty()) return this
    val ordered = sortedByDescending { SuggestionSourceWeight.of(it.reason) }
    val seenKeys = LinkedHashMap<String, SuggestionItem>()
    for (item in ordered) {
        val key = SuggestionTitleResolver.cleanTitle(item.title)
        if (key.isBlank()) {
            val pk = item.providerId ?: item.providerUrl
            if (seenKeys.values.none { (it.providerId ?: it.providerUrl) == pk }) {
                seenKeys["__blank__:${seenKeys.size}:$pk"] = item
            }
            continue
        }
        if (key !in seenKeys) {
            seenKeys[key] = item
        }
    }
    return seenKeys.values.toList()
}

/**
 * Best match score for this item against the seed's candidate titles.
 * Used to drive the final score = weight × bestMatchScore in the ranker.
 */
fun SuggestionItem.bestMatchScoreFor(seed: SuggestionSeed): Int {
    if (seed.candidateTitles.isEmpty()) return 0
    return seed.candidateTitles.maxOfOrNull { candidate ->
        SuggestionTitleResolver.scoreMatch(candidate, title)
    } ?: 0
}
