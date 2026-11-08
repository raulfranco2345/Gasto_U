package com.example.gastou

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gastou.core.ui.theme.GastoUGreen
import com.example.gastou.core.ui.theme.GastoUTheme
import com.example.gastou.di.AppViewModelProvider
import com.example.gastou.navigation.AppNavGraph

/**
 * Actividad de entrada: solo aloja el tema y el grafo de navegación.
 * Extiende FragmentActivity porque el diálogo biométrico (BiometricPrompt) lo requiere.
 */
class MainActivity : FragmentActivity() {

    private val mainViewModel: MainViewModel by viewModels { AppViewModelProvider.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GastoUTheme {
                val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()
                val isLoggedIn by mainViewModel.isLoggedIn.collectAsStateWithLifecycle()

                val start = startDestination
                if (start == null) {
                    // Mientras se lee la sesión guardada en DataStore
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GastoUGreen)
                    }
                } else {
                    AppNavGraph(startDestination = start, isLoggedIn = isLoggedIn)
                }
            }
        }
    }
}
