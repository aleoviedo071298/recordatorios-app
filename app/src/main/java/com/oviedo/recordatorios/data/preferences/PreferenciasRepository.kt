package com.oviedo.recordatorios.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Única instancia de DataStore de la app (archivo "configuracion"). */
private val Context.dataStore by preferencesDataStore(
    name = "configuracion"
)

/**
 * Guarda solo configuración simple. Los recordatorios se guardan en Room.
 */
class PreferenciasRepository(
    private val context: Context
) {

    private object Claves {
        val TEMA_OSCURO = booleanPreferencesKey("tema_oscuro")
        val MOSTRAR_COMPLETADOS = booleanPreferencesKey("mostrar_completados")
        val ORDEN_LISTADO = stringPreferencesKey("orden_listado")
    }

    val preferencias: Flow<PreferenciasApp> =
        context.dataStore.data
            .catch { error ->
                // Error de lectura recuperable: se usan los valores por defecto.
                if (error is IOException) {
                    Log.w("PREFERENCIAS", "No se pudieron leer las preferencias", error)
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { datos ->
                val ordenGuardado = datos[Claves.ORDEN_LISTADO]
                val orden = OrdenListado.desdeTexto(ordenGuardado)

                if (ordenGuardado != null && orden.name != ordenGuardado) {
                    Log.w(
                        "PREFERENCIAS",
                        "Orden desconocido \"$ordenGuardado\"; se usa ${orden.name}"
                    )
                }

                PreferenciasApp(
                    temaOscuro = datos[Claves.TEMA_OSCURO] ?: false,
                    mostrarCompletados = datos[Claves.MOSTRAR_COMPLETADOS] ?: true,
                    ordenListado = orden
                )
            }

    suspend fun cambiarTemaOscuro(activar: Boolean) {
        context.dataStore.edit { it[Claves.TEMA_OSCURO] = activar }
    }

    suspend fun cambiarMostrarCompletados(mostrar: Boolean) {
        context.dataStore.edit { it[Claves.MOSTRAR_COMPLETADOS] = mostrar }
    }

    suspend fun cambiarOrdenListado(orden: OrdenListado) {
        context.dataStore.edit { it[Claves.ORDEN_LISTADO] = orden.name }
    }

    /**
     * Solo para pruebas: guarda un valor que no corresponde a ninguna
     * opción, para comprobar que la app vuelve al orden predeterminado.
     */
    suspend fun guardarOrdenInvalidoParaPrueba() {
        context.dataStore.edit { it[Claves.ORDEN_LISTADO] = "ALFABETICO_INVERSO" }
    }
}
