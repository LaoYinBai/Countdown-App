package com.countdownapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.countdownapp.R

/**
 * 墨堤字体分工。
 *
 * 墨堤的完整体系是五字体（匾额大楷 / 诗文霞鹜文楷 / 注释仿宋 / 目录明體 / 正文思源宋体）。
 * 本项目取其中三款（P0 已确认的决策），缺失的两个角色按语义就近回落到 `body`：
 *  - `annotation`（明體，用于图注）→ 回落 `body`
 *  - `default`（思源宋体）→ 回落 `body`
 *
 * 字体文件来自墨堤仓库 `assets/fonts/android-res/font/`，sha256 与
 * `assets/fonts/font-artifacts.lock.json` 记录一致。三款均为单字重注册，
 * 与墨堤实现一致：SemiBold / Bold 由系统合成，没有真实粗细轴。
 */
object MoDiFontFamilies {
    /** 匾额 · 大楷：页面标题、展示大字。 */
    val title = FontFamily(Font(R.font.modi_title_alimama_dongfang_dakai, FontWeight.Normal))

    /** 诗文 · 霞鹜文楷：列表行、卡片标题、按钮等功能性文字。 */
    val function = FontFamily(Font(R.font.modi_function_lxgw_wenkai, FontWeight.Normal))

    /** 注释 · 朱雀仿宋：正文、图注、次要说明。 */
    val body = FontFamily(Font(R.font.modi_body_zhuque_fangsong, FontWeight.Normal))
}

/**
 * 与墨堤 Android 端 `MoDiTypography` 逐槽位对齐；仅把墨堤未随附的两个字族
 * 按语义回落到 `body`。所有 15 个槽位都显式声明，避免 Roboto 从基线上渗入。
 */
val MoDiTypography = Typography(
    // —— 大楷：品牌展示与页面标题 ——
    displayLarge = TextStyle(
        fontFamily = MoDiFontFamilies.title,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 31.sp
    ),
    displayMedium = TextStyle(
        fontFamily = MoDiFontFamilies.body,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = MoDiFontFamilies.title,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),

    // —— 大楷：段落级标题 ——
    headlineLarge = TextStyle(
        fontFamily = MoDiFontFamilies.body,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MoDiFontFamilies.body,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = MoDiFontFamilies.title,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),

    // —— 霞鹜文楷：卡片标题与列表行标题 ——
    titleLarge = TextStyle(
        fontFamily = MoDiFontFamilies.function,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MoDiFontFamilies.function,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MoDiFontFamilies.function,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),

    // —— 朱雀仿宋：正文与图注 ——
    bodyLarge = TextStyle(
        fontFamily = MoDiFontFamilies.body,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MoDiFontFamilies.body,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MoDiFontFamilies.body,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 19.sp
    ),

    // —— 霞鹜文楷：按钮与标签 ——
    labelLarge = TextStyle(
        fontFamily = MoDiFontFamilies.function,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MoDiFontFamilies.function,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MoDiFontFamilies.function,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
