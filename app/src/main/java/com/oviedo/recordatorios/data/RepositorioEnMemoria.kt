package com.oviedo.recordatorios.data

import com.oviedo.recordatorios.Recordatorio
import com.oviedo.recordatorios.recordatorios

/**
 * Implementación en memoria del repositorio.
 *
 * Los parámetros de simulación permiten comprobar todos los estados de la
 * interfaz sin una base de datos ni un servicio remoto. Se configuran en
 * RecordatoriosApplication.
 */
class RepositorioEnMemoria(
    private val simularListaVacia: Boolean = false,
    private val simularErrorCarga: Boolean = false,
    private val simularErrorGuardado: Boolean = false
) : RecordatorioRepository {

    private val listaRecordatorios =
        if (simularListaVacia) mutableListOf() else recordatorios.toMutableList()

    override suspend fun obtenerRecordatorios(): List<Recordatorio> {
        if (simularErrorCarga) {
            throw Exception("No se pudieron cargar los recordatorios (error simulado).")
        }
        return listaRecordatorios.toList()
    }

    override suspend fun obtenerPorId(id: Int): Recordatorio? {
        return listaRecordatorios.find { it.id == id }
    }

    override suspend fun guardarRecordatorio(recordatorio: Recordatorio) {
        if (simularErrorGuardado) {
            throw Exception("No se pudo guardar el recordatorio (error simulado).")
        }
        listaRecordatorios.add(recordatorio)
    }
}
