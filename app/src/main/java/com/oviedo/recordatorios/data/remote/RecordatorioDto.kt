package com.oviedo.recordatorios.data.remote

/**
 * Contrato del servicio remoto. Los campos son nullables porque Gson
 * puede dejarlos en null si el servidor envía un JSON incompleto; la
 * validación se hace al convertirlo a entidad (toEntityOrNull).
 */
data class RecordatorioDto(
    val id: String?,
    val titulo: String?,
    val descripcion: String?,
    val categoria: String?,
    val prioridad: String?,
    val estado: String?,
    val fecha: Long?
)
