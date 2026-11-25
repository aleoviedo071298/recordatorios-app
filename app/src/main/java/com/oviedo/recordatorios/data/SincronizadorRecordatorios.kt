package com.oviedo.recordatorios.data

import android.util.Log
import com.oviedo.recordatorios.data.local.RecordatorioDao
import com.oviedo.recordatorios.data.local.RecordatorioEntity
import com.oviedo.recordatorios.data.mapper.toDto
import com.oviedo.recordatorios.data.mapper.toEntityOrNull
import com.oviedo.recordatorios.data.remote.RecordatorioDto
import com.oviedo.recordatorios.data.remote.RecordatoriosApi
import com.oviedo.recordatorios.data.remote.ResultadoApi
import com.oviedo.recordatorios.data.remote.ejecutarApi
import com.oviedo.recordatorios.data.sync.ResultadoSincronizacion
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Sincroniza Room con el servicio remoto en dos pasos:
 *
 * 1. Envío: manda los cambios locales pendientes (crear, modificar, eliminar).
 * 2. Descarga: trae los registros del servicio y los guarda en Room.
 *
 * Room sigue siendo la fuente de verdad: la interfaz recibe los cambios
 * por los Flow del DAO, nunca directamente desde la red.
 */
class SincronizadorRecordatorios(
    private val dao: RecordatorioDao,
    private val api: RecordatoriosApi
) {

    suspend fun sincronizar(): ResultadoSincronizacion = mutex.withLock {
        val envio = enviarPendientes()
        if (envio == ResultadoSincronizacion.REINTENTAR) {
            return@withLock envio
        }
        descargarRemotos()
    }

    // ---------------------------------------------------------------
    // 1. Envío de pendientes
    // ---------------------------------------------------------------

    private suspend fun enviarPendientes(): ResultadoSincronizacion {
        for (entidad in dao.obtenerPendientes()) {

            val uuid = entidad.idRemoto
                ?: UUID.randomUUID().toString().also {
                    dao.asignarIdRemoto(entidad.id, it)
                }

            val resultado = if (entidad.eliminado) {
                ejecutarApi { api.eliminarRecordatorio(uuid) }
            } else {
                enviar(entidad.toDto(uuid))
            }

            when {
                confirmado(entidad, resultado) -> {
                    val filas = if (entidad.eliminado) {
                        dao.eliminarConfirmado(entidad.id, entidad.versionLocal)
                    } else {
                        dao.marcarSincronizado(entidad.id, entidad.versionLocal)
                    }
                    // Si filas == 0, el registro cambió durante el envío:
                    // queda pendiente y se envía en la próxima pasada.
                    log("Confirmado id=${entidad.id} uuid=$uuid filas=$filas")
                }

                esRechazoPermanente(resultado) -> {
                    // 4xx: reenviar lo mismo no lo va a arreglar. Se marca con
                    // error y se sigue con el resto, sin bloquear la cola.
                    val mensaje = (resultado as ResultadoApi.ErrorHttp).let {
                        "${it.mensaje} (HTTP ${it.codigo})"
                    }
                    dao.marcarError(entidad.id, entidad.versionLocal, mensaje)
                    log("Rechazado id=${entidad.id}: $mensaje")
                }

                else -> {
                    // Sin conexión, timeout o 5xx: el registro sigue pendiente.
                    log("Fallo transitorio id=${entidad.id}: $resultado")
                    return ResultadoSincronizacion.REINTENTAR
                }
            }
        }
        return ResultadoSincronizacion.COMPLETADA
    }

    /**
     * PUT con el UUID; si el servicio todavía no lo conoce (404), POST.
     * Como el servicio usa el UUID como identificador, repetir el envío
     * actualiza el mismo registro en lugar de crear uno nuevo.
     */
    private suspend fun enviar(dto: RecordatorioDto): ResultadoApi<RecordatorioDto> {
        val id = requireNotNull(dto.id)
        val actualizacion = ejecutarApi { api.actualizarRecordatorio(id, dto) }

        return if (actualizacion is ResultadoApi.ErrorHttp && actualizacion.codigo == 404) {
            ejecutarApi { api.crearRecordatorio(dto) }
        } else {
            actualizacion
        }
    }

    private fun confirmado(entidad: RecordatorioEntity, resultado: ResultadoApi<*>): Boolean =
        when (resultado) {
            is ResultadoApi.Exito -> true
            // Eliminar algo que el servicio ya no tiene cuenta como confirmado.
            is ResultadoApi.ErrorHttp -> entidad.eliminado && resultado.codigo == 404
            else -> false
        }

    private fun esRechazoPermanente(resultado: ResultadoApi<*>): Boolean =
        resultado is ResultadoApi.ErrorHttp &&
            resultado.codigo in 400..499 &&
            resultado.codigo != 408 &&
            resultado.codigo != 429

    // ---------------------------------------------------------------
    // 2. Descarga de registros remotos
    // ---------------------------------------------------------------

    private suspend fun descargarRemotos(): ResultadoSincronizacion {
        return when (val resultado = ejecutarApi { api.obtenerRecordatorios() }) {

            is ResultadoApi.Exito -> {
                var nuevos = 0
                var actualizados = 0
                var descartados = 0

                for (dto in resultado.datos) {
                    val remota = dto.toEntityOrNull()
                    if (remota == null) {
                        descartados++
                        continue
                    }

                    val local = dao.obtenerPorIdRemoto(requireNotNull(remota.idRemoto))
                    when {
                        local == null -> {
                            if (dao.insertarSiNoExiste(remota) > 0) nuevos++
                        }
                        // Si hay cambios locales sin enviar, gana la versión local.
                        !local.pendienteSincronizacion && !local.eliminado -> {
                            actualizados += dao.actualizarDesdeServidor(
                                id = local.id,
                                titulo = remota.titulo,
                                descripcion = remota.descripcion,
                                categoria = remota.categoria,
                                prioridad = remota.prioridad,
                                estado = remota.estado,
                                fecha = remota.fecha
                            )
                        }
                    }
                }

                log("Descarga: nuevos=$nuevos actualizados=$actualizados descartados=$descartados")
                ResultadoSincronizacion.COMPLETADA
            }

            // Un 4xx en la descarga no se arregla reintentando; los datos
            // locales se conservan y se vuelve a descargar en la próxima sincronización.
            is ResultadoApi.ErrorHttp -> if (esRechazoPermanente(resultado)) {
                log("Descarga rechazada: HTTP ${resultado.codigo}")
                ResultadoSincronizacion.COMPLETADA
            } else {
                ResultadoSincronizacion.REINTENTAR
            }

            else -> ResultadoSincronizacion.REINTENTAR
        }
    }

    private fun log(mensaje: String) = Log.d("SYNC", mensaje)

    private companion object {
        /** Compartido por todas las instancias: nunca dos sincronizaciones a la vez. */
        val mutex = Mutex()
    }
}
