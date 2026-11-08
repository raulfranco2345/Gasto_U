package com.example.gastou.domain.model

/**
 * Resultado de una operación que escribe en la base local y después intenta
 * enviarse a la API. La interfaz lo usa para mostrar el mensaje adecuado.
 */
sealed interface SyncOutcome {
    /** Guardado localmente y confirmado por el servidor. */
    data object Synced : SyncOutcome

    /** Guardado localmente; se enviará a la API cuando vuelva la conexión. */
    data class SavedOffline(val message: String) : SyncOutcome

    /** No se pudo completar la operación. */
    data class Failed(val message: String) : SyncOutcome
}
