package com.oviedo.recordatorios.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oviedo.recordatorios.Recordatorio
import com.oviedo.recordatorios.ui.components.IndicadorCarga
import com.oviedo.recordatorios.ui.components.MensajeEstado
import com.oviedo.recordatorios.ui.components.TarjetaRecordatorio
import com.oviedo.recordatorios.ui.state.ListadoUiState
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModel
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModelFactory

/**
 * Punto de entrada con estado: obtiene el ViewModel y observa su StateFlow.
 */
@Composable
fun PantallaListado(
    viewModelFactory: RecordatoriosViewModelFactory,
    onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (Int) -> Unit
) {
    val recordatoriosViewModel: RecordatoriosViewModel = viewModel(factory = viewModelFactory)
    val listadoState by recordatoriosViewModel.listadoState.collectAsStateWithLifecycle()

    // Recarga al mostrarse y al volver desde la creación.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        recordatoriosViewModel.cargarRecordatorios()
    }

    ContenidoListado(
        estado = listadoState,
        onCrearRecordatorio = onCrearRecordatorio,
        onRecordatorioClick = onRecordatorioClick,
        onReintentar = recordatoriosViewModel::cargarRecordatorios
    )
}

/**
 * Contenido sin estado: solo dibuja el UiState recibido y comunica acciones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoListado(
    estado: ListadoUiState,
    onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (Int) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Mis recordatorios") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearRecordatorio) {
                Icon(Icons.Filled.Add, contentDescription = "Crear recordatorio")
            }
        }
    ) { innerPadding ->
        val contenidoModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (estado) {
            ListadoUiState.Cargando -> IndicadorCarga(
                modifier = contenidoModifier,
                mensaje = "Cargando recordatorios..."
            )

            ListadoUiState.Vacio -> MensajeEstado(
                titulo = "Todavía no hay recordatorios",
                mensaje = "Creá tu primer recordatorio para empezar.",
                textoAccion = "Crear recordatorio",
                onAccion = onCrearRecordatorio,
                modifier = contenidoModifier
            )

            is ListadoUiState.Error -> MensajeEstado(
                titulo = "Ocurrió un error",
                mensaje = estado.mensaje,
                esError = true,
                textoAccion = "Reintentar",
                onAccion = onReintentar,
                modifier = contenidoModifier
            )

            is ListadoUiState.Contenido -> ListaRecordatorios(
                recordatorios = estado.recordatorios,
                onRecordatorioClick = onRecordatorioClick,
                modifier = contenidoModifier
            )
        }
    }
}

@Composable
private fun ListaRecordatorios(
    recordatorios: List<Recordatorio>,
    onRecordatorioClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "${recordatorios.size} recordatorios registrados",
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            // Espacio inferior para que el FAB no tape la última tarjeta.
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            items(recordatorios, key = { it.id }) { recordatorio ->
                TarjetaRecordatorio(
                    recordatorio = recordatorio,
                    onClick = { onRecordatorioClick(recordatorio.id) }
                )
            }
        }
    }
}
