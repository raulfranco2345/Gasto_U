package com.example.gastou.presentation.expense

import com.example.gastou.core.ui.components.*
import com.example.gastou.domain.model.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import com.example.gastou.integration.camera.rememberReceiptCamera
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.AddCircle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.GastoUButton
import com.example.gastou.core.ui.components.GastoULogo
import com.example.gastou.core.ui.theme.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    uiState: AddExpenseUiState,
    onAmountChange: (String) -> Unit,
    onNavigateToCategory: () -> Unit,
    onNavigateToDate: () -> Unit,
    onNavigateToPaymentMethod: () -> Unit,
    onNoteChange: (String) -> Unit,
    onReceiptPhotoTaken: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToGoals: () -> Unit
) {
    val amount = uiState.amount
    val category = uiState.category
    val date = uiState.displayDate
    val paymentMethod = uiState.paymentMethod
    val note = uiState.note
    val receiptUri = uiState.receiptUri
    val context = LocalContext.current
    // La cámara y su permiso viven en el módulo integration/camera
    val takeReceiptPhoto = rememberReceiptCamera(onPhotoTaken = onReceiptPhotoTaken)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        GastoULogo(fontSize = 24, modifier = Modifier.offset(x = (-24).dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GastoUBackgroundLight)
            )
        },
        bottomBar = { AddExpenseBottomNavigation(onNavigateHome, onNavigateToHistory, onNavigateToGoals) },
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

            Text("Registrar gasto", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)

            Spacer(modifier = Modifier.height(24.dp))

            // Amount Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$ ", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if (amount.isEmpty()) GastoUTextLightGray else GastoUTextDark)
                        BasicTextField(
                            value = amount,
                            onValueChange = onAmountChange,
                            textStyle = TextStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = GastoUTextDark
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(IntrinsicSize.Min),
                            decorationBox = { innerTextField ->
                                if (amount.isEmpty()) {
                                    Text("0.00", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = GastoUTextLightGray)
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Fields
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    // Category
                    FormRowItem(
                        icon = { Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = GastoUTextDark) },
                        label = category ?: "Categoría",
                        isPlaceholder = category == null,
                        onClick = onNavigateToCategory,
                        showArrow = true
                    )
                    
                    HorizontalDivider(color = GastoUBackgroundLight, modifier = Modifier.padding(horizontal = 16.dp))

                    // Date
                    FormRowItem(
                        icon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = GastoUTextDark) },
                        label = date,
                        isPlaceholder = false,
                        onClick = onNavigateToDate,
                        showArrow = true
                    )

                    HorizontalDivider(color = GastoUBackgroundLight, modifier = Modifier.padding(horizontal = 16.dp))

                    // Payment Method
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToPaymentMethod)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = GastoUTextDark)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Método de pago", fontSize = 16.sp, color = GastoUTextDark)
                                Text(paymentMethod, fontSize = 12.sp, color = GastoUGreen, fontWeight = FontWeight.Medium)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = GastoUTextLightGray)
                    }

                    HorizontalDivider(color = GastoUBackgroundLight, modifier = Modifier.padding(horizontal = 16.dp))

                    // Note
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Description, contentDescription = null, tint = GastoUTextDark)
                        Spacer(modifier = Modifier.width(16.dp))
                        BasicTextField(
                            value = note,
                            onValueChange = onNoteChange,
                            textStyle = TextStyle(fontSize = 16.sp, color = GastoUTextDark),
                            decorationBox = { innerTextField ->
                                if (note.isEmpty()) {
                                    Text("Nota (opcional)", fontSize = 16.sp, color = GastoUTextLightGray)
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Camera / Receipt Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        takeReceiptPhoto()
                    }
            ) {
                // Dashed border effect using a standard border for now, 
                // in Compose native dashed borders require custom drawing, we'll use a normal border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                        .background(Color.Transparent, RoundedCornerShape(14.dp))
                        .border(1.dp, GastoULightGreen, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(GastoUBackgroundLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(if (receiptUri == null) Icons.Outlined.CameraAlt else Icons.Outlined.Check, contentDescription = null, tint = GastoUGreen)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(if (receiptUri == null) "Escanear / Tomar foto del recibo" else "Foto adjuntada", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                                Text(if (receiptUri == null) "(Opcional: vincula tu ticket o comprobante)" else "Recibo listo para guardar", fontSize = 10.sp, color = GastoUTextGray)
                            }
                        }
                        if (receiptUri == null) {
                            Icon(Icons.Outlined.Add, contentDescription = null, tint = GastoUGreen)
                        } else {
                            Icon(Icons.Outlined.Check, contentDescription = null, tint = GastoUGreen)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            ErrorBanner(uiState.errorMessage)

            Spacer(modifier = Modifier.height(12.dp))

            GastoUButton(
                text = if (uiState.isSaving) "Guardando..." else "Guardar gasto",
                onClick = onSave,
                enabled = !uiState.isSaving && amount.isNotEmpty() && category != null
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FormRowItem(
    icon: @Composable () -> Unit,
    label: String,
    isPlaceholder: Boolean,
    onClick: () -> Unit,
    showArrow: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                color = if (isPlaceholder) GastoUTextLightGray else GastoUTextDark
            )
        }
        if (showArrow) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = GastoUTextLightGray)
        }
    }
}

@Composable
fun AddExpenseBottomNavigation(onNavigateHome: () -> Unit, onNavigateToHistory: () -> Unit, onNavigateToGoals: () -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = GastoUGreen,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onNavigateHome,
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
            label = { Text("Inicio", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = GastoUTextLightGray,
                unselectedTextColor = GastoUTextLightGray
            )
        )
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Registrar") },
            label = { Text("Registrar", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
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

@Preview(showBackground = true)
@Composable
fun AddExpenseScreenPreview() {
    GastoUTheme {
        AddExpenseScreen(
            uiState = AddExpenseUiState(),
            onAmountChange = {},
            onNavigateToCategory = {},
            onNavigateToDate = {},
            onNavigateToPaymentMethod = {},
            onNoteChange = {},
            onReceiptPhotoTaken = {},
            onSave = {}, onNavigateHome = {},
            onNavigateToHistory = {}, onNavigateToGoals = {}
        )
    }
}