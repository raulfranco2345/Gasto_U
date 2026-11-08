package com.example.gastou.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.ErrorBanner
import com.example.gastou.core.ui.components.FieldError
import com.example.gastou.core.ui.components.GastoUButton
import com.example.gastou.core.ui.components.GastoUTextField
import com.example.gastou.core.ui.components.formatYearMonth
import com.example.gastou.core.ui.theme.GastoUBackgroundLight
import com.example.gastou.core.ui.theme.GastoUGreen
import com.example.gastou.core.ui.theme.GastoUTextDark
import com.example.gastou.core.ui.theme.GastoUTextGray
import com.example.gastou.core.ui.theme.GastoUTheme
import com.example.gastou.domain.model.CategoryTotal
import com.example.gastou.domain.model.MonthlySummary
import com.example.gastou.domain.model.User
import java.time.YearMonth
import java.util.Locale

/**
 * Vista de perfil. Solo dibuja el estado y envía eventos; la comprobación de biometría
 * la hace el composable que la contiene (ver AppNavGraph) mediante el módulo de integración.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNavigateBack: () -> Unit,
    onRetrySummary: () -> Unit,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onEditNameChange: (String) -> Unit,
    onEditUniversityChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onBiometricToggle: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil", fontWeight = FontWeight.Bold, color = GastoUTextDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            if (uiState.isEditing) {
                EditProfileForm(
                    uiState = uiState,
                    onEditNameChange = onEditNameChange,
                    onEditUniversityChange = onEditUniversityChange,
                    onSave = onSaveProfile,
                    onCancel = onCancelEditing
                )
            } else {
                ProfileHeader(user = uiState.user, onEdit = onStartEditing)
            }

            Spacer(Modifier.height(24.dp))

            // Estadísticas guardadas en Room
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Gastos", uiState.totalExpenses.toString(), Modifier.weight(1f))
                StatCard("Metas activas", uiState.activeGoals.toString(), Modifier.weight(1f))
                StatCard("Completadas", uiState.completedGoals.toString(), Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            SectionTitle("Resumen del mes en la nube")
            Spacer(Modifier.height(8.dp))
            SummaryCard(uiState.summary, onRetrySummary)

            Spacer(Modifier.height(24.dp))

            SectionTitle("Seguridad")
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = GastoUBackgroundLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = GastoUGreen)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Bloquear con huella", fontWeight = FontWeight.Bold, color = GastoUTextDark)
                        Text(
                            "Pide tu huella, rostro o PIN al abrir GastoU",
                            fontSize = 12.sp,
                            color = GastoUTextGray
                        )
                    }
                    Switch(
                        checked = uiState.biometricEnabled,
                        onCheckedChange = onBiometricToggle,
                        colors = SwitchDefaults.colors(checkedTrackColor = GastoUGreen)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = Color(0xFFDC2626))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileHeader(user: User?, onEdit: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GastoUBackgroundLight),
            contentAlignment = Alignment.Center
        ) {
            val initial = user?.nombre?.firstOrNull()?.uppercaseChar()
            if (initial != null) {
                Text(initial.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
            } else {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = GastoUGreen)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(user?.nombre.orEmpty(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
            Text(user?.email.orEmpty(), fontSize = 14.sp, color = GastoUTextGray)
            if (!user?.universidad.isNullOrBlank()) {
                Text(user?.universidad.orEmpty(), fontSize = 13.sp, color = GastoUTextGray)
            }
            memberSince(user?.createdAt)?.let {
                Text("Miembro desde $it", fontSize = 12.sp, color = GastoUTextGray)
            }
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Outlined.Edit, contentDescription = "Editar perfil", tint = GastoUGreen)
        }
    }
}

@Composable
private fun EditProfileForm(
    uiState: ProfileUiState,
    onEditNameChange: (String) -> Unit,
    onEditUniversityChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column {
        SectionTitle("Editar perfil")
        Spacer(Modifier.height(8.dp))
        GastoUTextField(value = uiState.editName, onValueChange = onEditNameChange, placeholder = "Nombre completo")
        FieldError(uiState.nameError)
        Spacer(Modifier.height(12.dp))
        GastoUTextField(
            value = uiState.editUniversity,
            onValueChange = onEditUniversityChange,
            placeholder = "Universidad (opcional)"
        )
        Spacer(Modifier.height(16.dp))
        GastoUButton(
            text = if (uiState.isSaving) "Guardando..." else "Guardar cambios",
            onClick = onSave,
            enabled = !uiState.isSaving
        )
        TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Cancelar", color = GastoUTextGray)
        }
    }
}

@Composable
private fun SummaryCard(state: SummaryState, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GastoUBackgroundLight),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            when (state) {
                SummaryState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = GastoUGreen, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Consultando el servidor...", color = GastoUTextGray, fontSize = 14.sp)
                }
                is SummaryState.Error -> ErrorBanner(state.message, onRetry = onRetry)
                is SummaryState.Content -> SummaryContent(state.summary)
            }
        }
    }
}

@Composable
private fun SummaryContent(summary: MonthlySummary) {
    Text(formatYearMonth(summary.month), fontSize = 13.sp, color = GastoUTextGray)
    Text(money(summary.total), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
    summary.budget?.let {
        Text("Presupuesto: ${money(it)}", fontSize = 13.sp, color = GastoUTextGray)
    }
    Spacer(Modifier.height(8.dp))
    if (summary.byCategory.isEmpty()) {
        Text("Aún no hay gastos sincronizados este mes.", fontSize = 13.sp, color = GastoUTextGray)
    } else {
        summary.byCategory.forEach { item ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    "${item.category} (${item.count})",
                    fontSize = 13.sp,
                    color = GastoUTextDark,
                    modifier = Modifier.weight(1f)
                )
                Text(money(item.total), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GastoUBackgroundLight),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GastoUGreen)
            Text(label, fontSize = 11.sp, color = GastoUTextGray)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GastoUTextDark)
}

private fun money(value: Double) = "$" + String.format(Locale.US, "%,.2f", value)

private fun memberSince(createdAt: String?): String? = runCatching {
    formatYearMonth(YearMonth.parse(createdAt!!.take(7)))
}.getOrNull()

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    GastoUTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                user = User("1", "Walter Franco", "walter@correo.com", "UP Chiapas", "2026-11-08T00:00:00Z"),
                totalExpenses = 12,
                activeGoals = 2,
                summary = SummaryState.Content(
                    MonthlySummary(YearMonth.now(), 1520.0, 3000.0, listOf(CategoryTotal("Transporte", 420.0, 6)))
                )
            ),
            onNavigateBack = {}, onRetrySummary = {}, onStartEditing = {}, onCancelEditing = {},
            onEditNameChange = {}, onEditUniversityChange = {}, onSaveProfile = {},
            onBiometricToggle = {}, onLogout = {}
        )
    }
}
