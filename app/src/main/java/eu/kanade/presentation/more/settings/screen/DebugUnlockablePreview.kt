package eu.kanade.presentation.more.settings.screen

import eu.kanade.domain.ui.model.AppTheme

private val previewAuraUnlockables = setOf(
    "aura_harem",
    "aura_level_up",
    "aura_matrix",
)

private val previewPresetUnlockables = setOf(
    "profile_nickname_effect_aurora_crown",
    "profile_nickname_effect_glitch_rune",
    "profile_nickname_effect_cipher",
    "avatar_frame_neon",
    "avatar_frame_hologram",
    "avatar_frame_prismatic",
    "home_badge_orbit",
    "home_badge_crown",
    "home_badge_shuriken",
    "special_background_petal_storm",
    "special_background_neon_orbit",
)

private val previewThemeUnlockables by lazy {
    val hiddenThemes = AppTheme.entries
        .filter(AppTheme::isHidden)
        .flatMap(::themeUnlockableIds)
        .toSet()
    hiddenThemes
}

internal fun shouldShowTreasury(
    isDebugBuild: Boolean,
    unlockedUnlockables: Set<String>,
): Boolean {
    return isDebugBuild || unlockedUnlockables.isNotEmpty()
}

internal fun visibleUnlockablesForTreasuryPreview(
    debugBypassLocks: Boolean,
    unlockedUnlockables: Set<String>,
): Set<String> {
    if (!debugBypassLocks) return unlockedUnlockables

    return unlockedUnlockables + previewThemeUnlockables + previewAuraUnlockables + previewPresetUnlockables
}

internal fun isThemePreviewUnlocked(
    theme: AppTheme,
    unlockedUnlockables: Set<String>,
): Boolean {
    return themeUnlockableIds(theme).any(unlockedUnlockables::contains)
}

internal fun themeUnlockableIds(theme: AppTheme): Set<String> {
    return setOf(
        "theme_${theme.name}",
        "theme_${theme.name.lowercase()}",
    )
}
