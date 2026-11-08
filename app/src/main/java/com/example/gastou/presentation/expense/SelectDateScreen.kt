package com.example.gastou.presentation.expense

import com.example.gastou.core.ui.components.*
import com.example.gastou.domain.model.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.GastoUButton
import com.example.gastou.core.ui.components.GastoULogo
import com.example.gastou.core.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectDateScreen(
    currentDate: LocalDate,
    currentTime: LocalTime,
    onDateTimeSelected: (LocalDate, LocalTime) -> Unit,
    onConfirm: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(currentDate) }
    var displayedMonth by remember { mutableStateOf(YearMonth.from(currentDate)) }
    var selectedTime by remember { mutableStateOf(currentTime) }
    
    // Time picker state
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour = false
    )
    
    // UI Helpers
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "ES"))
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM", Locale("es", "ES"))
    
    fun formatDisplayDate(date: LocalDate): String {
        val formatted = date.format(dateFormatter)
        return when (date) {
            today -> "Hoy ($formatted)"
            yesterday -> "Ayer ($formatted)"
            else -> formatted
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        GastoULogo(fontSize = 24, modifier = Modifier.offset(x = (-24).dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.background(Color.White).padding(24.dp)) {
                GastoUButton(
                    text = "Confirmar fecha →",
                    onClick = { 
                        onDateTimeSelected(selectedDate, selectedTime)
                        onConfirm() 
                    }
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text("Seleccionar fecha", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
            Text("Indica cuándo realizaste este gasto universitario", fontSize = 12.sp, color = GastoUTextGray)

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickDateChip(formatDisplayDate(today), isSelected = selectedDate == today, onClick = { selectedDate = today; displayedMonth = YearMonth.from(today) })
                QuickDateChip(formatDisplayDate(yesterday), isSelected = selectedDate == yesterday, onClick = { selectedDate = yesterday; displayedMonth = YearMonth.from(yesterday) })
                
                val defaultThirdDate = today.minusDays(2)
                if (selectedDate != today && selectedDate != yesterday) {
                    QuickDateChip(formatDisplayDate(selectedDate), isSelected = true, onClick = {})
                } else {
                    QuickDateChip(formatDisplayDate(defaultThirdDate), isSelected = false, onClick = { selectedDate = defaultThirdDate; displayedMonth = YearMonth.from(defaultThirdDate) })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Calendar Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, GastoUTextFieldBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = GastoUTextDark)
                        }
                        Row {
                            val monthName = displayedMonth.format(monthFormatter).replaceFirstChar { it.titlecase(Locale.getDefault()) }
                            Text(monthName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GastoUTextDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(shape = RoundedCornerShape(4.dp), color = GastoUBackgroundLight) {
                                Text(displayedMonth.year.toString(), fontSize = 14.sp, color = GastoUGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = GastoUTextDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Days of week
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                        days.forEach { Text(it, fontSize = 12.sp, color = GastoUTextLightGray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days grid
                    val daysInMonth = displayedMonth.lengthOfMonth()
                    val firstDayOfWeek = displayedMonth.atDay(1).dayOfWeek.value // 1 (Mon) - 7 (Sun)
                    
                    val allCells = mutableListOf<String>()
                    // Add empty cells for padding
                    for (i in 1 until firstDayOfWeek) {
                        allCells.add("")
                    }
                    // Add days
                    for (i in 1..daysInMonth) {
                        allCells.add(i.toString())
                    }
                    // Pad end
                    while (allCells.size % 7 != 0) {
                        allCells.add("")
                    }
                    
                    val rows = allCells.chunked(7)
                    
                    rows.forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            row.forEach { dayStr ->
                                if (dayStr.isEmpty()) {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val day = dayStr.toInt()
                                    val cellDate = displayedMonth.atDay(day)
                                    val isSelected = selectedDate == cellDate
                                    
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(if (isSelected) GastoUGreen else Color.Transparent)
                                            .clickable { selectedDate = cellDate },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayStr,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else GastoUTextDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GastoUGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Días con gastos registrados", fontSize = 10.sp, color = GastoUTextLightGray)
                        }
                        Text("Ir a hoy", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUGreen, modifier = Modifier.clickable { 
                            selectedDate = LocalDate.now()
                            displayedMonth = YearMonth.from(LocalDate.now())
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Approximation Card
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale("en", "US"))
            val formattedTime = selectedTime.format(timeFormatter)
            val timePeriodName = when (selectedTime.hour) {
                in 5..11 -> "Mañana"
                in 12..18 -> "Tarde"
                else -> "Noche"
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, GastoUTextFieldBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Hora exacta", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                                Text("$formattedTime ($timePeriodName)", fontSize = 10.sp, color = GastoUTextGray)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp), 
                            color = GastoUBackgroundLight,
                            onClick = { showTimePicker = true }
                        ) {
                            Text("Cambiar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GastoUGreen, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TimeChip("Mañana", null, timePeriodName == "Mañana") { selectedTime = LocalTime.of(9, 0) }
                        TimeChip("Tarde", Icons.Outlined.WbSunny, timePeriodName == "Tarde") { selectedTime = LocalTime.of(14, 30) }
                        TimeChip("Noche", null, timePeriodName == "Noche") { selectedTime = LocalTime.of(20, 0) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
        }
        
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = { 
                        selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false 
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancelar")
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }
    }
}

@Composable
fun QuickDateChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) GastoUGreen else Color.White,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, GastoUTextFieldBorder) else null,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else GastoUTextGray
            )
        }
    }
}

@Composable
fun RowScope.TimeChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector?, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GastoUGreen) else androidx.compose.foundation.BorderStroke(1.dp, GastoUTextFieldBorder),
        modifier = Modifier.weight(1f).clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null && isSelected) {
                Icon(icon, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GastoUGreen else GastoUTextGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectDateScreenPreview() {
    GastoUTheme {
        SelectDateScreen(currentDate = LocalDate.now(), currentTime = LocalTime.now(), onDateTimeSelected = { _, _ -> }, onConfirm = {}, onNavigateBack = {})
    }
}