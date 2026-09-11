package com.countdownapp.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 墨堤 / MoDi Connect 设计语言间距、圆角、描边与动效令牌。
 *
 * 数值来源为墨堤 Android 官方端与官网 token 层的实测值：
 *  - 官网 `modi-web/styles/tokens.css`：`--radius-card: 10px`、`--border-card: 1px solid`、
 *    `--duration-fast/base/ink/theme`、`--easing-standard`
 *  - Android 端间距阶梯实测为 4/8/10/12/16/20/24/32，容器高 48/56/64
 * 对应《墨堤设计系统》第四/五/六节。
 *
 * 关于卡片圆角：文档卷三 3.2 与官网 token 用 10px，墨堤 Android 端实际用 12dp。
 * 本项目采用 12dp，与墨堤 Android 端同平台保持一致（P0 已确认的决策）。
 */
object MoDiSpacing {
    val XS = 4.dp
    val S = 8.dp
    /** 卡片网格专用间隙，墨堤唯一非 4 倍数的常规间距。 */
    val CardGap = 10.dp
    val M = 12.dp
    /** 页面左右边距与行内边距的主力值。 */
    val L = 16.dp
    val XL = 20.dp
    val XXL = 24.dp
    val XXXL = 32.dp

    /** 触控目标标准高。 */
    val TouchTarget = 48.dp
    /** 列表行高。 */
    val RowHeight = 56.dp
    /** 标题栏高。 */
    val HeaderHeight = 64.dp
}

object MoDiRadius {
    /** 卡片圆角（P0 决策：12dp，与墨堤 Android 端一致）。 */
    val Card = 12.dp
    val Button = 8.dp
    val Small = 8.dp
    val Large = 16.dp
    /** M3 默认的对话框圆角，保持不变。 */
    val Dialog = 28.dp
    val Pill = 999.dp
}

object MoDiBorder {
    /** 墨线描边宽度。 */
    val Width = 1.dp
}

/**
 * 墨堤把形状体系收敛到 MaterialTheme.shapes。
 * 注意墨堤 Android 端从未覆写 shapes，圆角是散落硬编码；此处把它收敛为体系。
 * extraLarge 保持 M3 默认值，使 AlertDialog 等库组件外观不变。
 */
val MoDiShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(MoDiRadius.Small),
    medium = RoundedCornerShape(MoDiRadius.Card),
    large = RoundedCornerShape(MoDiRadius.Large),
    extraLarge = RoundedCornerShape(MoDiRadius.Dialog)
)

/** 墨堤动效令牌。EasingStandard 与 Compose 内建的 FastOutSlowInEasing 是同一条贝塞尔曲线。 */
object MoDiMotion {
    /** 按下反馈。 */
    const val Instant = 100
    /** hover、弹层收起。 */
    const val Fast = 150
    /** 胶囊滑动、拨杆、弹层展开。 */
    const val Base = 200
    /** 降级淡变、置灰、遮罩淡入。 */
    const val Slow = 300
    /** 页面元素显现。 */
    const val Page = 240

    /** 快起柔落，默认缓动：cubic-bezier(0.4, 0, 0.2, 1)。 */
    val Standard: Easing = FastOutSlowInEasing

    /** 弹层展开微过冲：cubic-bezier(0.34, 1.56, 0.64, 1)。 */
    val Overshoot: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
}

/** 墨堤「墨晕」底纹参数，取值与墨堤 Android 端 InkTraceSurface 实现一致。 */
object MoDiTrace {
    /** 噪点栅格步长。 */
    val Grid = 22.dp
    /** 噪点不透明度。 */
    const val NoiseAlpha = 0.018f
    /** 水纹线不透明度。 */
    const val WashAlpha = 0.012f
    /** 水纹线条数。 */
    const val WashLines = 5
    /** 水纹线宽。 */
    val WashStroke = 0.7.dp
}
