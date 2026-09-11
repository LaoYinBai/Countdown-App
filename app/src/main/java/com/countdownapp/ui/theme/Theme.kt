package com.countdownapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 倒数日主题：墨堤 / MoDi Connect 设计语言实例化。
 *
 * 与墨堤 Android 官方端 `Theme.kt` 的 16 个槽位映射对齐，并**补齐墨堤主题里
 * 「被组件使用却从未定义」的槽位**——墨堤侧这些槽位实际落在 M3 基线调色板上
 * （例如底栏指示器与 OutlinedButton 描边色），属于既有缺陷；本项目显式定义，
 * 避免基线色渗入：
 *   - `outline` / `outlineVariant` → 墨线 [MoDiColors.PaperBorder]，决定 OutlinedButton 描边色
 *   - `primaryContainer` / `onPrimaryContainer` → 选中态底色 + 主文字色
 *   - `errorContainer` / `onErrorContainer` → 朱砂实底 + 白字
 *   - `inverseSurface` / `inverseOnSurface` / `scrim` / `surfaceTint` → 显式声明
 *
 * `surfaceContainer` 系列映射到「裁好的纸」，是墨堤用底色差而非投影做层级的手段。
 *
 * 本轮只启用浅色主题「宣纸 · 昼堤」：深色「墨 · 夜堤」未获授权，
 * 因此不按系统深浅切换，行为与重构前一致（重构前也恒为浅色）。
 */
private val MoDiLightColorScheme = lightColorScheme(
    // 品牌色：深浅共用，不随主题变
    primary = MoDiColors.InkOrange,
    onPrimary = Color.White,
    primaryContainer = MoDiColors.AccentBg,
    onPrimaryContainer = MoDiColors.InkText,
    inversePrimary = MoDiColors.InkOrange,

    secondary = MoDiColors.BridgeGreen,
    onSecondary = Color.White,
    secondaryContainer = MoDiColors.PaperCardSecondary,
    onSecondaryContainer = MoDiColors.InkText,

    tertiary = MoDiColors.WaterBlue,
    onTertiary = MoDiColors.InkText,
    tertiaryContainer = MoDiColors.PaperCardSecondary,
    onTertiaryContainer = MoDiColors.InkText,

    // 表面与文字
    background = MoDiColors.PaperDay,
    onBackground = MoDiColors.InkText,
    surface = MoDiColors.PaperDay,
    onSurface = MoDiColors.InkText,
    surfaceVariant = MoDiColors.PaperCardSecondary,
    onSurfaceVariant = MoDiColors.TextSecondary,
    surfaceTint = MoDiColors.InkOrange,
    inverseSurface = MoDiColors.InkText,
    inverseOnSurface = MoDiColors.PaperDay,

    surfaceBright = MoDiColors.PaperCard,
    surfaceDim = MoDiColors.PaperCardSecondary,
    surfaceContainerLowest = MoDiColors.PaperCard,
    surfaceContainerLow = MoDiColors.PaperDay,
    surfaceContainer = MoDiColors.PaperCard,
    surfaceContainerHigh = MoDiColors.PaperCardSecondary,
    surfaceContainerHighest = MoDiColors.PaperCardSecondary,

    // 危险语义：朱砂
    error = MoDiColors.Error,
    onError = Color.White,
    errorContainer = MoDiColors.Error,
    onErrorContainer = Color.White,

    // 墨线
    outline = MoDiColors.PaperBorder,
    outlineVariant = MoDiColors.PaperBorder,
    scrim = Color.Black
)

/** 保留原函数名与签名，调用方 MainActivity 无需改动。 */
@Composable
fun CountdownAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MoDiLightColorScheme,
        typography = MoDiTypography,
        shapes = MoDiShapes,
        content = content
    )
}
