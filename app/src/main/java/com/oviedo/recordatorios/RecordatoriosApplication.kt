package com.oviedo.recordatorios

import android.app.Application
import com.oviedo.recordatorios.data.RecordatorioRepository
import com.oviedo.recordatorios.data.RepositorioEnMemoria

class RecordatoriosApplication : Application() {

    // Para comprobar los estados vacío y de error, cambiar a true
    // la bandera correspondiente y volver a ejecutar la aplicación.
    val repositorio: RecordatorioRepository by lazy {
        RepositorioEnMemoria(
            simularListaVacia = false,
            simularErrorCarga = false,
            simularErrorGuardado = false
        )
    }
}
