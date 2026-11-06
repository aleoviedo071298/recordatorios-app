package com.oviedo.recordatorios.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oviedo.recordatorios.Recordatorio
import com.oviedo.recordatorios.nombreCategoria
import com.oviedo.recordatorios.nombreEstado
import com.oviedo.recordatorios.nombrePrioridad
import com.oviedo.recordatorios.ui.components.IndicadorCarga
import com.oviedo.recordatorios.ui.components.MensajeEstado
import com.oviedo.recordatorios.ui.state.DetalleUiState
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModel
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModelFactory

@Composable
fun PantallaDetalle(
    recordatorioId: Int,
    viewModelFactory: RecordatoriosViewModelFactory,
    onVolver: () -> Unit
) {
    val recordatoriosViewModel: RecordatoriosViewModel = viewModel(factory = viewModelFactory)
    val detalleState by recordatoriosViewModel.detalleState.collectAsStateWithLifecycle()

    LaunchedEffect(recordatorioId) {
        recordatoriosViewModel.cargarDetalle(recordatorioId)
    }

    ContenidoDetalle(
        estado = detalleState,
        onVolver = onVolver,
        onReintentar = { recordatoriosViewModel.cargarDetalle(recordatorioId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoDetalle(
    estado: DetalleUiState,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Detalle del recordatorio") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al listado"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val contenidoModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (estado) {
            DetalleUiState.Cargando -> IndicadorCarga(
                modifier = contenidoModifier,
                mensaje = "Cargando recordatorio..."
            )

            is DetalleUiState.Error -> MensajeEstado(
                titulo = "No se pudo mostrar el recordatorio",
                mensaje = estado.mensaje,
                esError = true,
                textoAccion = "Reintentar",
                onAccion = onReintentar,
                modifier = contenidoModifier
            )

            is DetalleUiState.Contenido -> TarjetaDetalle(
                recordatorio = estado.recordatorio,
                modifier = contenidoModifier
            )
        }
    }
}

@Composable
private fun TarjetaDetalle(
    recordatorio: Recordatorio,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = recordatorio.titulo,
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider()
                FilaDato("Descripción", recordatorio.descripcion ?: "Sin descripción")
                FilaDato("Categoría", nombreCategoria(recordatorio.categoria))
                FilaDato("Prioridad", nombrePrioridad(recordatorio.prioridad))
                FilaDato("Estado", nombreEstado(recordatorio.estado))
                FilaDato("Identificador", recordatorio.id.toString())
            }
        }
    }
}

@Composable
private fun FilaDato(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valor, style = MaterialTheme.typography.bodyLarge)
    }
}
