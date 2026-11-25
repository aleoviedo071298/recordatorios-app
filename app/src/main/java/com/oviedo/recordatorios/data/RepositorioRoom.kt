package com.oviedo.recordatorios.data

import android.content.Context
import com.oviedo.recordatorios.EstadoRecordatorio
import com.oviedo.recordatorios.Prioridad
import com.oviedo.recordatorios.Recordatorio
import com.oviedo.recordatorios.data.local.RecordatorioDao
import com.oviedo.recordatorios.data.mapper.aTexto
import com.oviedo.recordatorios.data.mapper.toDomain
import com.oviedo.recordatorios.data.mapper.toEntity
import com.oviedo.recordatorios.data.preferences.OrdenListado
import com.oviedo.recordatorios.data.preferences.PreferenciasRepository
import com.oviedo.recordatorios.data.sync.ProgramadorSincronizacion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Repositorio local-first. Coordina las tres fuentes:
 *
 * - Room: fuente de verdad. Toda lectura de la interfaz sale de acá.
 * - DataStore: decide qué se muestra y en qué orden.
 * - Servicio remoto: se usa solo a través de la sincronización en
 *   segundo plano (WorkManager), nunca desde una pantalla.
 *
 * Toda escritura se aplica primero en Room y queda pendiente; después se
 * programa la sincronización, que la envía cuando hay conexión.
 */
class RepositorioRoom(
    private val dao: RecordatorioDao,
    private val preferencias: PreferenciasRepository,
    private val context: Context
) : RecordatorioRepository {

    // ---------------------------------------------------------------
    // Lecturas (Flow desde Room)
    // ---------------------------------------------------------------

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observarListado(): Flow<ListadoRecordatorios> =
        preferencias.preferencias
            .distinctUntilChanged()
            .flatMapLatest { prefs ->
                val origen = if (prefs.mostrarCompletados) {
                    dao.obtenerTodos()
                } else {
                    dao.obtenerExcluyendoEstado(EstadoRecordatorio.Completado.aTexto())
                }

                combine(origen, dao.contarVisibles()) { entidades, total ->
                    ListadoRecordatorios(
                        recordatorios = ordenar(
                            entidades.map { it.toDomain() },
                            prefs.ordenListado
                        ),
                        totalGuardados = total
                    )
                }
            }

    override fun observarPorId(id: Int): Flow<Recordatorio?> =
        dao.obtenerPorId(id).map { it?.toDomain() }

    override fun observarEstadoSincronizacion(): Flow<EstadoSincronizacionGeneral> =
        combine(
            dao.observarCantidadPendientes(),
            dao.observarCantidadConError(),
            ProgramadorSincronizacion.observarEnCurso(context)
        ) { pendientes, conError, enCurso ->
            EstadoSincronizacionGeneral(
                pendientes = pendientes,
                conError = conError,
                enCurso = enCurso
            )
        }

    override suspend fun obtenerPorId(id: Int): Recordatorio? =
        dao.obtenerPorId(id).first()?.toDomain()

    // ---------------------------------------------------------------
    // Escrituras (primero Room, después sincronización)
    // ---------------------------------------------------------------

    override suspend fun crearRecordatorio(recordatorio: Recordatorio) {
        val entidad = recordatorio.toEntity().copy(
            id = 0,
            idRemoto = UUID.randomUUID().toString(),
            pendienteSincronizacion = true,
            eliminado = false,
            versionLocal = 1L,
            errorSincronizacion = null
        )
        dao.insertar(entidad)
        ProgramadorSincronizacion.programar(context)
    }

    override suspend fun actualizarRecordatorio(recordatorio: Recordatorio) {
        val actual = dao.obtenerPorId(recordatorio.id).first()
            ?: throw IllegalArgumentException("Recordatorio no encontrado")

        val actualizada = recordatorio.toEntity().copy(
            id = actual.id,
            fecha = actual.fecha,
            idRemoto = actual.idRemoto ?: UUID.randomUUID().toString(),
            pendienteSincronizacion = true,
            eliminado = false,
            versionLocal = actual.versionLocal + 1L,
            errorSincronizacion = null
        )
        dao.actualizar(actualizada)
        ProgramadorSincronizacion.programar(context)
    }

    override suspend fun eliminarRecordatorio(id: Int) {
        val actual = dao.obtenerPorId(id).first()
            ?: throw IllegalArgumentException("Recordatorio no encontrado")

        // Borrado lógico: el registro desaparece de la interfaz, pero se
        // conserva hasta que el servicio confirme la eliminación.
        dao.actualizar(
            actual.copy(
                idRemoto = actual.idRemoto ?: UUID.randomUUID().toString(),
                eliminado = true,
                pendienteSincronizacion = true,
                versionLocal = actual.versionLocal + 1L,
                errorSincronizacion = null
            )
        )
        ProgramadorSincronizacion.programar(context)
    }

    override suspend fun reintentarSincronizacion() {
        dao.limpiarErrores()
        ProgramadorSincronizacion.programar(context)
    }

    private fun ordenar(lista: List<Recordatorio>, orden: OrdenListado): List<Recordatorio> =
        when (orden) {
            OrdenListado.FECHA -> lista.sortedByDescending { it.fecha }
            OrdenListado.TITULO -> lista.sortedBy { it.titulo.lowercase() }
            OrdenListado.PRIORIDAD -> lista.sortedWith(
                compareBy<Recordatorio> {
                    when (it.prioridad) {
                        Prioridad.Alta -> 0
                        Prioridad.Media -> 1
                        Prioridad.Baja -> 2
                    }
                }.thenByDescending { it.fecha }
            )
        }
}
