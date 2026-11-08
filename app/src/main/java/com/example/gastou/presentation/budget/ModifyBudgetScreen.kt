package com.example.gastou.presentation.budget

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.GastoUButton
import com.example.gastou.core.ui.theme.*
import com.example.gastou.core.ui.components.formatYearMonth
import java.time.YearMonth
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyBudgetScreen(
    currentBudget: Double?,
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onSaveBudget: (Double) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Se reinicia cuando llega el presupuesto guardado (Room) o cuando cambia el mes
    var newBudget by remember(currentBudget) { mutableStateOf(currentBudget ?: 0.0) }
    var textValue by remember(currentBudget) { mutableStateOf(if (newBudget > 0) newBudget.toInt().toString() else "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Modificar presupuesto", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { newBudget = currentBudget ?: 0.0 }) {
                        Icon(
                            imageVector = Icons.Outlined.Restore,
                            contentDescription = "Restaurar",
                            tint = GastoUGreen,
                            modifier = Modifier
                                .background(GastoUBackgroundLight, CircleShape)
                                .padding(6.dp)
                                .size(20.dp)
                        )
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

            // Period Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("PERIODO ACTIVO", fontSize = 10.sp, color = GastoUTextLightGray, fontWeight = FontWeight.Bold)
                            Text(formatYearMonth(selectedMonth), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                        }
                    }
                    
                    var monthMenuExpanded by remember { mutableStateOf(false) }
                    val recentMonths = remember { (-3..3).map { YearMonth.now().plusMonths(it.toLong()) } }
                    
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { monthMenuExpanded = true }
                                .padding(4.dp)
                        ) {
                            Text("Cambiar mes", fontSize = 12.sp, color = GastoUGreen, fontWeight = FontWeight.Medium)
                            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(16.dp))
                        }
                        
                        DropdownMenu(
                            expanded = monthMenuExpanded,
                            onDismissRequest = { monthMenuExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            recentMonths.forEach { month ->
                                DropdownMenuItem(
                                    text = { Text(formatYearMonth(month)) },
                                    onClick = {
                                        onMonthSelected(month)
                                        monthMenuExpanded = false
                                    },
                                    colors = if (month == selectedMonth) MenuDefaults.itemColors(textColor = GastoUGreen) else MenuDefaults.itemColors()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // New Limit Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NUEVO LÍMITE MENSUAL", fontSize = 10.sp, color = GastoUTextLightGray, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GastoUGreen, modifier = Modifier.padding(bottom = 6.dp, end = 4.dp))
                        
                        androidx.compose.foundation.text.BasicTextField(
                            value = textValue,
                            onValueChange = { newValue ->
                                val filtered = newValue.filter { it.isDigit() }
                                textValue = filtered
                                newBudget = filtered.toDoubleOrNull() ?: 0.0
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GastoUTextDark
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(IntrinsicSize.Min),
                            decorationBox = { innerTextField ->
                                if (textValue.isEmpty()) {
                                    Text("0", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = GastoUTextLightGray)
                                }
                                innerTextField()
                            }
                        )
                        
                        Text("MXN", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = GastoUTextLightGray, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (currentBudget != null && currentBudget > 0) {
                        Text(
                            text = buildAnnotatedString {
                                append("Presupuesto previo: $${String.format(Locale.US, "%,d", currentBudget.toInt())} MXN ")
                                val diff = newBudget - currentBudget
                                if (diff > 0) {
                                    withStyle(style = SpanStyle(color = GastoUGreen, fontWeight = FontWeight.Bold)) {
                                        append("(+$${String.format(Locale.US, "%,d", diff.toInt())})")
                                    }
                                } else if (diff < 0) {
                                    withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                                        append("(-$${String.format(Locale.US, "%,d", -diff.toInt())})")
                                    }
                                }
                            },
                            fontSize = 12.sp,
                            color = GastoUTextGray
                        )
                    } else {
                        Text("Establece tu primer presupuesto mensual", fontSize = 12.sp, color = GastoUTextGray)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionButton(text = "- $200", onClick = {
                            val result = if (newBudget >= 200) newBudget - 200 else 0.0
                            newBudget = result
                            textValue = result.toInt().toString()
                        })
                        QuickActionButton(text = "+ $500", onClick = {
                            newBudget += 500
                            textValue = newBudget.toInt().toString()
                        })
                        QuickActionButton(text = "+ $1,000", onClick = {
                            newBudget += 1000
                            textValue = newBudget.toInt().toString()
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alert Box
            if (newBudget > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GastoUBackgroundLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("¡Equilibrio universitario!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Al ajustar a ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("$${String.format(Locale.US, "%,d", newBudget.toInt())}") }
                                    append(", tu nuevo límite sugerido es de ")
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = GastoUGreen)) { 
                                        append("~$${String.format(Locale.US, "%.0f", newBudget / 30)} MXN/día") 
                                    }
                                    append(" para los 30 días del ciclo.")
                                },
                                fontSize = 11.sp,
                                color = GastoUTextDark,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Distribution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Distribución inteligente", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                    Text("Calculado a partir de tus gastos habituales", fontSize = 12.sp, color = GastoUTextLightGray)
                }
                Text("Reajustar %", fontSize = 12.sp, color = GastoUGreen, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories
            val totalBudget = if (newBudget > 0) newBudget else 0.0
            
            CategoryProgressRow("Comida y snacks", if (totalBudget > 0) "35%" else "0%", "$${String.format(Locale.US, "%,d", (totalBudget * 0.35).toInt())} MXN", CatFood, if (totalBudget > 0) 0.35f else 0f)
            CategoryProgressRow("Transporte", if (totalBudget > 0) "20%" else "0%", "$${String.format(Locale.US, "%,d", (totalBudget * 0.20).toInt())} MXN", CatTransport, if (totalBudget > 0) 0.2f else 0f)
            CategoryProgressRow("Estudios / Copias", if (totalBudget > 0) "20%" else "0%", "$${String.format(Locale.US, "%,d", (totalBudget * 0.20).toInt())} MXN", CatStudy, if (totalBudget > 0) 0.2f else 0f)
            CategoryProgressRow("Ocio y salidas", if (totalBudget > 0) "15%" else "0%", "$${String.format(Locale.US, "%,d", (totalBudget * 0.15).toInt())} MXN", CatLeisure, if (totalBudget > 0) 0.15f else 0f)
            CategoryProgressRow("Ahorro y otros", if (totalBudget > 0) "10%" else "0%", "$${String.format(Locale.US, "%,d", (totalBudget * 0.10).toInt())} MXN", CatOther, if (totalBudget > 0) 0.1f else 0f)

            Spacer(modifier = Modifier.height(16.dp))

            // Total Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = if (totalBudget > 0) GastoUGreen else GastoUTextLightGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (totalBudget > 0) "100% presupuestado" else "0% presupuestado", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (totalBudget > 0) GastoUGreen else GastoUTextLightGray)
                }
                Text("Total asignado: $${String.format(Locale.US, "%,d", newBudget.toInt())}", fontSize = 12.sp, color = GastoUTextLightGray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Limit Card
            if (newBudget > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GastoUTextFieldBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GastoUBackgroundLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.MonetizationOn, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Límite diario recomendado", fontSize = 11.sp, color = GastoUTextGray)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("~$${String.format(Locale.US, "%.2f", newBudget / 30)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                                    Text(" / día", fontSize = 12.sp, color = GastoUTextGray, modifier = Modifier.padding(bottom = 2.dp))
                                }
                            }
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GastoUBackgroundLight
                        ) {
                            Text(
                                "Sostenible",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GastoUGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            GastoUButton(
                text = if (currentBudget == null) "Fijar presupuesto →" else "Guardar nuevo presupuesto →", 
                onClick = { onSaveBudget(newBudget) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (currentBudget == null) "Cancelar" else "Cancelar y mantener actual",
                fontSize = 14.sp,
                color = GastoUTextGray,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateBack() }
                    .padding(8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CategoryProgressRow(
    title: String,
    percentageStr: String,
    amountStr: String,
    color: Color,
    fraction: Float
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                Spacer(modifier = Modifier.width(4.dp))
                Text("($percentageStr)", fontSize = 10.sp, color = GastoUTextLightGray)
            }
            Text(amountStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(GastoUTextFieldBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModifyBudgetScreenPreview() {
    GastoUTheme {
        ModifyBudgetScreen(
            currentBudget = null,
            selectedMonth = YearMonth.now(),
            onMonthSelected = {},
            onSaveBudget = {},
            onNavigateBack = {}
        )
    }
}
