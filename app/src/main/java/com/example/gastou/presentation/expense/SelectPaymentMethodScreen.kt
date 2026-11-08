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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.GastoUButton
import com.example.gastou.core.ui.components.GastoULogo
import com.example.gastou.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectPaymentMethodScreen(
    currentMethod: String,
    onMethodSelected: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedId by remember { mutableStateOf(currentMethod) }

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
                    text = "Confirmar método de pago →",
                    onClick = { onMethodSelected(selectedId) }
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

            Text("Seleccionar método de pago", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
            Text("¿Cómo pagaste este gasto universitario?", fontSize = 12.sp, color = GastoUTextGray)

            Spacer(modifier = Modifier.height(24.dp))

            // Methods List
            paymentMethods.forEach { method ->
                val isSelected = method.id == selectedId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { selectedId = method.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GastoUGreen)
                             else androidx.compose.foundation.BorderStroke(1.dp, GastoUBackgroundLight)
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(method.iconTint.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(method.icon, contentDescription = null, tint = method.iconTint)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(method.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = GastoUBackgroundLight) {
                                            Text("Actual", fontSize = 10.sp, color = GastoUGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text(method.subtitle, fontSize = 10.sp, color = GastoUTextGray)
                            }
                        }

                        if (isSelected) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GastoUGreen)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = GastoUTextLightGray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom method button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, GastoUTextFieldBorder),
                modifier = Modifier.fillMaxWidth().clickable { }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar otro método de pago", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recuerda que puedes adjuntar foto del ticket para conciliar tu saldo automáticamente.", fontSize = 10.sp, color = GastoUTextGray, lineHeight = 14.sp)
            }

            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectPaymentMethodScreenPreview() {
    GastoUTheme {
        SelectPaymentMethodScreen(currentMethod = "Tarjeta prepago U", onMethodSelected = {}, onNavigateBack = {})
    }
}