package com.oviedo.recordatorios.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oviedo.recordatorios.Recordatorio
import com.oviedo.recordatorios.data.EstadoSincronizacionGeneral
import com.oviedo.recordatorios.ui.components.BannerSincronizacion
import com.oviedo.recordatorios.ui.components.IndicadorCarga
import com.oviedo.recordatorios.ui.components.MensajeEstado
import com.oviedo.recordatorios.ui.components.TarjetaRecordatorio
import com.oviedo.recordatorios.ui.state.ListadoUiState
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModel
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModelFactory

/**
 * Punto de entrada con estado: obtiene el ViewModel y observa sus StateFlow.
 * El filtro y el orden ya vienen aplicados desde el repositorio.
 */
@Composable
fun PantallaListado(
    viewModelFactory: RecordatoriosViewModelFactory,
    onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (Int) -> Unit,
    onPreferenciasClick: () -> Unit
) {
    val recordatoriosViewModel: RecordatoriosViewModel = viewModel(factory = viewModelFactory)
    val listadoState by recordatoriosViewModel.listadoState.collectAsStateWithLifecycle()
    val estadoSincronizacion by recordatoriosViewModel.estadoSincronizacion
        .collectAsStateWithLifecycle()

    ContenidoListado(
        estado = listadoState,
        estadoSincronizacion = estadoSincronizacion,
        onCrearRecordatorio = onCrearRecordatorio,
        onRecordatorioClick = onRecordatorioClick,
        onPreferenciasClick = onPreferenciasClick,
        onReintentar = recordatoriosViewModel::cargarRecordatorios,
        onReintentarSincronizacion = recordatoriosViewModel::reintentarSincronizacion
    )
}

/**
 * Contenido sin estado: solo dibuja el UiState recibido y comunica acciones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoListado(
    estado: ListadoUiState,
    estadoSincronizacion: EstadoSincronizacionGeneral,
    onCrearRecordatorio: () -> Unit,
    onRecordatorioClick: (Int) -> Unit,
    onPreferenciasClick: () -> Unit,
    onReintentar: () -> Unit,
    onReintentarSincronizacion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Mis recordatorios") },
                actions = {
                    IconButton(onClick = onPreferenciasClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Preferencias")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCrearRecordatorio) {
                Icon(Icons.Filled.Add, contentDescription = "Crear recordatorio")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BannerSincronizacion(
                estado = estadoSincronizacion,
                onReintentar = onReintentarSincronizacion,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val contenidoModifier = Modifier
                .fillMaxWidth()
                .weight(1f)

            when (estado) {
                ListadoUiState.Cargando -> IndicadorCarga(
                    modifier = contenidoModifier,
                    mensaje = "Cargando recordatorios..."
                )

                is ListadoUiState.Vacio -> if (estado.hayOcultos) {
                    MensajeEstado(
                        titulo = "No hay recordatorios para mostrar",
                        mensaje = "Los recordatorios completados están ocultos. Podés mostrarlos desde Preferencias.",
                        textoAccion = "Abrir preferencias",
                        onAccion = onPreferenciasClick,
                        modifier = contenidoModifier
                    )
                } else {
                    MensajeEstado(
                        titulo = "Todavía no hay recordatorios",
                        mensaje = "Creá tu primer recordatorio para empezar.",
                        textoAccion = "Crear recordatorio",
                        onAccion = onCrearRecordatorio,
                        modifier = contenidoModifier
                    )
                }

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
}

@Composable
private fun ListaRecordatorios(
    recordatorios: List<Recordatorio>,
    onRecordatorioClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "${recordatorios.size} recordatorios",
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            // Espacio inferior para que el FAB no tape la última tarjeta.
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            items(items = recordatorios, key = { it.id }) { recordatorio ->
                TarjetaRecordatorio(
                    recordatorio = recordatorio,
                    onClick = { onRecordatorioClick(recordatorio.id) }
                )
            }
        }
    }
}
