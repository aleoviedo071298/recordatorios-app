package com.oviedo.recordatorios
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModelFactory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.oviedo.recordatorios.ui.theme.RecordatoriosAppTheme
import com.oviedo.recordatorios.ui.navigation.RecordatoriosNavHost

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repositorio = (application as RecordatoriosApplication).repositorio
        val viewModelFactory = RecordatoriosViewModelFactory(repositorio)
        setContent {
            RecordatoriosAppTheme {
                RecordatoriosNavHost(
                    viewModelFactory = viewModelFactory
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