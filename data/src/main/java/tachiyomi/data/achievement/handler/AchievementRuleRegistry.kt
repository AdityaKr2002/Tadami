package tachiyomi.data.achievement.handler

import tachiyomi.data.achievement.rules.BalancedRule
import tachiyomi.data.achievement.rules.ChadRule
import tachiyomi.data.achievement.rules.CrybabyRule
import tachiyomi.data.achievement.rules.DekuRule
import tachiyomi.data.achievement.rules.DiversityRule
import tachiyomi.data.achievement.rules.ErenRule
import tachiyomi.data.achievement.rules.EventRule
import tachiyomi.data.achievement.rules.FeatureBasedRule
import tachiyomi.data.achievement.rules.GokuRule
import tachiyomi.data.achievement.rules.HaremKingRule
import tachiyomi.data.achievement.rules.IsekaiTruckRule
import tachiyomi.data.achievement.rules.JojoRule
import tachiyomi.data.achievement.rules.LelouchRule
import tachiyomi.data.achievement.rules.LibraryRule
import tachiyomi.data.achievement.rules.MetaRule
import tachiyomi.data.achievement.rules.OnePieceRule
import tachiyomi.data.achievement.rules.QuantityRule
import tachiyomi.data.achievement.rules.SaitamaRule
import tachiyomi.data.achievement.rules.ShonenRule
import tachiyomi.data.achievement.rules.StreakRule
import tachiyomi.data.achievement.rules.TimeBasedRule
import tachiyomi.domain.achievement.model.AchievementCategory
import tachiyomi.domain.achievement.rule.AchievementRule
import tachiyomi.domain.entries.anime.repository.AnimeRepository
import tachiyomi.domain.entries.manga.repository.MangaRepository
import tachiyomi.domain.entries.novel.repository.NovelRepository

class AchievementRuleRegistry(
    private val mangaRepository: MangaRepository,
    private val animeRepository: AnimeRepository,
    private val novelRepository: NovelRepository,
) {
    val rules: List<AchievementRule> by lazy {
        listOf(
            // Quantity rules
            QuantityRule("read_10_chapters", AchievementCategory.MANGA),
            QuantityRule("read_50_chapters", AchievementCategory.MANGA),
            QuantityRule("read_100_chapters", AchievementCategory.MANGA),
            QuantityRule("read_500_chapters", AchievementCategory.MANGA),
            QuantityRule("read_1000_chapters", AchievementCategory.MANGA),
            QuantityRule("complete_10_manga", AchievementCategory.MANGA),
            QuantityRule("complete_50_manga", AchievementCategory.MANGA),

            QuantityRule("read_10_novel_chapters", AchievementCategory.NOVEL),
            QuantityRule("read_50_novel_chapters", AchievementCategory.NOVEL),
            QuantityRule("read_100_novel_chapters", AchievementCategory.NOVEL),
            QuantityRule("read_500_novel_chapters", AchievementCategory.NOVEL),
            QuantityRule("read_1000_novel_chapters", AchievementCategory.NOVEL),
            QuantityRule("complete_10_novel", AchievementCategory.NOVEL),
            QuantityRule("complete_50_novel", AchievementCategory.NOVEL),

            QuantityRule("watch_10_episodes", AchievementCategory.ANIME),
            QuantityRule("watch_50_episodes", AchievementCategory.ANIME),
            QuantityRule("watch_100_episodes", AchievementCategory.ANIME),
            QuantityRule("watch_500_episodes", AchievementCategory.ANIME),
            QuantityRule("watch_1000_episodes", AchievementCategory.ANIME),
            QuantityRule("complete_10_anime", AchievementCategory.ANIME),
            QuantityRule("complete_50_anime", AchievementCategory.ANIME),

            QuantityRule("content_master", AchievementCategory.BOTH),
            QuantityRule("content_god", AchievementCategory.BOTH),
            QuantityRule("content_overlord", AchievementCategory.BOTH),

            // Event rules
            EventRule("first_chapter"),
            EventRule("first_episode"),
            EventRule("first_novel_chapter"),
            EventRule("complete_1_manga"),
            EventRule("complete_1_anime"),
            EventRule("complete_1_novel"),
            EventRule("read_long_manga"),
            EventRule("read_long_novel"),

            // Library rules
            LibraryRule("library_collector", AchievementCategory.BOTH),
            LibraryRule("library_hoarder", AchievementCategory.BOTH),
            LibraryRule("library_titan", AchievementCategory.BOTH),
            LibraryRule("library_god", AchievementCategory.BOTH),

            // Diversity rules
            DiversityRule("genre_explorer", AchievementCategory.BOTH),
            DiversityRule("genre_explorer_complete", AchievementCategory.BOTH),
            DiversityRule("genre_explorer_ultimate", AchievementCategory.BOTH),

            // Streak rules
            StreakRule("week_warrior"),
            StreakRule("month_master"),
            StreakRule("season_champion"),
            StreakRule("yearly_devotee"),

            // Balanced rules
            BalancedRule("balanced_fan"),
            BalancedRule("hybrid_connoisseur"),
            BalancedRule("perfect_balance"),

            // Time based rules
            TimeBasedRule("night_owl"),
            TimeBasedRule("early_bird"),
            TimeBasedRule("marathon_reader"),

            // Feature based rules
            FeatureBasedRule("download_starter"),
            FeatureBasedRule("chapter_collector"),
            FeatureBasedRule("trophy_hunter"),
            FeatureBasedRule("search_user"),
            FeatureBasedRule("advanced_explorer"),
            FeatureBasedRule("filter_master"),
            FeatureBasedRule("backup_master"),
            FeatureBasedRule("settings_explorer"),
            FeatureBasedRule("stats_viewer"),
            FeatureBasedRule("theme_changer"),
            FeatureBasedRule("persistent_clicker"),
            FeatureBasedRule("secret_hall_unlocked"),

            // Secret rules
            SaitamaRule(),
            JojoRule(),
            HaremKingRule(),
            IsekaiTruckRule(),
            ChadRule(),
            ShonenRule(),
            CrybabyRule(mangaRepository, animeRepository, novelRepository),
            DekuRule(mangaRepository, animeRepository, novelRepository),
            ErenRule(mangaRepository, animeRepository, novelRepository),
            LelouchRule(mangaRepository, animeRepository, novelRepository),
            OnePieceRule(),
            GokuRule(),

            // Meta rules
            MetaRule("master_achiever"),
            MetaRule("achievement_hunter"),
            MetaRule("achievement_collector"),
            MetaRule("achievement_completionist"),
        )
    }

    private val rulesById: Map<String, AchievementRule> by lazy {
        rules.associateBy { it.achievementId }
    }

    fun getRule(achievementId: String): AchievementRule? {
        return rulesById[achievementId]
    }
}
