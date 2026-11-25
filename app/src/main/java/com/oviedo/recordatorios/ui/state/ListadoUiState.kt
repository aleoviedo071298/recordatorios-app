package com.oviedo.recordatorios.ui.state

import com.oviedo.recordatorios.Recordatorio

sealed interface ListadoUiState {
    data object Cargando : ListadoUiState

    /**
     * No hay nada para mostrar. [hayOcultos] indica que existen
     * recordatorios guardados que las preferencias ocultan.
     */
    data class Vacio(val hayOcultos: Boolean = false) : ListadoUiState

    data class Contenido(
        val recordatorios: List<Recordatorio>
    ) : ListadoUiState

    data class Error(
        val mensaje: String
    ) : ListadoUiState
}
