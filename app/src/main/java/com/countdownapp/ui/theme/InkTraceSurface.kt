package com.countdownapp.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

/**
 * 墨堤「墨晕」底纹层：2% 噪点 + 微斜水纹。
 *
 * 移植自墨堤 Android 官方端 `ui/theme/InkTraceSurface.kt`，绘制参数收敛到 [MoDiTrace]。
 * 墨堤的实现要点（原样保留）：
 *  - 噪点位置用行列取模的**确定性伪随机**，不用 Random，保证重组/重绘不闪烁
 *  - 每 3 个点中有一个点径更大（1.2 : 0.7）
 *  - 5 条水纹线等距分布在 12%~88% 高度处，右端比左端略高
 *  - 取色全部走主题令牌，深浅色自适应
 *
 * 调用方需要让底纹透出：容器底色用 Color.Transparent（墨堤的做法），
 * 否则底纹会被不透明背景盖住。
 */
@Composable
fun InkTraceSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(colors.background)

            val step = MoDiTrace.Grid.toPx()
            var row = 0
            var y = step * .5f
            while (y < size.height) {
                var column = 0
                var x = step * .5f
                while (x < size.width) {
                    val jitterX = (((row * 17 + column * 11) % 9) - 4) * density
                    val jitterY = (((row * 7 + column * 19) % 7) - 3) * density
                    drawCircle(
                        color = colors.onBackground.copy(alpha = MoDiTrace.NoiseAlpha),
                        radius = if ((row + column) % 3 == 0) 1.2f * density else .7f * density,
                        center = Offset(x + jitterX, y + jitterY)
                    )
                    x += step
                    column++
                }
                y += step
                row++
            }

            repeat(MoDiTrace.WashLines) { index ->
                val lineY = size.height * (.12f + index * .19f)
                drawLine(
                    color = colors.onBackground.copy(alpha = MoDiTrace.WashAlpha),
                    start = Offset(-20f, lineY),
                    end = Offset(size.width + 20f, lineY - 10.dp.toPx()),
                    strokeWidth = MoDiTrace.WashStroke.toPx()
                )
            }
        }
        content()
    }
}
