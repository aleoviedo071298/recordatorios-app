package com.oviedo.recordatorios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oviedo.recordatorios.ui.navigation.RecordatoriosNavHost
import com.oviedo.recordatorios.ui.theme.RecordatoriosAppTheme
import com.oviedo.recordatorios.ui.viewmodel.PreferenciasViewModel
import com.oviedo.recordatorios.ui.viewmodel.PreferenciasViewModelFactory
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repositorio = (application as RecordatoriosApplication).repositorio
        val viewModelFactory = RecordatoriosViewModelFactory(repositorio)
        val preferenciasFactory = PreferenciasViewModelFactory(
            (application as RecordatoriosApplication).preferenciasRepository
        )
        setContent {
            val preferenciasViewModel: PreferenciasViewModel = viewModel(
                factory = preferenciasFactory
            )

            val preferencias by preferenciasViewModel.preferencias
                .collectAsStateWithLifecycle()

            RecordatoriosAppTheme(
                darkTheme = preferencias.temaOscuro
            ) {
                RecordatoriosNavHost(
                    viewModelFactory = viewModelFactory,
                    preferenciasFactory = preferenciasFactory
                )
            }
        }
    }
}

fun nombreCategoria(categoria: Categoria): String =
    when (categoria) {
        Categoria.Estudio -> "Estudio"
        Categoria.Trabajo -> "Trabajo"
        Categoria.Personal -> "Personal"
    }

fun nombrePrioridad(prioridad: Prioridad): String =
    when (prioridad) {
        Prioridad.Alta -> "Alta"
        Prioridad.Media -> "Media"
        Prioridad.Baja -> "Baja"
    }

fun nombreEstado(estado: EstadoRecordatorio): String =
    when (estado) {
        EstadoRecordatorio.Pendiente -> "Pendiente"
        EstadoRecordatorio.Completado -> "Completado"
        EstadoRecordatorio.Cancelado -> "Cancelado"
    }
