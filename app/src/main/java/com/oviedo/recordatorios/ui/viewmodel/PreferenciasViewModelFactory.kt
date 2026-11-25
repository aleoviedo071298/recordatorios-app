package com.oviedo.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.oviedo.recordatorios.data.preferences.PreferenciasRepository

class PreferenciasViewModelFactory(
    private val repositorio: PreferenciasRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(
                PreferenciasViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return PreferenciasViewModel(repositorio) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
