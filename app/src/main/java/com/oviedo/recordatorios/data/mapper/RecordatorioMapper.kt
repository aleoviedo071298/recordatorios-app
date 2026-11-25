package com.oviedo.recordatorios.data.mapper

import com.oviedo.recordatorios.Categoria
import com.oviedo.recordatorios.EstadoRecordatorio
import com.oviedo.recordatorios.EstadoSincronizacion
import com.oviedo.recordatorios.Prioridad
import com.oviedo.recordatorios.Recordatorio
import com.oviedo.recordatorios.data.local.RecordatorioEntity
import com.oviedo.recordatorios.data.remote.RecordatorioDto

// ---------------------------------------------------------------
// Conversión de los tipos del dominio a texto (Room y servicio)
// ---------------------------------------------------------------

fun Categoria.aTexto(): String = when (this) {
    Categoria.Estudio -> "Estudio"
    Categoria.Trabajo -> "Trabajo"
    Categoria.Personal -> "Personal"
}

fun Prioridad.aTexto(): String = when (this) {
    Prioridad.Alta -> "Alta"
    Prioridad.Media -> "Media"
    Prioridad.Baja -> "Baja"
}

fun EstadoRecordatorio.aTexto(): String = when (this) {
    EstadoRecordatorio.Pendiente -> "Pendiente"
    EstadoRecordatorio.Completado -> "Completado"
    EstadoRecordatorio.Cancelado -> "Cancelado"
}

/** Un valor desconocido usa la categoría predeterminada. */
fun categoriaDesdeTexto(texto: String?): Categoria = when (texto) {
    "Estudio" -> Categoria.Estudio
    "Trabajo" -> Categoria.Trabajo
    else -> Categoria.Personal
}

fun prioridadDesdeTexto(texto: String?): Prioridad = when (texto) {
    "Alta" -> Prioridad.Alta
    "Baja" -> Prioridad.Baja
    else -> Prioridad.Media
}

fun estadoDesdeTexto(texto: String?): EstadoRecordatorio = when (texto) {
    "Completado" -> EstadoRecordatorio.Completado
    "Cancelado" -> EstadoRecordatorio.Cancelado
    else -> EstadoRecordatorio.Pendiente
}

// ---------------------------------------------------------------
// Dominio <-> Entity (Room)
// ---------------------------------------------------------------

/**
 * Convierte el modelo de dominio a entidad. Los campos de
 * sincronización los completa el repositorio según la operación.
 */
fun Recordatorio.toEntity(): RecordatorioEntity = RecordatorioEntity(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    categoria = categoria.aTexto(),
    prioridad = prioridad.aTexto(),
    estado = estado.aTexto(),
    fecha = if (fecha > 0L) fecha else System.currentTimeMillis()
)

fun RecordatorioEntity.toDomain(): Recordatorio = Recordatorio(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    categoria = categoriaDesdeTexto(categoria),
    prioridad = prioridadDesdeTexto(prioridad),
    estado = estadoDesdeTexto(estado),
    fecha = fecha,
    estadoSincronizacion = when {
        !pendienteSincronizacion -> EstadoSincronizacion.Sincronizado
        errorSincronizacion != null -> EstadoSincronizacion.Error(errorSincronizacion)
        else -> EstadoSincronizacion.Pendiente
    }
)

// ---------------------------------------------------------------
// Entity (Room) <-> DTO (servicio remoto)
// ---------------------------------------------------------------

/** Arma el cuerpo que se envía al servicio, identificado por su UUID. */
fun RecordatorioEntity.toDto(idRemoto: String): RecordatorioDto = RecordatorioDto(
    id = idRemoto,
    titulo = titulo,
    descripcion = descripcion,
    categoria = categoria,
    prioridad = prioridad,
    estado = estado,
    fecha = fecha
)

/**
 * Convierte un registro recibido del servicio en una entidad ya
 * sincronizada. Devuelve null si el DTO no es válido, para que un dato
 * defectuoso del servidor nunca llegue a la base local ni a la interfaz.
 * Los textos desconocidos se normalizan a los valores del dominio.
 */
fun RecordatorioDto.toEntityOrNull(): RecordatorioEntity? {
    val idValido = id?.takeIf { it.isNotBlank() } ?: return null
    val tituloValido = titulo?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    return RecordatorioEntity(
        id = 0,
        titulo = tituloValido,
        descripcion = descripcion?.trim()?.ifBlank { null },
        categoria = categoriaDesdeTexto(categoria).aTexto(),
        prioridad = prioridadDesdeTexto(prioridad).aTexto(),
        estado = estadoDesdeTexto(estado).aTexto(),
        fecha = fecha?.takeIf { it > 0L } ?: System.currentTimeMillis(),
        pendienteSincronizacion = false,
        idRemoto = idValido,
        eliminado = false,
        versionLocal = 0L,
        errorSincronizacion = null
    )
}
