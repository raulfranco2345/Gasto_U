package com.example.gastou.presentation.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.ErrorBanner
import com.example.gastou.core.ui.components.GastoUButton
import com.example.gastou.core.ui.components.GastoULogo
import com.example.gastou.core.ui.theme.GastoUBackgroundLight
import com.example.gastou.core.ui.theme.GastoUGreen
import com.example.gastou.core.ui.theme.GastoUTextGray
import com.example.gastou.core.ui.theme.GastoUTheme

/** Pantalla que se muestra al abrir la app cuando el bloqueo biométrico está activo. */
@Composable
fun LockScreen(
    userName: String,
    errorMessage: String?,
    onUnlock: () -> Unit,
    onUseAnotherAccount: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GastoULogo()
        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(GastoUBackgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = GastoUGreen, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = if (userName.isNotBlank()) "Hola, ${userName.substringBefore(" ")}" else "GastoU está protegido",
            fontSize = 20.sp,
            color = Color(0xFF1F2937)
        )
        Text(
            text = "Confirma tu identidad para ver tus finanzas",
            fontSize = 14.sp,
            color = GastoUTextGray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        ErrorBanner(errorMessage)
        Spacer(Modifier.height(16.dp))
        GastoUButton(text = "Desbloquear", onClick = onUnlock)
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onUseAnotherAccount) {
            Text("Usar otra cuenta", color = GastoUTextGray)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LockScreenPreview() {
    GastoUTheme {
        LockScreen(userName = "Walter Franco", errorMessage = null, onUnlock = {}, onUseAnotherAccount = {})
    }
}
