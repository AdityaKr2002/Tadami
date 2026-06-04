package tachiyomi.data.achievement

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tachiyomi.data.achievement.handler.FeatureUsageCollector
import tachiyomi.data.achievement.handler.RuleContextImpl
import tachiyomi.data.achievement.handler.checkers.DiversityAchievementChecker
import tachiyomi.data.achievement.handler.checkers.StreakAchievementChecker
import tachiyomi.data.handlers.anime.AnimeDatabaseHandler
import tachiyomi.data.handlers.manga.MangaDatabaseHandler
import tachiyomi.data.handlers.novel.NovelDatabaseHandler
import tachiyomi.domain.achievement.model.AchievementCategory
import tachiyomi.domain.achievement.repository.AchievementRepository
import tachiyomi.domain.entries.anime.repository.AnimeRepository
import tachiyomi.domain.entries.manga.repository.MangaRepository
import tachiyomi.domain.entries.novel.repository.NovelRepository

class RuleContextTest : AchievementTestBase() {

    private lateinit var mangaHandler: MangaDatabaseHandler
    private lateinit var animeHandler: AnimeDatabaseHandler
    private lateinit var novelHandler: NovelDatabaseHandler
    private lateinit var mangaRepository: MangaRepository
    private lateinit var animeRepository: AnimeRepository
    private lateinit var novelRepository: NovelRepository
    private lateinit var diversityChecker: DiversityAchievementChecker
    private lateinit var streakChecker: StreakAchievementChecker
    private lateinit var featureCollector: FeatureUsageCollector
    private lateinit var pointsManager: tachiyomi.data.achievement.handler.PointsManager
    private lateinit var achievementRepository: AchievementRepository

    private lateinit var context: RuleContextImpl

    @BeforeEach
    override fun setup() {
        super.setup()

        mangaHandler = mockk()
        animeHandler = mockk()
        novelHandler = mockk()
        mangaRepository = mockk()
        animeRepository = mockk()
        novelRepository = mockk()
        diversityChecker = mockk()
        streakChecker = mockk()
        featureCollector = mockk()
        pointsManager = mockk()
        achievementRepository = mockk()

        context = RuleContextImpl(
            mangaHandler = mangaHandler,
            animeHandler = animeHandler,
            novelHandler = novelHandler,
            mangaRepository = mangaRepository,
            animeRepository = animeRepository,
            novelRepository = novelRepository,
            diversityChecker = diversityChecker,
            streakChecker = streakChecker,
            featureCollector = featureCollector,
            pointsManager = pointsManager,
            achievementRepository = achievementRepository,
        )
    }

    @Test
    fun `getChaptersRead Manga calls MangaDatabaseHandler`() = runTest {
        coEvery { mangaHandler.awaitOneOrNull<Long>(any(), any()) } returns 42L

        val result = context.getChaptersRead(AchievementCategory.MANGA)

        result shouldBe 42
    }
}
