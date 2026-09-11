package com.countdownapp.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.core.view.drawToBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.countdownapp.data.entity.Event
import com.countdownapp.util.calculateEventDays
import com.countdownapp.util.formatLunarDate
import com.countdownapp.util.formatLunarDayName
import com.countdownapp.util.formatLunarMonthName
import com.countdownapp.util.getDaysDescription
import com.countdownapp.util.generateEventColor
import com.countdownapp.util.lunarFromMillis
import com.countdownapp.util.lunarToSolarMillis
import com.countdownapp.ui.theme.MoDiBorder
import com.countdownapp.ui.theme.MoDiColors
import com.countdownapp.ui.theme.MoDiMotion
import com.countdownapp.ui.theme.MoDiRadius
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    visible: Boolean,
    onBack: () -> Unit,
    onUpdateBackground: (String?) -> Unit,
    onUpdateName: (String) -> Unit,
    onUpdateTargetDate: (Long, Int, Int, String, Int, Int, Int, Boolean, Boolean) -> Unit
) {
    val context = LocalContext.current
    val rootView = LocalView.current
    val days = calculateEventDays(event)
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()) }
    val dateFormatterNoYear = remember { SimpleDateFormat("MM月dd日", Locale.getDefault()) }
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

    var backgroundPath by remember { mutableStateOf(event.backgroundImagePath) }
    var textColor by remember { mutableStateOf(Color.Black) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNameEditor by remember { mutableStateOf(false) }
    var editingName by remember(event.name) { mutableStateOf(event.name) }
    var cardBoundsInWindow by remember { mutableStateOf<Rect?>(null) }
    val scope = rememberCoroutineScope()
    fun saveImageToGallery() {
        scope.launch {
            delay(120)
            val saved = exportVisibleCardImage(context, rootView, cardBoundsInWindow)
            Toast.makeText(
                context,
                if (saved) "图片已保存到相册" else "保存失败",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    val savePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            saveImageToGallery()
        } else {
            Toast.makeText(context, "需要存储权限才能保存图片", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(event.backgroundImagePath) {
        backgroundPath = event.backgroundImagePath
    }

    // 文字色跟随用户所选背景图的亮度：这是"在任意照片上保证可读性"的对比度决策，
    // 不属于主题样式，故保持黑/白字面值，不纳入令牌。
    LaunchedEffect(backgroundPath) {
        if (backgroundPath != null) {
            val brightness = withContext(Dispatchers.IO) {
                calculateImageBrightness(backgroundPath!!)
            }
            textColor = if (brightness > 128) Color.Black else Color.White
        } else {
            textColor = Color.Black
        }
    }

    // 天数语义色：朱砂=已过去，成功绿=未到来（语义与重构前一致，仅取墨堤令牌色值）
    val daysTextColor = if (days < 0) MoDiColors.Cinnabar else MoDiColors.Success

    // Handle Android back button
    BackHandler(enabled = visible) {
        onBack()
    }

    val expansionProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = MoDiMotion.Slow),
        label = "expansion"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = MoDiMotion.Slow),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                onClick = onBack
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(520.dp)
                .onGloballyPositioned { coordinates ->
                    cardBoundsInWindow = coordinates.boundsInWindow()
                }
                .graphicsLayer {
                    scaleX = 0.8f + 0.2f * expansionProgress
                    scaleY = 0.8f + 0.2f * expansionProgress
                    alpha = animatedAlpha
                },
            shape = RoundedCornerShape(MoDiRadius.Card),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(MoDiBorder.Width, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (backgroundPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(backgroundPath!!))
                            .size(Size.ORIGINAL)
                            .allowHardware(false)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (days < 0) "距离${event.name}已经" else "距离${event.name}还有",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (days < 0) (-days).toString() else days.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            color = daysTextColor,
                            textAlign = TextAlign.Center
                        )
                    }

                    val hasYear = event.recurringMonth < 0
                    Text(
                        text = if (days < 0) {
                            if (lunarDateText != null) {
                                "起始日 $lunarDateText"
                            } else if (hasYear) {
                                "起始日 ${dateFormatter.format(Date(event.targetDate))}"
                            } else {
                                "起始日 ${dateFormatterNoYear.format(Date(event.targetDate))}"
                            }
                        } else {
                            if (lunarDateText != null) {
                                "目标日 $lunarDateText"
                            } else if (hasYear) {
                                "目标日 ${dateFormatter.format(Date(event.targetDate))}"
                            } else {
                                "目标日 ${dateFormatterNoYear.format(Date(event.targetDate))}"
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp,
                        color = daysTextColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                editingName = event.name
                                showNameEditor = true
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Text("修改名称", style = MaterialTheme.typography.labelLarge, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Text("修改日期", style = MaterialTheme.typography.labelLarge, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val imagePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        uri?.let { selectedUri ->
                            val inputStream = context.contentResolver.openInputStream(selectedUri)
                            val file = File(context.filesDir, "bg_${event.id}_${System.currentTimeMillis()}.jpg")
                            inputStream?.use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            val newPath = file.absolutePath
                            backgroundPath = newPath
                            onUpdateBackground(newPath)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Text(
                                if (backgroundPath != null) "更换背景" else "设置背景",
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (backgroundPath != null) {
                            OutlinedButton(
                                onClick = {
                                    backgroundPath = null
                                    onUpdateBackground(null)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Text("恢复默认", style = MaterialTheme.typography.labelLarge, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Circular download button — top right corner
                IconButton(
                    onClick = {
                        if (
                            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            savePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            saveImageToGallery()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = CircleShape
                        )
                        .border(
                            width = MoDiBorder.Width,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "保存图片",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val currentLunar = remember { lunarFromMillis(System.currentTimeMillis()) }
        val eventSolar = remember(event.targetDate) {
            Calendar.getInstance().apply { timeInMillis = event.targetDate }
        }
        val eventLunar = remember(event.targetDate) { lunarFromMillis(event.targetDate) }
        var calendarType by remember { mutableStateOf(event.calendarType) }
        var selectedYear by remember { mutableStateOf(currentYear) }
        var selectedMonth by remember { mutableStateOf(currentMonth) }
        var selectedDay by remember { mutableStateOf(currentDay.coerceAtLeast(1)) }
        var selectYear by remember { mutableStateOf(true) }
        var isRepeatYearly by remember { mutableStateOf(event.isRepeatYearly) }

        LaunchedEffect(showDatePicker) {
            if (showDatePicker) {
                calendarType = event.calendarType
                if (event.calendarType == "lunar") {
                    selectedYear = if (event.lunarYear > 0) event.lunarYear else eventLunar.year
                    selectedMonth = (if (event.lunarMonth > 0) event.lunarMonth else eventLunar.month) - 1
                    selectedDay = if (event.lunarDay > 0) event.lunarDay else eventLunar.day
                    selectYear = !event.isRepeatYearly
                    isRepeatYearly = event.isRepeatYearly
                } else {
                    selectedYear = eventSolar.get(Calendar.YEAR)
                    selectedMonth = eventSolar.get(Calendar.MONTH)
                    selectedDay = eventSolar.get(Calendar.DAY_OF_MONTH)
                    selectYear = event.recurringMonth < 0
                    isRepeatYearly = false
                }
            }
        }

        val years = (1900..2099).toList()
        val months = (0..11).toList()
        val dateDays = if (calendarType == "lunar") (1..30).toList() else (1..31).toList()
        val isSelectedLunarDateValid = calendarType != "lunar" ||
            lunarToSolarMillis(selectedYear, selectedMonth + 1, selectedDay) != null

        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("修改目标日期") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = {
                                calendarType = "solar"
                                selectedYear = eventSolar.get(Calendar.YEAR)
                                selectedMonth = eventSolar.get(Calendar.MONTH)
                                selectedDay = eventSolar.get(Calendar.DAY_OF_MONTH)
                                selectYear = true
                                isRepeatYearly = false
                            },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (calendarType == "solar") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (calendarType == "solar") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("阳历")
                        }
                        TextButton(
                            onClick = {
                                calendarType = "lunar"
                                if (selectedDay > 30) {
                                    selectedDay = 30
                                }
                                selectedYear = if (event.lunarYear > 0) event.lunarYear else currentLunar.year
                                selectedMonth = (if (event.lunarMonth > 0) event.lunarMonth else currentLunar.month) - 1
                                selectedDay = if (event.lunarDay > 0) event.lunarDay else currentLunar.day
                                selectYear = !event.isRepeatYearly
                                isRepeatYearly = event.isRepeatYearly
                            },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (calendarType == "lunar") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (calendarType == "lunar") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("农历")
                        }
                    }
                    if (calendarType == "lunar") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatLunarDate(
                                year = selectedYear,
                                month = selectedMonth + 1,
                                day = selectedDay,
                                includeYear = selectYear
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectYear,
                            onCheckedChange = {
                                selectYear = it
                                if (!it) selectedYear = currentYear
                            }
                        )
                        Text("选择年份")
                    }
                    if (calendarType == "lunar") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isRepeatYearly,
                                onCheckedChange = {
                                    isRepeatYearly = it
                                    if (it) selectYear = false
                                }
                            )
                            Text("每年重复（农历生日）")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (selectYear) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(if (calendarType == "lunar") "农历年" else "年", style = MaterialTheme.typography.bodySmall, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                WheelPicker(
                                    items = years,
                                    selectedItem = selectedYear,
                                    onSelectedItemChange = { selectedYear = it },
                                    modifier = Modifier.height(150.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(if (calendarType == "lunar") "农历月" else "月", style = MaterialTheme.typography.bodySmall, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            WheelPicker(
                                items = months.map { it + 1 },
                                selectedItem = selectedMonth + 1,
                                onSelectedItemChange = { selectedMonth = it - 1 },
                                modifier = Modifier.height(150.dp),
                                itemLabel = {
                                    if (calendarType == "lunar") formatLunarMonthName(it) else it.toString()
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(if (calendarType == "lunar") "农历日" else "日", style = MaterialTheme.typography.bodySmall, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            WheelPicker(
                                items = dateDays,
                                selectedItem = selectedDay,
                                onSelectedItemChange = { selectedDay = it },
                                modifier = Modifier.height(150.dp),
                                itemLabel = {
                                    if (calendarType == "lunar") formatLunarDayName(it) else it.toString()
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = isSelectedLunarDateValid,
                    onClick = {
                        val finalDate = if (calendarType == "lunar") {
                            val lunarMonthValue = selectedMonth + 1
                            val lunarDayValue = selectedDay
                            val lunarYearValue = if (selectYear && !isRepeatYearly) selectedYear else {
                                val thisYearDate = lunarToSolarMillis(currentYear, lunarMonthValue, lunarDayValue)
                                val today = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                if (thisYearDate != null && thisYearDate > today) currentYear else currentYear + 1
                            }
                            lunarToSolarMillis(lunarYearValue, lunarMonthValue, lunarDayValue)
                                ?: (System.currentTimeMillis() + 86400000)
                        } else if (!selectYear) {
                            val today = Calendar.getInstance()
                            val thisYearDate = Calendar.getInstance().apply {
                                set(Calendar.YEAR, currentYear)
                                set(Calendar.MONTH, selectedMonth)
                                set(Calendar.DAY_OF_MONTH, selectedDay)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            if (thisYearDate.timeInMillis > today.timeInMillis) {
                                thisYearDate.timeInMillis
                            } else {
                                val nextYearDate = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, currentYear + 1)
                                    set(Calendar.MONTH, selectedMonth)
                                    set(Calendar.DAY_OF_MONTH, selectedDay)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                nextYearDate.timeInMillis
                            }
                        } else {
                            Calendar.getInstance().apply {
                                set(Calendar.YEAR, selectedYear)
                                set(Calendar.MONTH, selectedMonth)
                                set(Calendar.DAY_OF_MONTH, selectedDay)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                        }
                        val monthToSave = if (calendarType == "solar" && !selectYear) selectedMonth else -1
                        val dayToSave = if (calendarType == "solar" && !selectYear) selectedDay else -1
                        val lunarMonthValue = if (calendarType == "lunar") selectedMonth + 1 else 0
                        val lunarDayValue = if (calendarType == "lunar") selectedDay else 0
                        val lunarYearValue = if (calendarType == "lunar") {
                            if (selectYear && !isRepeatYearly) selectedYear else lunarFromMillis(finalDate).year
                        } else {
                            0
                        }
                        onUpdateTargetDate(
                            finalDate,
                            monthToSave,
                            dayToSave,
                            calendarType,
                            lunarYearValue,
                            lunarMonthValue,
                            lunarDayValue,
                            false,
                            calendarType == "lunar" && isRepeatYearly
                        )
                        showDatePicker = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showNameEditor) {
        AlertDialog(
            onDismissRequest = { showNameEditor = false },
            title = { Text("修改事件名称") },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("事件名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = editingName.isNotBlank(),
                    onClick = {
                        onUpdateName(editingName.trim())
                        showNameEditor = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameEditor = false }) {
                    Text("取消")
                }
            }
        )
    }
}


@Composable
fun WheelPicker(
    items: List<Int>,
    selectedItem: Int,
    onSelectedItemChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemLabel: (Int) -> String = { it.toString() }
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedItem) {
        val index = items.indexOf(selectedItem)
        if (index >= 0) {
            listState.animateScrollToItem(index, scrollOffset = -60)
        }
    }

    LaunchedEffect(Unit) {
        val index = items.indexOf(selectedItem)
        if (index >= 0) {
            listState.scrollToItem(index, scrollOffset = -60)
        }
    }

    Box(
        modifier = modifier
            .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val item = items[index]
                val isSelected = item == selectedItem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clickable {
                            onSelectedItemChange(item)
                            scope.launch {
                                listState.animateScrollToItem(index, scrollOffset = -60)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemLabel(item),
                        fontSize = if (isSelected) 22.sp else 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private suspend fun exportVisibleCardImage(
    context: Context,
    rootView: View,
    cardBounds: Rect?
): Boolean = withContext(Dispatchers.Main) {
    val bounds = cardBounds ?: return@withContext false
    runCatching {
        val fullBitmap = rootView.drawToBitmap(Bitmap.Config.ARGB_8888)

        // boundsInWindow() is relative to window origin (0,0 = window top-left).
        // rootView.drawToBitmap() captures from root view origin (0,0 = root view top-left).
        // Account for status bar offset to align the crop region correctly.
        val statusBarHeight = context.resources.getDimensionPixelSize(
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        ).coerceAtLeast(0)

        val left = bounds.left.toInt().coerceIn(0, fullBitmap.width - 1)
        val top = (bounds.top - statusBarHeight).toInt().coerceIn(0, fullBitmap.height - 1)
        val right = bounds.right.toInt().coerceIn(left + 1, fullBitmap.width)
        val bottom = (bounds.bottom - statusBarHeight).toInt().coerceIn(top + 1, fullBitmap.height)

        val croppedBitmap = Bitmap.createBitmap(fullBitmap, left, top, right - left, bottom - top)
        fullBitmap.recycle()
        val saved = withContext(Dispatchers.IO) {
            saveBitmapToGallery(context, croppedBitmap)
        }
        croppedBitmap.recycle()
        saved
    }.getOrDefault(false)
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean {
    val fileName = "countdown_${System.currentTimeMillis()}.png"
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/CountdownApp")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        resolver.openOutputStream(uri)?.use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        } ?: return false
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        true
    } else {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CountdownApp")
        if (!dir.exists() && !dir.mkdirs()) return false
        val file = File(dir, fileName)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DATA, file.absolutePath)
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        true
    }
}

private suspend fun calculateImageBrightness(imagePath: String): Int {
    return withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 8
            }
            val bitmap = BitmapFactory.decodeFile(imagePath, options) ?: return@withContext 128

            var totalBrightness = 0L
            var pixelCount = 0

            val stepX = bitmap.width / 20.coerceAtLeast(1)
            val stepY = bitmap.height / 20.coerceAtLeast(1)

            for (x in 0 until bitmap.width step stepX.coerceAtLeast(1)) {
                for (y in 0 until bitmap.height step stepY.coerceAtLeast(1)) {
                    val pixel = bitmap.getPixel(x, y)
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    totalBrightness += (0.299 * r + 0.587 * g + 0.114 * b).toLong()
                    pixelCount++
                }
            }

            bitmap.recycle()

            if (pixelCount > 0) (totalBrightness / pixelCount).toInt() else 128
        } catch (e: Exception) {
            128
        }
    }
}
