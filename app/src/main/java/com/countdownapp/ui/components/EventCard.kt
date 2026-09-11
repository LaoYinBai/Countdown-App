package com.countdownapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countdownapp.data.entity.Event
import com.countdownapp.util.calculateEventDays
import com.countdownapp.util.formatLunarDate
import com.countdownapp.util.generateEventColor
import com.countdownapp.util.getDaysDescription
import com.countdownapp.ui.theme.MoDiBorder
import com.countdownapp.ui.theme.MoDiColors
import com.countdownapp.ui.theme.MoDiRadius

@Composable
fun EventCard(
    event: Event,
    isSwipedOpen: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eventColor = generateEventColor(event.name)
    val days = calculateEventDays(event)
    val description = getDaysDescription(days)
    val lunarDateText = if (event.calendarType == "lunar" && event.lunarYear > 0) {
        formatLunarDate(
            year = event.lunarYear,
            month = event.lunarMonth,
            day = event.lunarDay,
            includeYear = !event.isRepeatYearly
        )
    } else {
        null
    }

    val isPinned = event.isPinned
    val isTall = isPinned // Tall card for pinned events
    val dateText = if (lunarDateText != null) {
        lunarDateText
    } else if (event.recurringMonth >= 0) {
        "${event.recurringMonth + 1}月${event.recurringDay}日"
    } else {
        val date = java.util.Date(event.targetDate)
        java.text.SimpleDateFormat("yyyy年MM月dd日", java.util.Locale.getDefault()).format(date)
    }
    // 语义色：朱砂=已过去，成功绿=未到来（语义与重构前一致，仅取墨堤令牌色值）
    val daysColor = if (days < 0) MoDiColors.Cinnabar else MoDiColors.Success
    val cardModifier = if (isSwipedOpen) {
        modifier
    } else {
        modifier.clickable(onClick = onClick)
    }

    Card(
        modifier = cardModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(if (isTall) 160.dp else 80.dp),
        shape = RoundedCornerShape(MoDiRadius.Card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(MoDiBorder.Width, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(eventColor)
            )

            if (isTall) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📌 ${event.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Column {
                            Text(
                                text = dateText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = description,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (days < 0) (-days).toString() else days.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Bold,
                            color = daysColor,
                            lineHeight = 56.sp
                        )
                        Text(
                            text = "天",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lunarDateText?.let { "$it · $description" } ?: description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
