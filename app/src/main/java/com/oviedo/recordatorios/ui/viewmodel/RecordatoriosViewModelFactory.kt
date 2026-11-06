package com.oviedo.recordatorios.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.oviedo.recordatorios.data.RecordatorioRepository

class RecordatoriosViewModelFactory(
    private val repositorio: RecordatorioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(
                RecordatoriosViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return RecordatoriosViewModel(repositorio) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
