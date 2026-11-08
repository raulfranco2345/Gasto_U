package com.example.gastou.presentation.history

import com.example.gastou.core.ui.components.*
import com.example.gastou.domain.model.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.GastoULogo
import com.example.gastou.core.ui.components.formatYearMonth
import com.example.gastou.core.ui.theme.*
import java.time.YearMonth
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onMonthSelected: (YearMonth) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToExpenseDetail: (String) -> Unit
) {
    val expenses = uiState.expenses
    val selectedMonth = uiState.selectedMonth
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        GastoULogo(fontSize = 24, modifier = Modifier.offset(x = (-24).dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { HistoryBottomNavigation(onNavigateToHome, onNavigateToAddExpense, onNavigateToGoals) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("Historial", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)

            Spacer(modifier = Modifier.height(16.dp))

            // Month Selector Pill
            var monthMenuExpanded by remember { mutableStateOf(false) }
            val recentMonths = remember { (-3..3).map { YearMonth.now().plusMonths(it.toLong()) } }

            Box {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GastoUBackgroundLight,
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

            if (expenses.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay gastos registrados en este mes.", color = GastoUTextGray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(expenses.reversed()) { expense ->
                        val catOption = defaultCategories.find { it.id == expense.categoryId }
                            ?: defaultCategories.last() // Fallback to 'Otros'
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToExpenseDetail(expense.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GastoUTextFieldBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(catOption.color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(catOption.icon, contentDescription = null, tint = catOption.iconTint)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(catOption.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                                        Text(expense.date, fontSize = 12.sp, color = GastoUTextGray)
                                    }
                                }
                                
                                Text(
                                    "- $${String.format(Locale.US, "%,.0f", expense.amount)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444) // Red color for expense
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryBottomNavigation(onNavigateToHome: () -> Unit, onNavigateToAddExpense: () -> Unit, onNavigateToGoals: () -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = GastoUGreen,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToHome,
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
            label = { Text("Inicio", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
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
            selected = true,
            onClick = { },
            icon = { Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, contentDescription = "Historial") },
            label = { Text("Historial", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
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

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    GastoUTheme {
        HistoryScreen(
            uiState = HistoryUiState(
                expenses = listOf(
                    Expense(amount = 120.0, categoryId = "Comida y Bebidas", date = "12 abr 2025", paymentMethod = "Tarjeta prepago U", note = ""),
                    Expense(amount = 40.0, categoryId = "Transporte", date = "11 abr 2025", paymentMethod = "Tarjeta prepago U", note = ""),
                    Expense(amount = 85.0, categoryId = "Ocio y Salidas", date = "10 abr 2025", paymentMethod = "Tarjeta prepago U", note = ""),
                )
            ),
            onMonthSelected = {},
            onNavigateToHome = {},
            onNavigateToAddExpense = {},
            onNavigateToGoals = {},
            onNavigateToExpenseDetail = {}
        )
    }
}