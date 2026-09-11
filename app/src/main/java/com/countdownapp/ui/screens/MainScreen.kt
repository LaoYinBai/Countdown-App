package com.countdownapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countdownapp.data.entity.Event
import com.countdownapp.ui.components.AddEventDialog
import com.countdownapp.ui.components.EventCard
import com.countdownapp.ui.components.SwipeToDeleteContainer
import com.countdownapp.ui.theme.MoDiBorder
import com.countdownapp.ui.theme.MoDiColors
import com.countdownapp.ui.theme.MoDiRadius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    events: List<Event>,
    isEditMode: Boolean,
    selectedEventIds: Set<Long>,
    onAddEvent: (String, Long, Int, Int, String, Int, Int, Int, Boolean, Boolean) -> Unit,
    onDeleteEvent: (Event) -> Unit,
    onEventClick: (Event) -> Unit,
    onEnterEditMode: () -> Unit,
    onExitEditMode: () -> Unit,
    onToggleSelection: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onTogglePin: (Event) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Track which swipe is open (mutual exclusion)
    var openedEventId by remember { mutableStateOf<Long?>(null) }
    var closeRequestVersion by remember { mutableIntStateOf(0) }

    fun closeOpenSwipe() {
        if (openedEventId != null) {
            openedEventId = null
            closeRequestVersion++
        }
    }

    // LazyColumn scroll state for closing swipes on scroll
    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Close swipes when scroll begins
    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            closeOpenSwipe()
        }
    }

    BackHandler(enabled = openedEventId != null) {
        closeOpenSwipe()
    }

    BackHandler(enabled = isEditMode && openedEventId == null) {
        onExitEditMode()
    }

    Scaffold(
        // 滑动打开时，任何"没有被控件消费"的轻点都撤回滑动。
        // 挂在 Scaffold 根节点上：子控件（卡片、置顶/删除按钮、顶栏按钮、FAB）在 Main 传递阶段
        // 先拿到事件并消费，只有它们没消费的轻点才会冒泡到这里——因此语义正好是
        //「除置顶/删除按钮区以外，任何区域轻点即撤回」。
        // 未打开滑动时该检测器不生效，列表/卡片/滚动行为完全不受影响。
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(openedEventId, isEditMode) {
                if (openedEventId != null && !isEditMode) {
                    detectTapGestures { closeOpenSwipe() }
                }
            },
        // 保持透明，让根节点的墨晕底纹透出（墨堤的做法）
        containerColor = Color.Transparent,
        topBar = {
            if (isEditMode) {
                // Edit mode top bar
                TopAppBar(
                    title = {
                        Text(
                            text = "已选择 ${selectedEventIds.size} 项",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        TextButton(onClick = onExitEditMode) {
                            Text("取消")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { showDeleteConfirmDialog = true },
                            enabled = selectedEventIds.isNotEmpty()
                        ) {
                            Text(
                                text = "删除",
                                color = if (selectedEventIds.isNotEmpty()) MoDiColors.Cinnabar else MoDiColors.TextMuted
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            } else {
                // Normal top bar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "倒数日",
                            style = MaterialTheme.typography.displayLarge,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    actions = {
                        TextButton(onClick = {
                            closeOpenSwipe()
                            onEnterEditMode()
                        }) {
                            Text("编辑")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        },
        floatingActionButton = {
            if (!isEditMode) {
                FloatingActionButton(
                    onClick = {
                        // 与顶栏「编辑」一致：先撤回滑动，再执行本按钮的动作
                        closeOpenSwipe()
                        showAddDialog = true
                    },
                    containerColor = MoDiColors.InkOrange
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加事件",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        bottomBar = {
            if (isEditMode) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        val isAllSelected = events.isNotEmpty() && selectedEventIds.size == events.size
                        TextButton(
                            onClick = {
                                if (isAllSelected) {
                                    onDeselectAll()
                                } else {
                                    onSelectAll()
                                }
                            },
                            enabled = events.isNotEmpty()
                        ) {
                            Text(if (isAllSelected) "全不选" else "全选")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "还没有倒数日事件",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = events,
                        key = { it.id }
                    ) { event ->
                        val isOtherOpen = openedEventId != null && openedEventId != event.id

                        if (isEditMode) {
                            // Edit mode: checkbox + no swipe
                            EditModeEventCard(
                                event = event,
                                isSelected = selectedEventIds.contains(event.id),
                                onToggleSelection = { onToggleSelection(event.id) },
                                onClick = { onToggleSelection(event.id) }
                            )
                        } else {
                            SwipeToDeleteContainer(
                                event = event,
                                onDelete = onDeleteEvent,
                                onPinToggle = onTogglePin,
                                onCardClick = { clickedEvent ->
                                    if (openedEventId != null) {
                                        closeOpenSwipe()
                                    } else {
                                        onEventClick(clickedEvent)
                                    }
                                },
                                swipeEnabled = !isEditMode,
                                isOtherOpen = isOtherOpen,
                                closeRequestVersion = closeRequestVersion,
                                onOpenThis = { openedEventId = event.id },
                                onCloseThis = {
                                    if (openedEventId == event.id) {
                                        openedEventId = null
                                    }
                                }
                            ) { e, isOpen, onCardClickHandler ->
                                EventCard(
                                    event = e,
                                    isSwipedOpen = isOpen,
                                    onClick = { onCardClickHandler(e) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, targetDate, month, day, calType, lunarY, lunarM, lunarD, isLeap, isRepeat ->
                onAddEvent(name, targetDate, month, day, calType, lunarY, lunarM, lunarD, isLeap, isRepeat)
                showAddDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("确认删除") },
            text = { Text("确认删除 ${selectedEventIds.size} 个事件吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteSelected()
                    }
                ) {
                    Text("删除", color = MoDiColors.Cinnabar)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun EditModeEventCard(
    event: Event,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onClick: () -> Unit
) {
    val eventColor = com.countdownapp.util.generateEventColor(event.name)
    val days = com.countdownapp.util.calculateEventDays(event)
    val description = com.countdownapp.util.getDaysDescription(days)
    val isTall = event.isPinned

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(MoDiRadius.Card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(MoDiBorder.Width, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTall) 160.dp else 80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                modifier = Modifier.padding(start = 8.dp)
            )
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(eventColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (event.isPinned) {
                        Text(text = "📌 ", fontSize = 16.sp)
                    }
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
