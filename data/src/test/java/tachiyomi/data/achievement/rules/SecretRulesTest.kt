package tachiyomi.data.achievement.rules

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tachiyomi.domain.achievement.model.AchievementCategory
import tachiyomi.domain.achievement.model.AchievementEvent
import tachiyomi.domain.achievement.rule.RuleContext
import tachiyomi.domain.achievement.rule.RuleResult
import tachiyomi.domain.entries.anime.model.Anime
import tachiyomi.domain.entries.anime.repository.AnimeRepository
import tachiyomi.domain.entries.manga.model.Manga
import tachiyomi.domain.entries.manga.repository.MangaRepository
import tachiyomi.domain.entries.novel.model.Novel
import tachiyomi.domain.entries.novel.repository.NovelRepository

class SecretRulesTest {

    private lateinit var context: RuleContext

    @BeforeEach
    fun setup() {
        context = mockk()
    }

    @Test
    fun `SaitamaRule triggers when library has exactly 1 manga 1 anime 1 novel`() = runTest {
        val rule = SaitamaRule()
        coEvery { context.getLibraryCount(AchievementCategory.MANGA) } returns 1
        coEvery { context.getLibraryCount(AchievementCategory.ANIME) } returns 1
        coEvery { context.getLibraryCount(AchievementCategory.NOVEL) } returns 1

        rule.evaluateFull(context) shouldBe 1

        val event = AchievementEvent.LibraryAdded(1L, AchievementCategory.MANGA)
        rule.evaluateDelta(event, 0, context) shouldBe RuleResult.Update(1)
    }

    @Test
    fun `JojoRule triggers when library title like jojo is present`() = runTest {
        val rule = JojoRule()
        coEvery { context.hasLibraryTitleLike("jojo") } returns true

        rule.evaluateFull(context) shouldBe 1

        val event = AchievementEvent.LibraryAdded(1L, AchievementCategory.MANGA)
        rule.evaluateDelta(event, 0, context) shouldBe RuleResult.Update(1)
    }

    @Test
    fun `HaremKingRule triggers when harem genre titles count is 20 or more`() = runTest {
        val rule = HaremKingRule()
        coEvery { context.hasLibraryGenre("Harem") } returns 20

        rule.evaluateFull(context) shouldBe 1
    }

    @Test
    fun `IsekaiTruckRule triggers when isekai genre titles count is 20 or more`() = runTest {
        val rule = IsekaiTruckRule()
        coEvery { context.hasLibraryGenre("Isekai") } returns 20

        rule.evaluateFull(context) shouldBe 1
    }

    @Test
    fun `CrybabyRule triggers when completed title has Tragedy or Drama genre`() = runTest {
        val mangaRepo = mockk<MangaRepository>()
        val animeRepo = mockk<AnimeRepository>()
        val novelRepo = mockk<NovelRepository>()
        val rule = CrybabyRule(mangaRepo, animeRepo, novelRepo)

        val manga = mockk<Manga> {
            coEvery { genre } returns listOf("Drama")
        }
        coEvery { mangaRepo.getMangaById(1L) } returns manga

        val event = AchievementEvent.MangaCompleted(1L)
        rule.evaluateDelta(event, 0, context) shouldBe RuleResult.Update(1)
    }

    @Test
    fun `ShonenRule triggers when completed shonen titles count is 10 or more`() = runTest {
        val rule = ShonenRule()
        coEvery { context.hasLibraryGenre("Shounen") } returns 6
        coEvery { context.hasLibraryGenre("Shonen") } returns 4

        rule.evaluateFull(context) shouldBe 1
    }

    @Test
    fun `DekuRule triggers when completed title has Super Power genre`() = runTest {
        val mangaRepo = mockk<MangaRepository>()
        val animeRepo = mockk<AnimeRepository>()
        val novelRepo = mockk<NovelRepository>()
        val rule = DekuRule(mangaRepo, animeRepo, novelRepo)

        val anime = mockk<Anime> {
            coEvery { genre } returns listOf("Super Power")
        }
        coEvery { animeRepo.getAnimeById(2L) } returns anime

        val event = AchievementEvent.AnimeCompleted(2L)
        rule.evaluateDelta(event, 0, context) shouldBe RuleResult.Update(1)
    }

    @Test
    fun `ErenRule triggers when completed title has Military genre`() = runTest {
        val mangaRepo = mockk<MangaRepository>()
        val animeRepo = mockk<AnimeRepository>()
        val novelRepo = mockk<NovelRepository>()
        val rule = ErenRule(mangaRepo, animeRepo, novelRepo)

        val novel = mockk<Novel> {
            coEvery { genre } returns listOf("Military")
        }
        coEvery { novelRepo.getNovelById(3L) } returns novel

        val event = AchievementEvent.NovelCompleted(3L)
        rule.evaluateDelta(event, 0, context) shouldBe RuleResult.Update(1)
    }

    @Test
    fun `LelouchRule triggers when completed title has Psychological genre`() = runTest {
        val mangaRepo = mockk<MangaRepository>()
        val animeRepo = mockk<AnimeRepository>()
        val novelRepo = mockk<NovelRepository>()
        val rule = LelouchRule(mangaRepo, animeRepo, novelRepo)

        val manga = mockk<Manga> {
            coEvery { genre } returns listOf("Psychological")
        }
        coEvery { mangaRepo.getMangaById(4L) } returns manga

        val event = AchievementEvent.MangaCompleted(4L)
        rule.evaluateDelta(event, 0, context) shouldBe RuleResult.Update(1)
    }

    @Test
    fun `OnePieceRule triggers when total read chapters is 1000 or more`() = runTest {
        val rule = OnePieceRule()
        coEvery { context.getChaptersRead(AchievementCategory.BOTH) } returns 1000

        rule.evaluateFull(context) shouldBe 1000
    }

    @Test
    fun `GokuRule triggers when total points is 9000 or more`() = runTest {
        val rule = GokuRule()
        coEvery { context.getCurrentPoints() } returns 9000

        rule.evaluateFull(context) shouldBe 9000

        val event = AchievementEvent.AppStart(12)
        rule.evaluateDelta(event, 0, context) shouldBe RuleResult.Update(9000)
    }
}
