package com.example.gastou.presentation.home

import com.example.gastou.core.ui.components.*
import com.example.gastou.domain.model.*

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import com.example.gastou.core.ui.components.GastoULogo
import com.example.gastou.core.ui.components.formatYearMonth
import com.example.gastou.core.ui.theme.*
import java.time.YearMonth
import java.util.Locale

import androidx.compose.ui.platform.LocalContext
import android.content.Intent

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onMonthSelected: (YearMonth) -> Unit,
    onNavigateToModifyBudget: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onRetrySync: () -> Unit,
    onExportPdf: () -> Unit,
    onPdfHandled: () -> Unit,
    onMessageShown: () -> Unit
) {
    val totalSpent = uiState.totalSpent
    val expenses = uiState.expenses
    val selectedMonth = uiState.selectedMonth
    val monthlyBudget = uiState.monthlyBudget
    val context = LocalContext.current

    // Cuando el ViewModel termina el PDF se abre el diálogo para compartirlo
    LaunchedEffect(uiState.pdfResult) {
        val result = uiState.pdfResult ?: return@LaunchedEffect
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, result.shareUri)
            putExtra(Intent.EXTRA_SUBJECT, "Estado de Cuenta - ${formatYearMonth(selectedMonth)}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir estado de cuenta PDF"))
        onPdfHandled()
    }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        onMessageShown()
    }

    Scaffold(
        bottomBar = { GastoUBottomNavigation(onNavigateToAddExpense, onNavigateToHistory, onNavigateToGoals) },
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
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GastoULogo(fontSize = 24)
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GastoUBackgroundLight)
                        .clickable(onClick = onNavigateToProfile),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Abrir perfil",
                        tint = GastoUGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.userName.isNotBlank()) {
                Text(
                    text = "Hola, ${uiState.userName.substringBefore(" ")}",
                    fontSize = 14.sp,
                    color = GastoUTextGray
                )
            }

            Text(
                text = "Resumen del mes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GastoUTextDark
            )

            // Estado de sincronización con la API: carga, error con reintento o cambios pendientes
            when {
                uiState.isSyncing -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoBanner("Sincronizando con el servidor...", showProgress = true)
                }
                uiState.syncError != null -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    ErrorBanner(uiState.syncError, onRetry = onRetrySync)
                }
                uiState.pendingChanges > 0 -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoBanner("${uiState.pendingChanges} cambio(s) pendiente(s) de enviar al servidor")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Month Selector Pill
            var monthMenuExpanded by remember { mutableStateOf(false) }
            val recentMonths = remember { (-3..3).map { YearMonth.now().plusMonths(it.toLong()) } }

            Box {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GastoUTextFieldBorder),
                    onClick = { monthMenuExpanded = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(formatYearMonth(selectedMonth), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GastoUTextDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = GastoUTextGray, modifier = Modifier.size(16.dp))
                    }
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

            Spacer(modifier = Modifier.height(24.dp))

            // Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    if (totalSpent == 0.0) {
                        // Empty State: Donut Chart
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            EmptyDonutChart()
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$0", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                                Text(text = "gastado", fontSize = 12.sp, color = GastoUTextGray)
                            }
                        }

                        // Empty State: Legend
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.Info, contentDescription = null, tint = GastoUTextLightGray, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Sin gastos", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                            Text("Tus registros del mes\naparecerán aquí.", fontSize = 12.sp, color = GastoUTextGray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    } else {
                        // Donut Chart
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            DonutChart(expenses)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$${String.format(Locale.US, "%,.0f", totalSpent)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                                Text(text = "gastado", fontSize = 12.sp, color = GastoUTextGray)
                            }
                        }

                        // Legend
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            val categorySums = expenses.groupBy { it.categoryId }.mapValues { (_, list) -> list.sumOf { it.amount } }
                            val catFoodSum = categorySums["Comida y Bebidas"] ?: 0.0
                            val catTransportSum = categorySums["Transporte"] ?: 0.0
                            val catStudySum = categorySums["Estudios y Materiales"] ?: 0.0
                            val catLeisureSum = categorySums["Ocio y Salidas"] ?: 0.0
                            val catHealthSum = categorySums["Salud y Bienestar"] ?: 0.0
                            val catSavingsSum = categorySums["Ahorro e Inversión"] ?: 0.0
                            val catOtherSum = categorySums["Otros / Imprevistos"] ?: 0.0
                            
                            if (catFoodSum > 0) LegendItem(color = CatFood, title = "Comida", percentage = "${((catFoodSum/totalSpent)*100).toInt()}%")
                            if (catTransportSum > 0) { Spacer(modifier = Modifier.height(8.dp)); LegendItem(color = CatTransport, title = "Transporte", percentage = "${((catTransportSum/totalSpent)*100).toInt()}%") }
                            if (catStudySum > 0) { Spacer(modifier = Modifier.height(8.dp)); LegendItem(color = CatStudy, title = "Estudios", percentage = "${((catStudySum/totalSpent)*100).toInt()}%") }
                            if (catLeisureSum > 0) { Spacer(modifier = Modifier.height(8.dp)); LegendItem(color = CatLeisure, title = "Ocio", percentage = "${((catLeisureSum/totalSpent)*100).toInt()}%") }
                            if (catHealthSum > 0) { Spacer(modifier = Modifier.height(8.dp)); LegendItem(color = CatHealth, title = "Salud", percentage = "${((catHealthSum/totalSpent)*100).toInt()}%") }
                            if (catSavingsSum > 0) { Spacer(modifier = Modifier.height(8.dp)); LegendItem(color = CatSavings, title = "Ahorro", percentage = "${((catSavingsSum/totalSpent)*100).toInt()}%") }
                            if (catOtherSum > 0) { Spacer(modifier = Modifier.height(8.dp)); LegendItem(color = CatOther, title = "Otros", percentage = "${((catOtherSum/totalSpent)*100).toInt()}%") }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Budget Card
            val isBudgetSet = monthlyBudget != null
            val budgetAmount = monthlyBudget ?: 0.0
            val remaining = budgetAmount - totalSpent
            val progress = if (budgetAmount > 0) (totalSpent / budgetAmount).toFloat().coerceIn(0f, 1f) else 0f
            val remainingProgress = 1f - progress

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GastoUBackgroundLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null, tint = GastoUGreen)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (isBudgetSet) "Presupuesto restante" else "Presupuesto mensual", 
                                    fontSize = 12.sp, color = GastoUTextGray
                                )
                                Text(
                                    text = if (isBudgetSet) "$${String.format(Locale.US, "%,d", remaining.toInt())}" else "No fijado", 
                                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark
                                )
                            }
                        }
                        
                        // Edit / Set button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GastoUBackgroundLight,
                            onClick = onNavigateToModifyBudget
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (isBudgetSet) Icons.Outlined.Edit else Icons.Outlined.Add, 
                                    contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBudgetSet) "Editar" else "Fijar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
                            }
                        }
                    }

                    if (isBudgetSet) {
                        Spacer(modifier = Modifier.height(24.dp))

                        LinearProgressIndicator(
                            progress = { remainingProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = GastoULightGreen,
                            trackColor = GastoUBackgroundDark
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = buildAnnotatedString {
                                append("Te queda el ")
                                withStyle(style = SpanStyle(color = GastoUGreen, fontWeight = FontWeight.Bold)) {
                                    append("${(remainingProgress * 100).toInt()}%")
                                }
                                append(" de tu presupuesto")
                            },
                            fontSize = 12.sp,
                            color = GastoUTextGray
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fija un monto mensual para empezar a llevar el control de tus finanzas.", 
                            fontSize = 12.sp, color = GastoUTextGray, lineHeight = 16.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // PDF Export Button
            OutlinedButton(
                onClick = onExportPdf,
                enabled = !uiState.isGeneratingPdf,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GastoUGreen),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GastoUGreen)
            ) {
                Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (uiState.isGeneratingPdf) "Generando PDF..." else "Exportar reporte en PDF",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EmptyDonutChart() {
    val strokeWidth = 16.dp
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawArc(
            color = GastoUTextFieldBorder,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
        )
    }
}

@Composable
fun DonutChart(expenses: List<Expense>) {
    val totalSpent = expenses.sumOf { it.amount }
    if (totalSpent == 0.0) {
        EmptyDonutChart()
        return
    }

    val strokeWidth = 16.dp
    Canvas(modifier = Modifier.fillMaxSize()) {
        val categorySums = expenses.groupBy { it.categoryId }.mapValues { (_, list) -> list.sumOf { it.amount } }
        
        val sweeps = listOf(
            Pair(CatFood, categorySums["Comida y Bebidas"] ?: 0.0),
            Pair(CatTransport, categorySums["Transporte"] ?: 0.0),
            Pair(CatStudy, categorySums["Estudios y Materiales"] ?: 0.0),
            Pair(CatLeisure, categorySums["Ocio y Salidas"] ?: 0.0),
            Pair(CatHealth, categorySums["Salud y Bienestar"] ?: 0.0),
            Pair(CatSavings, categorySums["Ahorro e Inversión"] ?: 0.0),
            Pair(CatOther, categorySums["Otros / Imprevistos"] ?: 0.0)
        )
        
        var currentAngle = -90f
        
        sweeps.forEach { (color, sum) ->
            if (sum > 0) {
                val sweepAngle = 360f * (sum / totalSpent).toFloat()
                drawArc(
                    color = color,
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                )
                currentAngle += sweepAngle
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, title: String, percentage: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 12.sp, color = GastoUTextDark, modifier = Modifier.width(64.dp))
        Text(text = percentage, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
    }
}

@Composable
fun GastoUBottomNavigation(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToGoals: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = GastoUGreen,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
            label = { Text("Inicio", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GastoUGreen,
                selectedTextColor = GastoUGreen,
                indicatorColor = GastoUBackgroundLight,
                unselectedIconColor = GastoUTextLightGray,
                unselectedTextColor = GastoUTextLightGray
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToAddExpense,
            icon = { Icon(Icons.Outlined.AddCircleOutline, contentDescription = "Registrar") },
            label = { Text("Registrar", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = GastoUTextLightGray,
                unselectedTextColor = GastoUTextLightGray
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToHistory,
            icon = { Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, contentDescription = "Historial") },
            label = { Text("Historial", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = GastoUTextLightGray,
                unselectedTextColor = GastoUTextLightGray
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToGoals,
            icon = { Icon(Icons.Outlined.TrackChanges, contentDescription = "Metas") },
            label = { Text("Metas", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = GastoUTextLightGray,
                unselectedTextColor = GastoUTextLightGray
            )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GastoUTheme {
        HomeScreen(
            uiState = HomeUiState(),
            onMonthSelected = {},
            onNavigateToModifyBudget = {},
            onNavigateToAddExpense = {},
            onNavigateToHistory = {},
            onNavigateToGoals = {},
            onNavigateToProfile = {},
            onRetrySync = {},
            onExportPdf = {},
            onPdfHandled = {},
            onMessageShown = {}
        )
    }
}
