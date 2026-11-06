package com.oviedo.recordatorios.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oviedo.recordatorios.Categoria
import com.oviedo.recordatorios.Prioridad
import com.oviedo.recordatorios.nombreCategoria
import com.oviedo.recordatorios.nombrePrioridad
import com.oviedo.recordatorios.ui.components.CampoTexto
import com.oviedo.recordatorios.ui.components.SelectorOpciones
import com.oviedo.recordatorios.ui.state.EstadoEnvio
import com.oviedo.recordatorios.ui.state.FormularioUiState
import com.oviedo.recordatorios.ui.state.LONGITUD_MAXIMA_DESCRIPCION
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModel
import com.oviedo.recordatorios.ui.viewmodel.RecordatoriosViewModelFactory

private val categorias = listOf(Categoria.Estudio, Categoria.Trabajo, Categoria.Personal)
private val prioridades = listOf(Prioridad.Alta, Prioridad.Media, Prioridad.Baja)

/**
 * Punto de entrada con estado: conecta el formulario con el ViewModel.
 */
@Composable
fun PantallaCrear(
    viewModelFactory: RecordatoriosViewModelFactory,
    onCancelar: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    val recordatoriosViewModel: RecordatoriosViewModel = viewModel(factory = viewModelFactory)
    val formularioState by recordatoriosViewModel.formularioState.collectAsStateWithLifecycle()

    LaunchedEffect(formularioState.estadoEnvio) {
        if (formularioState.estadoEnvio == EstadoEnvio.Exito) {
            onGuardadoExitoso()
        }
    }

    FormularioRecordatorio(
        estado = formularioState,
        onTituloChange = recordatoriosViewModel::actualizarTitulo,
        onDescripcionChange = recordatoriosViewModel::actualizarDescripcion,
        onCategoriaSeleccionada = recordatoriosViewModel::actualizarCategoria,
        onPrioridadSeleccionada = recordatoriosViewModel::actualizarPrioridad,
        onGuardar = recordatoriosViewModel::guardar,
        onCancelar = onCancelar
    )
}

/**
 * Formulario sin estado: recibe el UiState y comunica cada evento mediante lambdas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioRecordatorio(
    estado: FormularioUiState,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onCategoriaSeleccionada: (Categoria) -> Unit,
    onPrioridadSeleccionada: (Prioridad) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Nuevo recordatorio") },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancelar y volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoTexto(
                valor = estado.titulo,
                onValorChange = onTituloChange,
                etiqueta = "Título",
                mensajeError = estado.errorTitulo,
                ayuda = "Mínimo 4 caracteres"
            )

            CampoTexto(
                valor = estado.descripcion,
                onValorChange = onDescripcionChange,
                etiqueta = "Descripción (opcional)",
                mensajeError = estado.errorDescripcion,
                ayuda = "${estado.descripcion.length}/$LONGITUD_MAXIMA_DESCRIPCION",
                minLineas = 3
            )

            SelectorOpciones(
                titulo = "Categoría",
                opciones = categorias,
                seleccionada = estado.categoria,
                nombreOpcion = ::nombreCategoria,
                onSeleccion = onCategoriaSeleccionada,
                mensajeError = estado.errorCategoria
            )

            SelectorOpciones(
                titulo = "Prioridad",
                opciones = prioridades,
                seleccionada = estado.prioridad,
                nombreOpcion = ::nombrePrioridad,
                onSeleccion = onPrioridadSeleccionada,
                mensajeError = estado.errorPrioridad
            )

            if (estado.estadoEnvio is EstadoEnvio.Error) {
                Text(
                    text = "Error: ${estado.estadoEnvio.mensaje} Podés intentar nuevamente.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive }
                )
            }

            Button(
                onClick = onGuardar,
                enabled = estado.puedeGuardar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (estado.enviando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text(
                        when {
                            estado.enviando -> "Guardando..."
                            estado.estadoEnvio is EstadoEnvio.Error -> "Reintentar guardado"
                            else -> "Guardar recordatorio"
                        }
                    )
                }
            }

            OutlinedButton(
                onClick = onCancelar,
                enabled = !estado.enviando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}
