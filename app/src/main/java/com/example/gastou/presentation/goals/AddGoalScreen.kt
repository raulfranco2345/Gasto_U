package com.example.gastou.presentation.goals

import com.example.gastou.core.ui.components.*
import com.example.gastou.domain.model.*

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.GastoUButton
import com.example.gastou.core.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    onSaveGoal: (SavingGoal) -> Unit,
    onNavigateBack: () -> Unit
) {
    var targetAmount by remember { mutableStateOf(0.0) }
    var targetAmountStr by remember { mutableStateOf("") }
    var goalName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tecnología") }
    var targetDate by remember { mutableStateOf<LocalDate?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val categories = listOf("Tecnología", "Estudios", "Viajes", "Fondo SOS", "Ocio")

    var suggestedAmount by remember { mutableStateOf(0.0) }
    var suggestedPeriod by remember { mutableStateOf("semana") }
    var suggestedLabel by remember { mutableStateOf("semanal") }

    LaunchedEffect(targetAmount, targetDate) {
        if (targetAmount > 0 && targetDate != null) {
            val today = LocalDate.now()
            val days = ChronoUnit.DAYS.between(today, targetDate)
            if (days > 0) {
                when {
                    days >= 30 -> {
                        suggestedAmount = targetAmount / (days / 30.0)
                        suggestedPeriod = "mes"
                        suggestedLabel = "mensual"
                    }
                    days >= 7 -> {
                        suggestedAmount = targetAmount / (days / 7.0)
                        suggestedPeriod = "semana"
                        suggestedLabel = "semanal"
                    }
                    else -> {
                        suggestedAmount = targetAmount / days.toDouble()
                        suggestedPeriod = "día"
                        suggestedLabel = "diario"
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Fijar meta de ahorro", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark, modifier = Modifier.offset(x = (-24).dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    Surface(shape = RoundedCornerShape(16.dp), color = GastoUBackgroundLight, modifier = Modifier.padding(end = 16.dp)) {
                        Text("1/2", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
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
            Spacer(modifier = Modifier.height(16.dp))

            // Info Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = GastoUBackgroundLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.White), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Eco, contentDescription = null, tint = GastoUGreen)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("¡Paso a paso, universitario!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Define tu próximo objetivo y te ayudaremos a no quedarte en ceros.", fontSize = 12.sp, color = GastoUTextDark, lineHeight = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("¿CUÁNTO NECESITAS REUNIR?", fontSize = 10.sp, color = GastoUTextLightGray, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GastoUGreen, modifier = Modifier.padding(bottom = 6.dp, end = 4.dp))

                        BasicTextField(
                            value = targetAmountStr,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }
                                targetAmountStr = filtered
                                targetAmount = filtered.toDoubleOrNull() ?: 0.0
                            },
                            textStyle = TextStyle(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GastoUTextDark
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(IntrinsicSize.Min).defaultMinSize(minWidth = 30.dp),
                            decorationBox = { innerTextField ->
                                if (targetAmountStr.isEmpty()) {
                                    Text("0", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = GastoUTextLightGray)
                                }
                                innerTextField()
                            }
                        )

                        Text("MXN", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = GastoUTextLightGray, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionButton(text = "+ $500", onClick = { targetAmount += 500; targetAmountStr = targetAmount.toInt().toString() })
                        QuickActionButton(text = "+ $1,000", onClick = { targetAmount += 1000; targetAmountStr = targetAmount.toInt().toString() })
                        QuickActionButton(text = "+ $2,500", onClick = { targetAmount += 2500; targetAmountStr = targetAmount.toInt().toString() })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name and Category
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("NOMBRE DE LA META", fontSize = 10.sp, color = GastoUTextLightGray, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.School, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(selectedCategory, fontSize = 10.sp, color = GastoUGreen, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BasicTextField(
                value = goalName,
                onValueChange = { goalName = it },
                textStyle = TextStyle(fontSize = 14.sp, color = GastoUTextDark, fontWeight = FontWeight.Medium),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GastoUBackgroundLight.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        if (goalName.isEmpty()) {
                            Text("Laptop para el semestre", fontSize = 14.sp, color = GastoUTextLightGray)
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Categorías recomendadas:", fontSize = 10.sp, color = GastoUTextGray)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) GastoUGreen else Color.White,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, GastoUTextFieldBorder) else null,
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.White else GastoUTextGray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Date Selection
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("¿CUÁNDO LO NECESITAS?", fontSize = 10.sp, color = GastoUTextLightGray, fontWeight = FontWeight.Bold)
                if (targetDate != null) {
                    val days = ChronoUnit.DAYS.between(LocalDate.now(), targetDate)
                    if (days > 0) {
                        val timeLeft = when {
                            days >= 30 -> "${days / 30} meses"
                            days >= 7 -> "${days / 7} semanas"
                            else -> "$days días"
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(timeLeft, fontSize = 10.sp, color = GastoUGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, GastoUTextFieldBorder),
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = GastoUTextGray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = targetDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "Seleccionar fecha",
                            fontSize = 14.sp,
                            color = if (targetDate != null) GastoUTextDark else GastoUTextLightGray
                        )
                    }
                    Icon(Icons.Outlined.Event, contentDescription = null, tint = GastoUTextDark, modifier = Modifier.size(18.dp))
                }
            }

            if (targetAmount > 0 && targetDate != null && ChronoUnit.DAYS.between(LocalDate.now(), targetDate) > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GastoUBackgroundLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(GastoUGreen), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Aporte sugerido $suggestedLabel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                                Text("Para alcanzar tu meta a tiempo.", fontSize = 10.sp, color = GastoUTextGray)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$${String.format(Locale.US, "%,.0f", suggestedAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                            Text("/ $suggestedPeriod", fontSize = 10.sp, color = GastoUTextGray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            GastoUButton(
                text = "Fijar meta de ahorro →",
                onClick = {
                    if (goalName.isNotBlank() && targetAmount > 0) {
                        onSaveGoal(
                            SavingGoal(
                                name = goalName,
                                targetAmount = targetAmount,
                                category = selectedCategory,
                                targetDate = targetDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            )
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Siempre podrás pausar o ajustar los montos desde tu perfil.",
                fontSize = 10.sp,
                color = GastoUTextGray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.ofEpochMilli(millis)
                            targetDate = instant.atZone(ZoneId.of("UTC")).toLocalDate()
                        }
                        showDatePicker = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AddGoalScreenPreview() {
    GastoUTheme {
        AddGoalScreen(onSaveGoal = {}, onNavigateBack = {})
    }
}