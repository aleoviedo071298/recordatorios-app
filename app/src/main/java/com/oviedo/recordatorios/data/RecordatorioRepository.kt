package com.oviedo.recordatorios.data

import com.oviedo.recordatorios.Recordatorio

interface RecordatorioRepository {
    suspend fun obtenerRecordatorios(): List<Recordatorio>

    suspend fun obtenerPorId(id: Int): Recordatorio?

    suspend fun guardarRecordatorio(
        recordatorio: Recordatorio
    )
}