package com.oviedo.recordatorios

/**
 * Modelo de dominio. Es lo que usan el ViewModel y la interfaz;
 * no depende de Room ni del contrato del servicio remoto.
 *
 * Un id igual a 0 representa un recordatorio nuevo que todavía no
 * fue guardado (Room asigna el identificador definitivo).
 */
data class Recordatorio(
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: EstadoRecordatorio,
    val ubicacion: String? = null,
    val fechaAviso: String? = null,
    val fecha: Long = 0L,
    val estadoSincronizacion: EstadoSincronizacion = EstadoSincronizacion.Pendiente
) {
    init {
        require(id >= 0) {
            "El ID no puede ser negativo"
        }
        require(titulo.isNotBlank()) {
            "El titulo no puede estar en blanco"
        }
    }
}

sealed class Prioridad {
    data object Alta : Prioridad()
    data object Media : Prioridad()
    data object Baja : Prioridad()
}

sealed class Categoria {
    data object Estudio : Categoria()
    data object Trabajo : Categoria()
    data object Personal : Categoria()
}

sealed class EstadoRecordatorio {
    data object Pendiente : EstadoRecordatorio()
    data object Completado : EstadoRecordatorio()
    data object Cancelado : EstadoRecordatorio()
}

/**
 * Situación de un recordatorio respecto del servicio remoto.
 */
sealed interface EstadoSincronizacion {
    /** Tiene cambios locales que todavía no confirmó el servicio. */
    data object Pendiente : EstadoSincronizacion

    /** El servicio confirmó la última versión local. */
    data object Sincronizado : EstadoSincronizacion

    /** El servicio rechazó el cambio; necesita un nuevo intento manual. */
    data class Error(val mensaje: String) : EstadoSincronizacion
}
