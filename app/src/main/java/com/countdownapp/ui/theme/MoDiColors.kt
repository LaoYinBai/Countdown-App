package com.countdownapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 墨堤 / MoDi Connect 设计语言色彩令牌。
 *
 * 色值来源为两处互相独立的墨堤实现，二者逐字节一致：
 *  - Android 官方端 `modi-gitee/android/.../ui/theme/MoDiColors.kt`
 *  - 官网 `modi-web/styles/tokens.css` + `themes.css`
 * 对应《墨堤设计系统》卷一「墨堤色彩令牌」。
 *
 * 品牌三色与状态色深浅共用、不随主题变化——「品牌不换衣服」。
 * 本轮仅启用浅色主题「宣纸 · 昼堤」，故未定义深色「墨 · 夜堤」镜像令牌：
 * 未启用的令牌属于预建框架，等深色主题获得授权时再补。
 */
object MoDiColors {
    // ── 品牌三色 + 危险色（深浅共用） ──────────────────────────────
    /** 暖橘：唯一的「行动色」，主按钮、选中、强调。 */
    val InkOrange = Color(0xFFE8863C)

    /** 青绿：桥面上的颜色，辅助色。 */
    val BridgeGreen = Color(0xFF47937F)

    /** 浅蓝：河水，第三级。 */
    val WaterBlue = Color(0xFF86BFD8)

    /** 朱砂：危险按钮、错误、已过期语义。 */
    val Cinnabar = Color(0xFFC2452D)

    // ── 状态色（深浅共用） ────────────────────────────────────────
    val Success = Color(0xFF22C55E)
    val Warning = Color(0xFFF59E0B)
    val Error = Cinnabar

    // ── 浅色主题「宣纸 · 昼堤」 ───────────────────────────────────
    /** 窗口背景（宣纸）。 */
    val PaperDay = Color(0xFFF4F1E7)

    /** 一级卡片（裁好的纸）。 */
    val PaperCard = Color(0xFFFCFAF2)

    /** 二级面板 / 次级容器。 */
    val PaperCardSecondary = Color(0xFFEDE9DC)

    /** 墨线：边框描边色。 */
    val PaperBorder = Color(0xFFDCD5C3)

    /** 选中态背景（暖墨）。 */
    val AccentBg = Color(0xFFF6EDDC)

    /** 主文字（墨色）。 */
    val InkText = Color(0xFF242A2D)

    /** 次文字。 */
    val TextSecondary = Color(0xFF5C6862)

    /** 辅助文字。 */
    val TextMuted = Color(0xFF96A19A)
}
