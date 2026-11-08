package com.example.gastou.presentation.goals

import com.example.gastou.core.ui.components.*
import com.example.gastou.domain.model.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.GastoUButton
import com.example.gastou.core.ui.theme.*
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    uiState: GoalsUiState,
    onNavigateBack: () -> Unit,
    onAddFunds: (Double, String) -> Unit,
    onMarkCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    val goal = uiState.selectedGoal ?: return
    
    var showAddFundsDialog by remember { mutableStateOf(false) }
    var addFundsAmount by remember { mutableStateOf("") }
    
    var selectedPaymentMethod by remember { mutableStateOf("Tarjeta prepago U") }
    var expandedPaymentMethod by remember { mutableStateOf(false) }
    val paymentMethods = listOf(
        "Tarjeta prepago U",
        "Efectivo",
        "Tarjeta de débito / crédito",
        "Transferencia / SPEI / QR",
        "Billetera digital"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Detalle de la meta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark, modifier = Modifier.offset(x = (-24).dp))
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
                            .background(GastoUBackgroundLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Savings, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(goal.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)

                    if (goal.isCompleted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(16.dp), color = GastoUGreen.copy(alpha = 0.1f)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Meta completada", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = GastoULightGreen,
                        trackColor = GastoUBackgroundDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GastoUGreen, modifier = Modifier.padding(bottom = 4.dp))
                        Text(String.format(Locale.US, "%,.0f", goal.currentAmount), fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = GastoUGreen)
                        Text(" / $${String.format(Locale.US, "%,.0f", goal.targetAmount)} MXN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUTextLightGray, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
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
                        Icon(Icons.Outlined.Event, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("FECHA LÍMITE", fontSize = 10.sp, color = GastoUTextLightGray, fontWeight = FontWeight.Bold)
                            Text(goal.targetDate ?: "Sin fecha límite", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GastoUTextDark)
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
                            Text(goal.category, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GastoUTextDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!goal.isCompleted) {
                GastoUButton(
                    text = "Abonar a la meta",
                    onClick = { showAddFundsDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onMarkCompleted,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GastoUGreen)
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Marcar como completada", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar esta meta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showAddFundsDialog) {
            AlertDialog(
                onDismissRequest = { showAddFundsDialog = false },
                title = { Text("Abonar a la meta", fontWeight = FontWeight.Bold, color = GastoUTextDark) },
                text = {
                    Column {
                        Text("Ingresa el monto que deseas abonar. Esto se registrará como un gasto en tu historial.", fontSize = 12.sp, color = GastoUTextGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$ ", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
                            BasicTextField(
                                value = addFundsAmount,
                                onValueChange = { newValue ->
                                    val filtered = newValue.filter { it.isDigit() }
                                    addFundsAmount = filtered
                                },
                                textStyle = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GastoUTextDark
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.defaultMinSize(minWidth = 40.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Método de pago", fontSize = 12.sp, color = GastoUTextGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GastoUBackgroundLight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { expandedPaymentMethod = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedPaymentMethod, fontSize = 14.sp, color = GastoUTextDark, fontWeight = FontWeight.Medium)
                                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = GastoUGreen)
                                }
                            }
                            
                            DropdownMenu(
                                expanded = expandedPaymentMethod,
                                onDismissRequest = { expandedPaymentMethod = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                paymentMethods.forEach { method ->
                                    DropdownMenuItem(
                                        text = { Text(method, fontSize = 14.sp) },
                                        onClick = {
                                            selectedPaymentMethod = method
                                            expandedPaymentMethod = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            val amount = addFundsAmount.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                onAddFunds(amount, selectedPaymentMethod)
                            }
                            showAddFundsDialog = false
                            addFundsAmount = ""
                            selectedPaymentMethod = "Tarjeta prepago U"
                        }
                    ) {
                        Text("Abonar", color = GastoUGreen, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddFundsDialog = false }) {
                        Text("Cancelar", color = GastoUTextGray)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GoalDetailScreenPreview() {
    GastoUTheme {
        GoalDetailScreen(
            uiState = GoalsUiState(
                savingGoals = listOf(
                    SavingGoal(
                        id = "mock",
                        name = "Laptop para la uni",
                        targetAmount = 2500.0,
                        currentAmount = 1180.0,
                        category = "Tecnología",
                        targetDate = "30/06/2025"
                    )
                ),
                selectedGoalId = "mock"
            ),
            onNavigateBack = {},
            onAddFunds = { _, _ -> },
            onMarkCompleted = {},
            onDelete = {}
        )
    }
}