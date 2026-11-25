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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oviedo.recordatorios.EstadoRecordatorio
import com.oviedo.recordatorios.EstadoSincronizacion
import com.oviedo.recordatorios.Recordatorio
import com.oviedo.recordatorios.nombreCategoria
import com.oviedo.recordatorios.nombreEstado
import com.oviedo.recordatorios.nombrePrioridad
import com.oviedo.recordatorios.ui.components.EtiquetaSincronizacion
import com.oviedo.recordatorios.ui.components.IndicadorCarga
import com.oviedo.recordatorios.ui.components.MensajeEstado
import com.oviedo.recordatorios.ui.state.DetalleUiState
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModel
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModelFactory
import java.text.DateFormat
import java.util.Date

@Composable
fun PantallaDetalle(
    recordatorioId: Int,
    viewModelFactory: RecordatoriosViewModelFactory,
    onVolver: () -> Unit,
    onEditar: (Int) -> Unit
) {
    val recordatoriosViewModel: RecordatoriosViewModel = viewModel(factory = viewModelFactory)
    val detalleState by recordatoriosViewModel.detalleState.collectAsStateWithLifecycle()
    val operacionEnCurso by recordatoriosViewModel.operacionEnCurso.collectAsStateWithLifecycle()
    val mensajeOperacion by recordatoriosViewModel.mensajeOperacion.collectAsStateWithLifecycle()

    LaunchedEffect(recordatorioId) {
        recordatoriosViewModel.cargarDetalle(recordatorioId)
    }

    ContenidoDetalle(
        estado = detalleState,
        operacionEnCurso = operacionEnCurso,
        mensajeOperacion = mensajeOperacion,
        onVolver = onVolver,
        onReintentar = { recordatoriosViewModel.cargarDetalle(recordatorioId) },
        onEditar = { onEditar(recordatorioId) },
        onAlternarCompletado = { recordatoriosViewModel.alternarCompletado(recordatorioId) },
        onEliminar = {
            recordatoriosViewModel.eliminarRecordatorio(recordatorioId, onEliminado = onVolver)
        },
        onReintentarSincronizacion = recordatoriosViewModel::reintentarSincronizacion
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoDetalle(
    estado: DetalleUiState,
    operacionEnCurso: Boolean,
    mensajeOperacion: String?,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onEditar: () -> Unit,
    onAlternarCompletado: () -> Unit,
    onEliminar: () -> Unit,
    onReintentarSincronizacion: () -> Unit,
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
                },
                actions = {
                    if (estado is DetalleUiState.Contenido) {
                        IconButton(onClick = onEditar, enabled = !operacionEnCurso) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar recordatorio")
                        }
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
                operacionEnCurso = operacionEnCurso,
                mensajeOperacion = mensajeOperacion,
                onAlternarCompletado = onAlternarCompletado,
                onEliminar = onEliminar,
                onReintentarSincronizacion = onReintentarSincronizacion,
                modifier = contenidoModifier
            )
        }
    }
}

@Composable
private fun TarjetaDetalle(
    recordatorio: Recordatorio,
    operacionEnCurso: Boolean,
    mensajeOperacion: String?,
    onAlternarCompletado: () -> Unit,
    onEliminar: () -> Unit,
    onReintentarSincronizacion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmarEliminacion by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                EtiquetaSincronizacion(estado = recordatorio.estadoSincronizacion)
                HorizontalDivider()
                FilaDato("Descripción", recordatorio.descripcion ?: "Sin descripción")
                FilaDato("Categoría", nombreCategoria(recordatorio.categoria))
                FilaDato("Prioridad", nombrePrioridad(recordatorio.prioridad))
                FilaDato("Estado", nombreEstado(recordatorio.estado))
                FilaDato(
                    "Fecha de creación",
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(recordatorio.fecha))
                )
                FilaDato("Identificador", recordatorio.id.toString())
            }
        }

        val estadoSync = recordatorio.estadoSincronizacion
        if (estadoSync is EstadoSincronizacion.Error) {
            Text(
                text = "El servidor rechazó el último cambio: ${estadoSync.mensaje}",
                color = MaterialTheme.colorScheme.error
            )
            OutlinedButton(
                onClick = onReintentarSincronizacion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reintentar sincronización")
            }
        }

        if (mensajeOperacion != null) {
            Text(
                text = "Error: $mensajeOperacion",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive }
            )
        }

        Button(
            onClick = onAlternarCompletado,
            enabled = !operacionEnCurso,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (recordatorio.estado == EstadoRecordatorio.Completado) {
                    "Marcar como pendiente"
                } else {
                    "Marcar como completado"
                }
            )
        }

        OutlinedButton(
            onClick = { confirmarEliminacion = true },
            enabled = !operacionEnCurso,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Eliminar recordatorio")
        }
    }

    if (confirmarEliminacion) {
        AlertDialog(
            onDismissRequest = { confirmarEliminacion = false },
            title = { Text("Eliminar recordatorio") },
            text = { Text("¿Querés eliminar \"${recordatorio.titulo}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminacion = false
                    onEliminar()
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminacion = false }) { Text("Cancelar") }
            }
        )
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
