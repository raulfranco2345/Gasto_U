package com.example.gastou.presentation.goals

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
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.GastoUButton
import com.example.gastou.core.ui.components.GastoULogo
import com.example.gastou.core.ui.theme.*
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    uiState: GoalsUiState,
    onNavigateHome: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAddGoal: () -> Unit,
    onNavigateToGoalDetail: (String) -> Unit
) {
    val goals = uiState.savingGoals
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { GoalsBottomNavigation(onNavigateHome, onNavigateToAddExpense, onNavigateToHistory) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("Meta de ahorro", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)

            Spacer(modifier = Modifier.height(24.dp))

            if (goals.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.TrackChanges, contentDescription = null, tint = GastoUTextLightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Aún no tienes metas de ahorro.", fontSize = 14.sp, color = GastoUTextGray)
                        Text("¡Crea una para empezar a guardar!", fontSize = 12.sp, color = GastoUTextLightGray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(goals) { goal ->
                        GoalCard(goal = goal, onClick = { onNavigateToGoalDetail(goal.id) })
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GastoUButton(
                text = "+ Fijar nueva meta",
                onClick = onNavigateToAddGoal
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Motivation Card
            Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GastoUBackgroundLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Pequeños hábitos,\ngrandes logros",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                                color = GastoUGreen
                            )
                            Icon(Icons.Outlined.School, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(48.dp))
                        }
                    }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun GoalCard(goal: SavingGoal, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, GastoUBackgroundLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Jar Icon placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(GastoUBackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Savings, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(goal.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)

                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$${String.format(Locale.US, "%,.0f", goal.currentAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
                    Text(" / $${String.format(Locale.US, "%,.0f", goal.targetAmount)}", fontSize = 12.sp, color = GastoUTextLightGray)
                }
            }
        }
    }
}

@Composable
fun GoalsBottomNavigation(onNavigateHome: () -> Unit, onNavigateToAddExpense: () -> Unit, onNavigateToHistory: () -> Unit) {
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
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Filled.TrackChanges, contentDescription = "Metas") },
            label = { Text("Metas", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GastoUGreen,
                selectedTextColor = GastoUGreen,
                indicatorColor = GastoUBackgroundLight,
                unselectedIconColor = GastoUTextLightGray,
                unselectedTextColor = GastoUTextLightGray
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GoalsScreenPreview() {
    GastoUTheme {
        GoalsScreen(
            uiState = GoalsUiState(
                savingGoals = listOf(
                    SavingGoal(name = "Laptop para la uni", targetAmount = 2500.0, currentAmount = 1180.0, category = "Tecnología", targetDate = null)
                )
            ),
            onNavigateHome = {},
            onNavigateToAddExpense = {},
            onNavigateToHistory = {},
            onNavigateToAddGoal = {},
            onNavigateToGoalDetail = {}
        )
    }
}