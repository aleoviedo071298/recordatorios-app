package com.oviedo.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oviedo.recordatorios.data.preferences.OrdenListado
import com.oviedo.recordatorios.data.preferences.PreferenciasApp
import com.oviedo.recordatorios.data.preferences.PreferenciasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferenciasViewModel(
    private val repositorio: PreferenciasRepository
) : ViewModel() {

    val preferencias: StateFlow<PreferenciasApp> = repositorio.preferencias.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PreferenciasApp()
    )

    fun cambiarTemaOscuro(activar: Boolean) {
        viewModelScope.launch { repositorio.cambiarTemaOscuro(activar) }
    }

    fun cambiarMostrarCompletados(mostrar: Boolean) {
        viewModelScope.launch { repositorio.cambiarMostrarCompletados(mostrar) }
    }

    fun cambiarOrdenListado(orden: OrdenListado) {
        viewModelScope.launch { repositorio.cambiarOrdenListado(orden) }
    }

    fun guardarOrdenInvalidoParaPrueba() {
        viewModelScope.launch { repositorio.guardarOrdenInvalidoParaPrueba() }
    }
}
