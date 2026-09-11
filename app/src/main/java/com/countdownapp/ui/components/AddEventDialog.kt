package com.countdownapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.countdownapp.util.formatLunarDate
import com.countdownapp.util.formatLunarDayName
import com.countdownapp.util.formatLunarMonthName
import com.countdownapp.util.lunarFromMillis
import com.countdownapp.util.lunarToSolarMillis
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetDate: Long, month: Int, day: Int, calendarType: String, lunarYear: Int, lunarMonth: Int, lunarDay: Int, isLeapMonth: Boolean, isRepeatYearly: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Calendar type tab: "solar" or "lunar"
    var calendarType by remember { mutableStateOf("solar") }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val currentLunar = remember { lunarFromMillis(System.currentTimeMillis()) }

    var selectedYear by remember { mutableStateOf(currentYear + 1) }
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var selectedDay by remember { mutableStateOf(currentDay.coerceAtLeast(1)) }
    var selectYear by remember { mutableStateOf(true) }

    // Lunar fields
    var lunarYear by remember { mutableStateOf(currentLunar.year) }
    var lunarMonth by remember { mutableStateOf(currentLunar.month) }
    var lunarDay by remember { mutableStateOf(currentLunar.day) }
    var isLeapMonth by remember { mutableStateOf(false) }
    var isRepeatYearly by remember { mutableStateOf(false) }

    LaunchedEffect(showDatePicker) {
        if (showDatePicker) {
            if (calendarType == "lunar") {
                val lunar = lunarFromMillis(System.currentTimeMillis())
                selectedYear = lunar.year
                selectedMonth = lunar.month - 1
                selectedDay = lunar.day
            } else {
                selectedYear = currentYear
                selectedMonth = currentMonth
                selectedDay = currentDay.coerceAtLeast(1)
            }
            selectYear = false
        }
    }

    var resultDate by remember { mutableStateOf<Long?>(null) }
    val isSelectedLunarDateValid = calendarType != "lunar" ||
        lunarToSolarMillis(selectedYear, selectedMonth + 1, selectedDay) != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加事件", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("事件名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Calendar type tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { calendarType = "solar" },
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
                            if (resultDate == null) {
                                selectedYear = currentLunar.year
                                selectedMonth = currentLunar.month - 1
                                selectedDay = currentLunar.day
                                lunarYear = currentLunar.year
                                lunarMonth = currentLunar.month
                                lunarDay = currentLunar.day
                            }
                        },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = if (calendarType == "lunar") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (calendarType == "lunar") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("农历")
                        }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (calendarType == "lunar" && resultDate != null && lunarYear > 0) {
                            formatLunarDate(
                                year = lunarYear,
                                month = lunarMonth,
                                day = lunarDay,
                                includeYear = selectYear
                            )
                        } else {
                            resultDate?.let {
                                java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                            } ?: "选择日期"
                        }
                    )
                }

                if (calendarType == "lunar") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isRepeatYearly,
                            onCheckedChange = { isRepeatYearly = it }
                        )
                        Text("每年重复（农历生日）", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val date = if (calendarType == "lunar") {
                            resultDate ?: lunarToSolarMillis(lunarYear, lunarMonth, lunarDay)
                            ?: (System.currentTimeMillis() + 86400000)
                        } else {
                            resultDate ?: (System.currentTimeMillis() + 86400000)
                        }
                        val month = if (calendarType == "solar" && !selectYear) selectedMonth else -1
                        val day = if (calendarType == "solar" && !selectYear) selectedDay else -1
                        onConfirm(
                            name,
                            date,
                            month,
                            day,
                            calendarType,
                            lunarYear,
                            lunarMonth,
                            lunarDay,
                            isLeapMonth,
                            isRepeatYearly
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("确认")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )

    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text(if (calendarType == "lunar") "选择农历日期" else "选择日期", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (calendarType == "lunar") {
                        Text(
                            text = formatLunarDate(
                                year = selectedYear,
                                month = selectedMonth + 1,
                                day = selectedDay,
                                includeYear = selectYear
                            ),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = selectYear, onCheckedChange = { selectYear = it })
                        Text("选择年份", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectYear) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(if (calendarType == "lunar") "农历年" else "年", style = MaterialTheme.typography.bodySmall, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                WheelPicker(
                                    items = (1900..2099).toList(),
                                    selectedItem = selectedYear,
                                    onSelectedItemChange = { selectedYear = it },
                                    modifier = Modifier.height(150.dp)
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(if (calendarType == "lunar") "农历月" else "月", style = MaterialTheme.typography.bodySmall, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            WheelPicker(
                                items = (0..11).toList().map { it + 1 },
                                selectedItem = selectedMonth + 1,
                                onSelectedItemChange = { selectedMonth = it - 1 },
                                modifier = Modifier.height(150.dp),
                                itemLabel = {
                                    if (calendarType == "lunar") formatLunarMonthName(it) else it.toString()
                                }
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(if (calendarType == "lunar") "农历日" else "日", style = MaterialTheme.typography.bodySmall, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            WheelPicker(
                                items = if (calendarType == "lunar") (1..30).toList() else (1..31).toList(),
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
                            val lunarYearValue = if (selectYear) selectedYear else {
                                val thisYearDate = lunarToSolarMillis(currentYear, lunarMonthValue, lunarDayValue)
                                val today = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                if (thisYearDate != null && thisYearDate > today) currentYear else currentYear + 1
                            }
                            lunarYear = lunarYearValue
                            lunarMonth = lunarMonthValue
                            lunarDay = lunarDayValue
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
                        resultDate = finalDate
                        showDatePicker = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
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
