package com.countdownapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.countdownapp.data.entity.Event
import com.countdownapp.ui.screens.EventDetailScreen
import com.countdownapp.ui.screens.MainScreen
import com.countdownapp.ui.theme.CountdownAppTheme
import com.countdownapp.ui.theme.InkTraceSurface
import com.countdownapp.ui.theme.MoDiMotion
import com.countdownapp.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CountdownAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 墨堤的墨晕底纹挂在根节点；下层屏幕需保持透明才能透出底纹。
                    InkTraceSurface {
                        CountdownAppContent()
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownAppContent(viewModel: MainViewModel = viewModel()) {
    val events by viewModel.events.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val selectedEventIds by viewModel.selectedEventIds.collectAsState()
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    // Animation for frosted glass overlay
    val overlayAlpha by animateFloatAsState(
        targetValue = if (selectedEvent != null) 1f else 0f,
        animationSpec = tween(durationMillis = MoDiMotion.Slow),
        label = "overlayAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MainScreen(
            events = events,
            isEditMode = isEditMode,
            selectedEventIds = selectedEventIds,
            onAddEvent = { name, targetDate, month, day, calType, lunarY, lunarM, lunarD, isLeap, isRepeat ->
                viewModel.addEvent(
                    name = name,
                    targetDate = targetDate,
                    recurringMonth = month,
                    recurringDay = day,
                    calendarType = calType,
                    lunarYear = lunarY,
                    lunarMonth = lunarM,
                    lunarDay = lunarD,
                    isLeapMonth = isLeap,
                    isRepeatYearly = isRepeat
                )
            },
            onDeleteEvent = { event ->
                viewModel.deleteEvent(event)
            },
            onEventClick = { event ->
                selectedEvent = event
            },
            onEnterEditMode = { viewModel.enterEditMode() },
            onExitEditMode = { viewModel.exitEditMode() },
            onToggleSelection = { eventId -> viewModel.toggleSelection(eventId) },
            onSelectAll = { viewModel.selectAll() },
            onDeselectAll = { viewModel.deselectAll() },
            onDeleteSelected = {
                viewModel.deleteSelectedEvents()
            },
            onTogglePin = { event ->
                viewModel.togglePin(event)
            }
        )

        // Frosted glass overlay with blur effect
        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f * overlayAlpha))
                    .blur(20.dp)
            )
        }

        // Detail card
        if (selectedEvent != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                EventDetailScreen(
                    event = selectedEvent!!,
                    visible = true,
                    onBack = { selectedEvent = null },
                    onUpdateBackground = { path ->
                        viewModel.updateBackground(selectedEvent!!.id, path)
                        selectedEvent = selectedEvent!!.copy(backgroundImagePath = path)
                    },
                    onUpdateName = { name ->
                        viewModel.updateName(selectedEvent!!.id, name)
                        selectedEvent = selectedEvent!!.copy(name = name)
                    },
                    onUpdateTargetDate = { newDate, month, day, calendarType, lunarYear, lunarMonth, lunarDay, isLeap, isRepeat ->
                        viewModel.updateTargetDate(
                            eventId = selectedEvent!!.id,
                            newDate = newDate,
                            month = month,
                            day = day,
                            calendarType = calendarType,
                            lunarYear = lunarYear,
                            lunarMonth = lunarMonth,
                            lunarDay = lunarDay,
                            isLeapMonth = isLeap,
                            isRepeatYearly = isRepeat
                        )
                        selectedEvent = selectedEvent!!.copy(
                            targetDate = newDate,
                            recurringMonth = month,
                            recurringDay = day,
                            calendarType = calendarType,
                            lunarYear = lunarYear,
                            lunarMonth = lunarMonth,
                            lunarDay = lunarDay,
                            isLeapMonth = isLeap,
                            isRepeatYearly = isRepeat
                        )
                    }
                )
            }
        }
    }
}
