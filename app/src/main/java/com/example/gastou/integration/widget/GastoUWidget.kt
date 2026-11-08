package com.example.gastou.integration.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.gastou.GastoUApp
import com.example.gastou.MainActivity
import com.example.gastou.core.ui.components.formatMonth
import kotlinx.coroutines.flow.firstOrNull
import java.time.YearMonth
import java.util.Locale

/** Datos que muestra el widget. */
private data class WidgetData(
    val month: YearMonth,
    val spent: Double,
    val budget: Double?,
    val loggedIn: Boolean
)

/**
 * Integración de widget (Jetpack Glance): muestra en la pantalla de inicio del teléfono
 * cuánto se ha gastado en el mes y cuánto queda del presupuesto. Al tocarlo abre la app.
 * Los datos se leen de Room a través de los repositorios.
 */
class GastoUWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = loadData(context)
        provideContent { WidgetContent(data) }
    }

    private suspend fun loadData(context: Context): WidgetData {
        val container = (context.applicationContext as GastoUApp).container
        val month = YearMonth.now()
        val loggedIn = container.authRepository.session.firstOrNull() != null
        if (!loggedIn) return WidgetData(month, 0.0, null, false)
        return WidgetData(
            month = month,
            spent = container.expenseRepository.totalOfMonth(month),
            budget = container.budgetRepository.budgetOf(month),
            loggedIn = true
        )
    }
}

private val Green = Color(0xFF16A34A)
private val Dark = Color(0xFF1F2937)
private val Gray = Color(0xFF6B7280)

@androidx.compose.runtime.Composable
private fun WidgetContent(data: WidgetData) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.White)
            .cornerRadius(16.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "GastoU",
            style = TextStyle(color = fixed(Green), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(GlanceModifier.height(4.dp))

        if (!data.loggedIn) {
            Text(
                text = "Inicia sesión para ver tu resumen",
                style = TextStyle(color = fixed(Gray), fontSize = 12.sp)
            )
            return@Column
        }

        Text(
            text = "Gastado en ${formatMonth(data.month)}",
            style = TextStyle(color = fixed(Gray), fontSize = 12.sp)
        )
        Text(
            text = money(data.spent),
            style = TextStyle(color = fixed(Dark), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(GlanceModifier.height(6.dp))

        val budget = data.budget
        if (budget != null && budget > 0) {
            val fraction = (data.spent / budget).toFloat().coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = fraction,
                modifier = GlanceModifier.fillMaxWidth(),
                color = fixed(if (data.spent > budget) Color(0xFFDC2626) else Green),
                backgroundColor = fixed(Color(0xFFE5E7EB))
            )
            Spacer(GlanceModifier.height(4.dp))
            val remaining = budget - data.spent
            Text(
                text = if (remaining >= 0) "Te quedan ${money(remaining)}" else "Excedido por ${money(-remaining)}",
                style = TextStyle(color = fixed(Gray), fontSize = 11.sp)
            )
        } else {
            Text(
                text = "Sin presupuesto este mes",
                style = TextStyle(color = fixed(Gray), fontSize = 11.sp)
            )
        }
    }
}

private fun fixed(color: Color) = ColorProvider(day = color, night = color)

private fun money(value: Double) = "$" + String.format(Locale.US, "%,.2f", value)

/** Receptor que Android usa para crear y actualizar el widget (declarado en el Manifest). */
class GastoUWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GastoUWidget()
}

/** Pide a Android que vuelva a dibujar el widget cuando cambian los gastos o el presupuesto. */
class WidgetUpdater(private val context: Context) {
    suspend fun refresh() {
        runCatching { GastoUWidget().updateAll(context) }
    }
}
