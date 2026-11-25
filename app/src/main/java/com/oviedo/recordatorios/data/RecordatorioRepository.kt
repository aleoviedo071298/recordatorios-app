package com.oviedo.recordatorios.data

import com.oviedo.recordatorios.Recordatorio
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de la capa de datos que usan los ViewModel. La interfaz
 * nunca conoce Room, DataStore ni Retrofit: solo modelos de dominio.
 */
interface RecordatorioRepository {

    /** Listado ya filtrado y ordenado según las preferencias guardadas. */
    fun observarListado(): Flow<ListadoRecordatorios>

    fun observarPorId(id: Int): Flow<Recordatorio?>

    fun observarEstadoSincronizacion(): Flow<EstadoSincronizacionGeneral>

    suspend fun obtenerPorId(id: Int): Recordatorio?

    suspend fun crearRecordatorio(recordatorio: Recordatorio)

    suspend fun actualizarRecordatorio(recordatorio: Recordatorio)

    suspend fun eliminarRecordatorio(id: Int)

    /** Vuelve a habilitar los registros rechazados y lanza una sincronización. */
    suspend fun reintentarSincronizacion()
}

/**
 * @param recordatorios los elementos visibles según las preferencias.
 * @param totalGuardados cantidad total en la base; permite distinguir
 * "no hay recordatorios" de "hay recordatorios ocultos por las preferencias".
 */
data class ListadoRecordatorios(
    val recordatorios: List<Recordatorio>,
    val totalGuardados: Int
)

data class EstadoSincronizacionGeneral(
    val pendientes: Int = 0,
    val conError: Int = 0,
    val enCurso: Boolean = false
)
