package tachiyomi.data.achievement.handler

import kotlinx.coroutines.flow.first
import logcat.LogPriority
import logcat.logcat
import tachiyomi.data.achievement.database.AchievementsDatabase
import tachiyomi.data.achievement.handler.checkers.DiversityAchievementChecker
import tachiyomi.data.achievement.handler.checkers.StreakAchievementChecker
import tachiyomi.data.handlers.anime.AnimeDatabaseHandler
import tachiyomi.data.handlers.manga.MangaDatabaseHandler
import tachiyomi.data.handlers.novel.NovelDatabaseHandler
import tachiyomi.domain.achievement.model.Achievement
import tachiyomi.domain.achievement.model.AchievementProgress
import tachiyomi.domain.achievement.model.AchievementType
import tachiyomi.domain.achievement.repository.AchievementRepository
import tachiyomi.domain.entries.anime.repository.AnimeRepository
import tachiyomi.domain.entries.manga.repository.MangaRepository
import tachiyomi.domain.entries.novel.repository.NovelRepository

class AchievementCalculator(
    private val repository: AchievementRepository,
    private val mangaHandler: MangaDatabaseHandler,
    private val animeHandler: AnimeDatabaseHandler,
    private val novelHandler: NovelDatabaseHandler,
    private val diversityChecker: DiversityAchievementChecker,
    private val streakChecker: StreakAchievementChecker,
    private val achievementsDatabase: AchievementsDatabase,
    private val ruleRegistry: AchievementRuleRegistry,
    private val featureCollector: FeatureUsageCollector,
    private val pointsManager: PointsManager,
    private val mangaRepository: MangaRepository,
    private val animeRepository: AnimeRepository,
    private val novelRepository: NovelRepository,
) {
    companion object {
        private const val BATCH_SIZE = 50
    }

    suspend fun calculateInitialProgress(): CalculationResult {
        val startTime = System.currentTimeMillis()
        var achievementsProcessed = 0
        var achievementsUnlocked = 0

        try {
            logcat(LogPriority.INFO) { "Starting initial achievement calculation..." }

            val allAchievements = repository.getAll().first()
            val achievementsById = allAchievements.associateBy { it.id }

            val context = RuleContextImpl(
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
                achievementRepository = repository,
            )

            // Step 1: Evaluate all standard rules (non-meta, excluding secret_goku)
            val standardAchievements = allAchievements.filter {
                it.type != AchievementType.META &&
                    it.id != "secret_goku"
            }
            val standardProgressUpdates = standardAchievements.map { achievement ->
                val rule = ruleRegistry.getRule(achievement.id)
                val progress = rule?.evaluateFull(context) ?: 0
                buildProgress(achievement, progress)
            }

            // Step 2: Evaluate meta rules & Goku rule
            val metaAchievements = allAchievements.filter { it.type == AchievementType.META || it.id == "secret_goku" }
            val metaProgressUpdates = metaAchievements.map { achievement ->
                val rule = ruleRegistry.getRule(achievement.id)
                val progress = rule?.evaluateFull(context) ?: 0
                buildProgress(achievement, progress)
            }

            val allProgressUpdates = standardProgressUpdates + metaProgressUpdates

            allProgressUpdates.chunked(BATCH_SIZE).forEach { batch ->
                batch.forEach { progress ->
                    repository.insertOrUpdateProgress(progress)
                    achievementsProcessed++
                    if (progress.isUnlocked) achievementsUnlocked++
                }
            }

            val totalPoints = allProgressUpdates
                .filter { it.isUnlocked }
                .sumOf { achievementsById[it.achievementId]?.points ?: 0 }

            achievementsDatabase.userProfileQueries.updateXP(
                user_id = "default",
                total_xp = totalPoints.toLong(),
                current_xp = (totalPoints % 100).toLong(),
                level = 1, // Will be calculated by PointsManager
                xp_to_next_level = 100,
                last_updated = System.currentTimeMillis(),
            )
            achievementsDatabase.userProfileQueries.updateAchievementCounts(
                user_id = "default",
                unlocked = achievementsUnlocked.toLong(),
                total = allProgressUpdates.size.toLong(),
                last_updated = System.currentTimeMillis(),
            )

            populateActivityLog()

            val duration = System.currentTimeMillis() - startTime
            logcat(LogPriority.INFO) {
                "Achievement calculation completed in ${duration}ms. Processed: $achievementsProcessed, Unlocked: $achievementsUnlocked"
            }

            return CalculationResult(
                success = true,
                achievementsProcessed = achievementsProcessed,
                achievementsUnlocked = achievementsUnlocked,
                duration = duration,
            )
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { "Achievement calculation failed: ${e.message}" }
            return CalculationResult(
                success = false,
                error = e.message ?: "Unknown error",
            )
        }
    }

    private fun buildProgress(achievement: Achievement, progress: Int): AchievementProgress {
        val threshold = achievement.threshold ?: 1
        val isUnlocked = progress >= threshold
        return AchievementProgress(
            achievementId = achievement.id,
            progress = progress,
            maxProgress = threshold,
            isUnlocked = isUnlocked,
            unlockedAt = if (isUnlocked) System.currentTimeMillis() else null,
            lastUpdated = System.currentTimeMillis(),
        )
    }

    private suspend fun populateActivityLog() {
        logcat(LogPriority.INFO) { "Activity log population not yet implemented - streaks will build from first use" }
    }

    data class CalculationResult(
        val success: Boolean,
        val achievementsProcessed: Int = 0,
        val achievementsUnlocked: Int = 0,
        val duration: Long = 0,
        val error: String? = null,
    )
}
