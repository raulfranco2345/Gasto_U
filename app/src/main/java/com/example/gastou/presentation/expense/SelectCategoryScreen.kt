package com.example.gastou.presentation.expense

import com.example.gastou.core.ui.components.*
import com.example.gastou.domain.model.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun SelectCategoryScreen(
    currentCategory: String?,
    onCategorySelected: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf(currentCategory) }

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
                    text = "Confirmar categoría →",
                    onClick = { selectedId?.let { onCategorySelected(it) } }
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

            Text("Seleccionar categoría", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
            Text("Elige a qué corresponde tu gasto universitario", fontSize = 12.sp, color = GastoUTextGray)

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar categoría (ej. menú, fotocopias...)", color = GastoUTextLightGray, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = GastoUTextLightGray, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GastoUTextFieldBorder,
                    focusedBorderColor = GastoUGreen,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categories List
            defaultCategories.filter { it.title.contains(searchQuery, ignoreCase = true) || it.subtitle.contains(searchQuery, ignoreCase = true) }.forEach { cat ->
                val isSelected = cat.id == selectedId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { selectedId = cat.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GastoUGreen) 
                             else androidx.compose.foundation.BorderStroke(1.dp, GastoUTextFieldBorder)
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
                                    .background(cat.color),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(cat.icon, contentDescription = null, tint = cat.iconTint)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(cat.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(shape = RoundedCornerShape(4.dp), color = GastoUBackgroundLight) {
                                            Text("Actual", fontSize = 10.sp, color = GastoUGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text(cat.subtitle, fontSize = 10.sp, color = GastoUTextGray)
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
            
            // Custom category button
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
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = GastoUTextGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear categoría personalizada", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GastoUTextGray)
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectCategoryScreenPreview() {
    GastoUTheme {
        SelectCategoryScreen(currentCategory = "Comida y Bebidas", onCategorySelected = {}, onNavigateBack = {})
    }
}