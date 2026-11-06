package com.oviedo.recordatorios.ui.state

import com.oviedo.recordatorios.Recordatorio

sealed interface DetalleUiState {
    data object Cargando : DetalleUiState
    data class Contenido(
        val recordatorio: Recordatorio
    ) : DetalleUiState
    data class Error(
        val mensaje: String
    ) : DetalleUiState
}
