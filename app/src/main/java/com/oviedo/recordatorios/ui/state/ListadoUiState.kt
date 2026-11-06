package com.oviedo.recordatorios.ui.state

import com.oviedo.recordatorios.Recordatorio

sealed interface ListadoUiState {
    data object Cargando : ListadoUiState
    data object Vacio : ListadoUiState
    data class Contenido(
        val recordatorios: List<Recordatorio>
    ) : ListadoUiState
    data class Error(
        val mensaje: String
    ) : ListadoUiState
}