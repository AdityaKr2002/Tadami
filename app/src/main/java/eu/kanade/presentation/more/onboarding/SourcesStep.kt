package eu.kanade.presentation.more.onboarding

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.more.resolveAuroraMoreCardBorderColor
import eu.kanade.presentation.theme.AuroraSurfaceLevel
import eu.kanade.presentation.theme.AuroraTheme
import eu.kanade.presentation.theme.resolveAuroraElevation
import eu.kanade.presentation.theme.resolveAuroraSurfaceColor
import mihon.domain.extensionrepo.anime.interactor.CreateAnimeExtensionRepo
import mihon.domain.extensionrepo.manga.interactor.CreateMangaExtensionRepo
import mihon.domain.extensionrepo.novel.interactor.CreateNovelExtensionRepo
import org.json.JSONArray
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.LocalAppHaptics
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

internal class SourcesStep : OnboardingStep {

    private val uiPreferences: UiPreferences = Injekt.get()

    private val createAnimeRepo: CreateAnimeExtensionRepo = Injekt.get()
    private val createMangaRepo: CreateMangaExtensionRepo = Injekt.get()
    private val createNovelRepo: CreateNovelExtensionRepo = Injekt.get()

    private var _isComplete by mutableStateOf(true)

    override val isComplete: Boolean
        get() = _isComplete

    val selectedRepos = mutableStateListOf<PredefinedRepo>()
    var isRegistering by mutableStateOf(false)
        private set

    data class PredefinedRepo(
        val name: String,
        val url: String,
        val type: String,
    )

    private fun loadPredefinedRepos(context: Context): List<PredefinedRepo> {
        val jsonString = try {
            context.assets.open("predefined_repos_private.json").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            try {
                context.assets.open("predefined_repos.json").bufferedReader().use { it.readText() }
            } catch (e2: Exception) {
                "[]"
            }
        }
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<PredefinedRepo>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    PredefinedRepo(
                        name = obj.getString("name"),
                        url = obj.getString("url"),
                        type = obj.getString("type"),
                    ),
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun registerSelectedRepos() {
        if (selectedRepos.isEmpty()) return
        isRegistering = true
        _isComplete = false
        try {
            selectedRepos.forEach { repo ->
                try {
                    when (repo.type) {
                        "anime" -> createAnimeRepo.await(
                            repo.url,
                            displayName = repo.name,
                            forceLocalInsert = true,
                        )
                        "manga" -> createMangaRepo.await(
                            repo.url,
                            displayName = repo.name,
                            forceLocalInsert = true,
                        )
                        "novel" -> createNovelRepo.await(
                            repo.url,
                            displayName = repo.name,
                            forceLocalInsert = true,
                        )
                    }
                } catch (e: Exception) {
                    // Ignore failures, proceed
                }
            }
        } finally {
            isRegistering = false
            _isComplete = true
        }
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current

        val showAnime by uiPreferences.showAnimeSection().collectAsState()
        val showManga by uiPreferences.showMangaSection().collectAsState()
        val showNovel by uiPreferences.showNovelSection().collectAsState()

        val allRepos = remember { loadPredefinedRepos(context) }
        val filteredRepos = remember(showAnime, showManga, showNovel, allRepos) {
            allRepos.filter { repo ->
                (repo.type == "anime" && showAnime) ||
                    (repo.type == "manga" && showManga) ||
                    (repo.type == "novel" && showNovel)
            }
        }

        // Sync initially to select all filtered repos
        LaunchedEffect(filteredRepos) {
            selectedRepos.clear()
            selectedRepos.addAll(filteredRepos)
        }

        val colors = AuroraTheme.colors

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(MR.strings.onboarding_sources_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
            )

            Text(
                text = stringResource(MR.strings.onboarding_sources_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )

            if (isRegistering) {
                // Loading state card — Aurora-styled
                val loadingBg = when {
                    colors.isEInk -> resolveAuroraSurfaceColor(colors, AuroraSurfaceLevel.Subtle)
                    colors.isDark -> Color.White.copy(alpha = 0.05f)
                    else -> Color.White
                }
                val loadingBorder = when {
                    colors.isEInk -> resolveAuroraMoreCardBorderColor(colors)
                    colors.isDark -> Color.White.copy(alpha = 0.08f)
                    else -> Color.Transparent
                }
                val loadingElevation = if (!colors.isDark && !colors.isEInk) {
                    resolveAuroraElevation(colors, AuroraSurfaceLevel.Glass)
                } else {
                    0.dp
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = loadingBg),
                    border = BorderStroke(1.dp, loadingBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = loadingElevation),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            CircularProgressIndicator(color = colors.accent)
                            Text(
                                text = stringResource(MR.strings.onboarding_sources_loading),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            } else {
                if (filteredRepos.isEmpty()) {
                    // Empty state card
                    val emptyBg = when {
                        colors.isEInk -> resolveAuroraSurfaceColor(colors, AuroraSurfaceLevel.Subtle)
                        colors.isDark -> Color.White.copy(alpha = 0.05f)
                        else -> Color.White
                    }
                    val emptyBorder = when {
                        colors.isEInk -> resolveAuroraMoreCardBorderColor(colors)
                        colors.isDark -> Color.White.copy(alpha = 0.08f)
                        else -> Color.Transparent
                    }
                    val emptyElevation = if (!colors.isDark && !colors.isEInk) {
                        resolveAuroraElevation(colors, AuroraSurfaceLevel.Glass)
                    } else {
                        0.dp
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = emptyBg),
                        border = BorderStroke(1.dp, emptyBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = emptyElevation),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(MR.strings.onboarding_sources_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredRepos.forEach { repo ->
                            RepoItem(
                                repo = repo,
                                isChecked = selectedRepos.contains(repo),
                                onToggle = {
                                    if (selectedRepos.contains(repo)) {
                                        selectedRepos.remove(repo)
                                    } else {
                                        selectedRepos.add(repo)
                                    }
                                },
                            )
                        }
                    }
                }

                // Informative tip — accent-tinted accent box (no card double-layer)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accent.copy(alpha = 0.08f))
                        .border(1.dp, colors.accent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(MR.strings.onboarding_sources_tip),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textPrimary,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun RepoItem(
        repo: PredefinedRepo,
        isChecked: Boolean,
        onToggle: () -> Unit,
    ) {
        val colors = AuroraTheme.colors
        val appHaptics = LocalAppHaptics.current

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
            label = "repo_${repo.name}_scale",
        )

        val cardBgColor = when {
            colors.isEInk -> if (isChecked) {
                resolveAuroraSurfaceColor(colors, AuroraSurfaceLevel.Strong)
            } else {
                resolveAuroraSurfaceColor(colors, AuroraSurfaceLevel.Subtle)
            }
            colors.isDark -> if (isChecked) {
                colors.accent.copy(alpha = 0.18f)
            } else {
                Color.White.copy(alpha = 0.05f)
            }
            else -> if (isChecked) {
                colors.accent.copy(alpha = 0.12f)
            } else {
                Color.White
            }
        }

        val cardBorderColor = when {
            colors.isEInk -> if (isChecked) {
                colors.accent
            } else {
                resolveAuroraMoreCardBorderColor(colors)
            }
            colors.isDark -> if (isChecked) {
                colors.accent.copy(alpha = 0.5f)
            } else {
                Color.White.copy(alpha = 0.08f)
            }
            else -> if (isChecked) {
                colors.accent.copy(alpha = 0.35f)
            } else {
                Color.Transparent
            }
        }

        // Floating elevation in light theme when not selected; 0.dp when selected to avoid stencil artifact
        val cardElevation = if (!colors.isDark && !colors.isEInk && !isChecked) {
            resolveAuroraElevation(colors, AuroraSurfaceLevel.Glass)
        } else {
            0.dp
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            border = BorderStroke(
                width = if (isChecked && !colors.isEInk) 1.5.dp else 1.dp,
                color = cardBorderColor,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
            onClick = {
                appHaptics.tap()
                onToggle()
            },
            interactionSource = interactionSource,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        appHaptics.tap()
                        onToggle()
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.accent,
                        uncheckedColor = colors.textSecondary,
                        checkmarkColor = colors.textOnAccent,
                    ),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = repo.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = repo.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }

                if (isChecked) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}
