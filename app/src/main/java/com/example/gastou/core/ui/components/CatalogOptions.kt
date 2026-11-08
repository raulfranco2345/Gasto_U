package com.example.gastou.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.theme.*

/*
 * Catálogos visuales compartidos por varias pantallas (categorías y métodos de pago)
 * y componentes pequeños reutilizables.
 */

data class CategoryOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val iconTint: Color
)

val defaultCategories = listOf(
    CategoryOption("Comida y Bebidas", "Comida y Bebidas", "Cafetería, snacks, menú del día, agua", Icons.Outlined.Restaurant, Color(0xFFE6F9F5), CatFood),
    CategoryOption("Transporte", "Transporte", "Micro, metro, pasajes, bici, recargas", Icons.Outlined.DirectionsBus, Color(0xFFEFF6FF), CatTransport),
    CategoryOption("Estudios y Materiales", "Estudios y Materiales", "Copias, libros, papelería, impresiones", Icons.Outlined.MenuBook, Color(0xFFFEF9C3), CatStudy),
    CategoryOption("Ocio y Salidas", "Ocio y Salidas", "Cine, reuniones, juegos, salidas", Icons.Outlined.LocalActivity, Color(0xFFFEE2E2), CatLeisure),
    CategoryOption("Salud y Bienestar", "Salud y Bienestar", "Farmacia, atención médica, gimnasio", Icons.Outlined.FavoriteBorder, Color(0xFFF3E8FF), CatHealth),
    CategoryOption("Ahorro e Inversión", "Ahorro e Inversión", "Aporte a metas, apartado SOS, fondo", Icons.Outlined.Eco, Color(0xFFD1FAE5), CatSavings),
    CategoryOption("Otros / Imprevistos", "Otros / Imprevistos", "Gastos varios, comisiones, emergencias", Icons.Outlined.MoreHoriz, Color(0xFFF1F5F9), CatOther)
)


data class PaymentMethodOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color
)

val paymentMethods = listOf(
    PaymentMethodOption("Tarjeta prepago U", "Tarjeta prepago U", "Saldo en campus, becas y transferencias", Icons.Outlined.CreditCard, GastoUGreen),
    PaymentMethodOption("Efectivo", "Efectivo", "Billetes y monedas en mano", Icons.Outlined.Money, GastoUGreen),
    PaymentMethodOption("Tarjeta de débito / crédito", "Tarjeta de débito / crédito", "Banco vinculado (BBVA, Santander, etc.)", Icons.Outlined.CreditCard, Color(0xFF3B82F6)), // Blue
    PaymentMethodOption("Transferencia / SPEI / QR", "Transferencia / SPEI / QR", "Pagos digitales inmediatos", Icons.Outlined.SyncAlt, Color(0xFFF59E0B)), // Orange/Yellow
    PaymentMethodOption("Billetera digital", "Billetera digital", "Mercado Pago, Apple Pay, Google Wallet", Icons.Outlined.PhoneIphone, Color(0xFFA855F7)) // Purple
)


@Composable
fun QuickActionButton(text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GastoUBackgroundLight,
        onClick = onClick
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GastoUGreen,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

