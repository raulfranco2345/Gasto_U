package com.example.gastou.presentation.history

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
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.GastoUButton
import com.example.gastou.core.ui.theme.*
import java.util.Locale

import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expense: Expense,
    uiState: HistoryUiState,
    onNavigateBack: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val monthlyBudget = uiState.monthlyBudget
    val categoryExpenses = uiState.selectedExpenseCategoryTotal
    val catOption = defaultCategories.find { it.id == expense.categoryId } ?: defaultCategories.last()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Detalle de gasto", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark, modifier = Modifier.offset(x = (-24).dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu options */ }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreHoriz,
                            contentDescription = "Opciones",
                            tint = GastoUTextDark,
                            modifier = Modifier
                                .background(Color.White, CircleShape)
                                .padding(4.dp)
                                .size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GastoUBackgroundLight)
            )
        },
        containerColor = GastoUBackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Main Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(GastoUGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(catOption.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(catOption.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                    if (expense.note.isNotBlank()) {
                        Text(expense.note.take(30) + if (expense.note.length > 30) "..." else "", fontSize = 12.sp, color = GastoUTextGray)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("- $", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), modifier = Modifier.padding(bottom = 4.dp))
                        Text(String.format(Locale.US, "%,.0f", expense.amount), fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
                        Text(".00 MXN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), modifier = Modifier.padding(bottom = 8.dp, start = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(shape = RoundedCornerShape(16.dp), color = GastoUBackgroundLight) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Registrado con éxito", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = GastoUTextLightGray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(expense.paymentMethod, fontSize = 12.sp, color = GastoUTextGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Date Time
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("FECHA Y HORA", fontSize = 10.sp, color = GastoUTextLightGray, fontWeight = FontWeight.Bold)
                            Text("${expense.date} • 2:30 PM", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GastoUTextDark) // Added mock time to match UI
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = GastoUBackgroundLight)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Category
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Outlined.LocalOffer, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CATEGORÍA", fontSize = 10.sp, color = GastoUTextLightGray, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(catOption.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GastoUTextDark)
                                Surface(shape = RoundedCornerShape(8.dp), color = GastoUBackgroundLight) {
                                    Text("Campus", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GastoUGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    if (monthlyBudget != null && monthlyBudget > 0) {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = GastoUBackgroundLight)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Impact
                        val impactPercentage = (categoryExpenses / monthlyBudget) * 100
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Impacto mensual", fontSize = 12.sp, color = GastoUTextGray)
                                    Text(String.format(Locale.US, "%.1f%%", impactPercentage), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(GastoUBackgroundLight)) {
                                    Box(modifier = Modifier.fillMaxWidth((impactPercentage / 100).toFloat().coerceIn(0f, 1f)).height(6.dp).clip(CircleShape).background(GastoUGreen))
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = buildAnnotatedString {
                                        append("Llevas consumido $${String.format(Locale.US, "%,.0f", categoryExpenses)} de tu presupuesto mensual de $${String.format(Locale.US, "%,.0f", monthlyBudget)}")
                                    },
                                    fontSize = 10.sp,
                                    color = GastoUTextLightGray,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    if (expense.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = GastoUBackgroundLight)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Note
                        Text("NOTA DEL GASTO", fontSize = 10.sp, color = GastoUTextLightGray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GastoUBackgroundLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "\"${expense.note}\"",
                                fontSize = 12.sp,
                                color = GastoUTextGray,
                                modifier = Modifier.padding(16.dp),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    if (expense.receiptUri != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Mock Ticket
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GastoUBackgroundLight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Abrir la imagen en una aplicación externa de galería
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(expense.receiptUri), "image/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Manejar el caso donde no hay app para abrir imágenes (raro, pero posible)
                                        e.printStackTrace()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(GastoUBackgroundLight), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Outlined.Image, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Recibo_adjunto.jpg", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                                        Text("Adjuntado con la cámara", fontSize = 10.sp, color = GastoUTextGray)
                                    }
                                }
                                Text("Ver ticket", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar este gasto", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseDetailScreenPreview() {
    GastoUTheme {
        ExpenseDetailScreen(
            expense = Expense(
                amount = 120.0,
                categoryId = "Comida y Bebidas",
                date = "12 abr 2025",
                paymentMethod = "Tarjeta prepago U",
                note = "Menú estudiantil (arroz, guisado y frijoles) + agua fresca con los compañeros del proyecto final de cálculo.",
                receiptUri = "mock_uri"
            ),
            uiState = HistoryUiState(monthlyBudget = 1200.0),
            onNavigateBack = {},
            onDelete = {}
        )
    }
}