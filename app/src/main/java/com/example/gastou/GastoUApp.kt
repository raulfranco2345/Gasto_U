package com.example.gastou

import android.app.Application
import com.example.gastou.di.AppContainer

/**
 * Clase Application: crea el contenedor de dependencias una sola vez
 * al iniciar el proceso de la app.
 */
class GastoUApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
