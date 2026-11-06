package com.oviedo.recordatorios.ui.state

import com.oviedo.recordatorios.Categoria
import com.oviedo.recordatorios.Prioridad

const val LONGITUD_MINIMA_TITULO = 4
const val LONGITUD_MAXIMA_DESCRIPCION = 200

data class FormularioUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: Categoria? = null,
    val prioridad: Prioridad? = null,
    // Pasa a true cuando la persona modifica algún campo, para no
    // mostrar errores antes de que empiece a completar el formulario.
    val interactuado: Boolean = false,
    val estadoEnvio: EstadoEnvio = EstadoEnvio.Inactivo
) {
    val tituloValido: Boolean
        get() = titulo.trim().length >= LONGITUD_MINIMA_TITULO
    val descripcionValida: Boolean
        get() = descripcion.length <= LONGITUD_MAXIMA_DESCRIPCION
    val categoriaValida: Boolean
        get() = categoria != null
    val prioridadValida: Boolean
        get() = prioridad != null

    // Mensajes de validación (null = sin error visible)
    val errorTitulo: String?
        get() = if (interactuado && !tituloValido) {
            "El título debe tener al menos $LONGITUD_MINIMA_TITULO caracteres."
        } else null
    val errorDescripcion: String?
        get() = if (!descripcionValida) {
            "La descripción no puede superar los $LONGITUD_MAXIMA_DESCRIPCION caracteres."
        } else null
    val errorCategoria: String?
        get() = if (interactuado && !categoriaValida) "Seleccioná una categoría." else null
    val errorPrioridad: String?
        get() = if (interactuado && !prioridadValida) "Seleccioná una prioridad." else null

    val formularioValido: Boolean
        get() = tituloValido &&
                descripcionValida &&
                categoriaValida &&
                prioridadValida
    val enviando: Boolean
        get() = estadoEnvio == EstadoEnvio.Guardando
    val puedeGuardar: Boolean
        get() = formularioValido &&
                (estadoEnvio == EstadoEnvio.Inactivo ||
                        estadoEnvio is EstadoEnvio.Error)
}

sealed interface EstadoEnvio {
    data object Inactivo : EstadoEnvio
    data object Guardando : EstadoEnvio
    data object Exito : EstadoEnvio
    data class Error(val mensaje: String) : EstadoEnvio
}
