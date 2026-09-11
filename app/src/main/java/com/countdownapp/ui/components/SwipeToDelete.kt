package com.countdownapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.countdownapp.data.entity.Event
import com.countdownapp.ui.theme.MoDiColors
import kotlin.math.roundToInt

private const val SWIPE_THRESHOLD_DP = 40
private const val BUTTON_WIDTH_DP = 80

@Composable
fun SwipeToDeleteContainer(
    event: Event,
    onDelete: (Event) -> Unit,
    onPinToggle: (Event) -> Unit,
    onCardClick: (Event) -> Unit,
    swipeEnabled: Boolean = true,
    isOtherOpen: Boolean = false,
    closeRequestVersion: Int = 0,
    onOpenThis: () -> Unit = {},
    onCloseThis: () -> Unit = {},
    content: @Composable (Event, Boolean, (Event) -> Unit) -> Unit
) {
    val density = LocalDensity.current
    val buttonWidthPx = with(density) { BUTTON_WIDTH_DP.dp.toPx() }
    val thresholdPx = with(density) { SWIPE_THRESHOLD_DP.dp.toPx() }

    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isOpen by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = spring(),
        label = "swipeOffset"
    )

    // Force close when another item opens
    LaunchedEffect(isOtherOpen) {
        if (isOtherOpen && isOpen) {
            isOpen = false
            dragOffset = 0f
            onCloseThis()
        }
    }

    LaunchedEffect(closeRequestVersion) {
        if (isOpen) {
            isOpen = false
            dragOffset = 0f
            onCloseThis()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (event.isPinned) 172.dp else 92.dp)
            .pointerInput(swipeEnabled) {
                if (!swipeEnabled) return@pointerInput

                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            // Left swipe (finger left, card left, dragOffset < 0)
                            dragOffset < -thresholdPx -> {
                                dragOffset = -buttonWidthPx
                                isOpen = true
                                onOpenThis()
                            }
                            // Right swipe (finger right, card right, dragOffset > 0)
                            dragOffset > thresholdPx -> {
                                dragOffset = buttonWidthPx
                                isOpen = true
                                onOpenThis()
                            }
                            else -> {
                                isOpen = false
                                dragOffset = 0f
                            }
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        if (isOpen) {
                            val newOffset = dragOffset + dragAmount
                            if (dragOffset < 0) {
                                dragOffset = newOffset.coerceIn(-buttonWidthPx, 0f)
                            } else {
                                dragOffset = newOffset.coerceIn(0f, buttonWidthPx)
                            }
                        } else {
                            dragOffset = (dragOffset + dragAmount).coerceIn(-buttonWidthPx, buttonWidthPx)
                        }
                    }
                )
            }
    ) {
        // LEFT button — revealed by RIGHT swipe (dragOffset > 0)
        // Positioned LEFT in Box, shows when card slides right
        val leftBgAlpha = (animatedOffset / buttonWidthPx).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .matchParentSize()
                .zIndex(if (dragOffset > 0f) 1f else 0f)
                .background(MoDiColors.BridgeGreen.copy(alpha = leftBgAlpha)),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .width(BUTTON_WIDTH_DP.dp)
                    .fillMaxHeight()
                    .clickable(enabled = dragOffset > thresholdPx) {
                        onPinToggle(event)
                        isOpen = false
                        dragOffset = 0f
                        onCloseThis()
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (event.isPinned) "取消置顶" else "置顶",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(start = 24.dp)
                )
            }
        }

        // RIGHT button — revealed by LEFT swipe (dragOffset < 0)
        // Positioned RIGHT in Box, shows when card slides left
        val rightBgAlpha = (-animatedOffset / buttonWidthPx).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .matchParentSize()
                .zIndex(if (dragOffset < 0f) 1f else 0f)
                .background(MoDiColors.Cinnabar.copy(alpha = rightBgAlpha)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .width(BUTTON_WIDTH_DP.dp)
                    .fillMaxHeight()
                    .clickable(enabled = isOpen && dragOffset < 0) {
                        onDelete(event)
                        isOpen = false
                        dragOffset = 0f
                        onCloseThis()
                    },
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "删除",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }

        // Main card — on top of buttons
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .fillMaxWidth()
                .zIndex(2f)
                .clickable {
                    if (isOpen) {
                        isOpen = false
                        dragOffset = 0f
                        onCloseThis()
                    } else {
                        onCardClick(event)
                    }
                }
        ) {
            content(event, isOpen) { clickedEvent ->
                if (!isOpen) {
                    onCardClick(clickedEvent)
                }
            }
        }
    }
}
